# LINZ Address Geocoder

This repository contains a basic geocoding service for [LINZ addresses](https://data.linz.govt.nz/layer/105689-nz-addresses/).  That is, we accept address strings as input, and return address coordinates if a matching address is found in the LINZ address database.

Note that it makes use of pre-trained neural network to parse address components.  This is described [here](https://github.com/cmhh/linzaddressparse).

# Getting Started

The library is provided as an [sbt](https://www.scala-sbt.org/) project.  A fat jar can be made simply by running:

```bash
sbt assembly
```

The library uses [deeplearning4j](https://deeplearning4j.konduit.ai/) which is a relatively large library.  If we build a fat jar with dl4j included it will be relatively large--about 1.4GB.  Either way, once built, the service can be started by running:

```bash
java -jar target/scala-2.13/linzgeocode.jar org.cmhh.linzaddressparse.Service
```

The service assumes a PostgreSQL database containing the LINZ addresses is accessible and will not function if this is not present, and if the database connection is not configured correctly.  The default application configuration looks as follows:

```hcon
db {
  connectionPool = "HikariCP" 
  dataSourceClass = "org.postgresql.ds.PGSimpleDataSource" 
  properties = {
    serverName = "localhost"
    portNumber = "5432"
    databaseName = "gis"
    user = "gisuser"
    password = "gisuser"
  }
  numThreads = 10
  minConnections = 10
  maxConnections = 50
  queueSize = 1000
}

data {
  schema = "linz"
  table = "addresses_v"
  hasPostcode = "no"
  lng = "gd2000_xcoord"
  lat = "gd2000_ycoord"
}

app {
  version = "0.1.0"
  prefix = "linzgeocode"
  port = 9001
}

search {
  maxResults = 10
}
```

An alternative configuration can be provided by passing additional parameters on startup.  E.g.:

```bash
java -Dconfig.file=/path/to/application.conf -cp linzgeocode.jar org.cmhh.linzgeocode.Service
```

Users can optionally make the service postcode aware if the address table itself contains postcode.  For example, assume we have a table containing postcode and we create the following materialised view:

```sql
create materialized view 
  linz.addresses_v 
as select 
  address_id, 
  unit_type, unit_value, 
  level_type, level_value, 
  address_number, address_number_suffix, address_number_high, 
  road_name, road_type_name, road_suffix, 
  s.suburb_locality, 
  case 
    when town_city is null then s.major_name 
    else town_city 
  end as town_city, 
  p.code as postcode,
  gd2000_xcoord as lng, gd2000_ycoord as lat 
from 
  linz.addresses a 
left join 
  linz.suburbs_and_localities s 
on 
  st_intersects(a.geom, s.geom) 
left join 
  nzpost.postcode p 
on 
  st_intersects(a.geom, p.geom)
```

We might then supply the alternative configuration:

```hcon
db {
  connectionPool = "HikariCP" 
  dataSourceClass = "org.postgresql.ds.PGSimpleDataSource" 
  properties = {
    serverName = "localhost"
    portNumber = "5432"
    databaseName = "gis"
    user = "gisuser"
    password = "gisuser"
  }
  numThreads = 10
  minConnections = 10
  maxConnections = 50
  queueSize = 1000
}

data {
  schema = "linz"
  table = "addresses_v"
  hasPostcode = "yes"
  postcode = "postcode"
  lng = "lng"
  lat = "lat"
}

app {
  version = "0.1.0"
  prefix = "linzgeocode"
  port = 9001
}

search {
  maxResults = 10
}
```

Once running, a GET endpoint is available at `/linzgeocode/geocode`, and it takes just two query paramters:

* `text` - address string, e.g. `1 Gonville Street, Tawa, Wellington 5028`
* `detailed` - whether or not the individual LINZ components should be returned, or just the location.

It is up to users to encode the URL correctly if required--for example, cURL requires this.  Note that many place names in New Zealand are M&#0101;ori words so the vowels with macrons are very common.  

letter   | encoding
---------|----------
&#x0100; | `%C4%80`
&#x0112; | `%C4%92`
&#x012A; | `%C4%AA`
&#x014C; | `%C5%8C`
&#x016A; | `%C5%AA`
&#x0101; | `%C4%81`
&#x0113; | `%C4%93`
&#x012B; | `%C4%AB`
&#x014D; | `%C5%8D`
&#x016B; | `%C5%AB`

R has a handy built-in function, `URLencode` which seems to work well generally, Java has `java.net.URLEncoder.encode`, and other languages probably have something similar.  Modern browsers such as Chrome will almost certainly encode strings correctly for you. Either way, we can geocode the address:

    1 Gonville Street, Tawa, Wellington 5028

as follows:


```bash
curl -s \
  "http://localhost:9001/linzgeocode/geocode?text=1%20Gonville%20Street,%20Tawa,%20Wellington%205028" | jq
```
```json
[
  {
    "linz_id": 384665,
    "relevance": 1,
    "formatted_address": "1 Gonville Street, Tawa, Wellington 5028",
    "coordinates": {
      "longitude": 174.81836305,
      "latitude": -41.18021768
    }
  }
]
```

Or, if we do want more detail:


```bash
curl -s \
  "http://localhost:9001/linzgeocode/geocode?text=1%20Gonville%20Street,%20Tawa,%20Wellington%205028&detailed=true" | jq
```
```json
[
  {
    "linz_id": 384665,
    "relevance": 1,
    "formatted_address": "1 Gonville Street, Tawa, Wellington 5028",
    "coordinates": {
      "longitude": 174.81836305,
      "latitude": -41.18021768
    },
    "components": {
      "id": 384665,
      "unit_type": null,
      "unit_value": null,
      "level_type": null,
      "level_value": null,
      "address_number": 1,
      "address_number_suffix": null,
      "address_number_high": null,
      "road_name": "Gonville",
      "road_type_name": "Street",
      "road_suffix": null,
      "suburb_locality": "Tawa",
      "postcode": "5028",
      "town_city": "Wellington",
      "lng": 174.81836305,
      "lat": -41.18021768
    }
  }
]
```

Alternatively, a POST endpoint is available at `/linzgeocode/geocode` which just expects a JSON object to be provided in the body.  For example:

```bash
curl -s -X POST \
  -d '{"text": "1 Gonville Street, Tawa, Wellington 5028", "detailed": false}' \
  http://localhost:9002/linzgeocode/geocode | jq
```

# Docker Compose

The easiest way to run a service like this is probably to use Docker Compose--we have one container with PostgreSQL + PostGIS, populated with our address data; and we have another container with the web service itself.  A basic setup is provided, and we can start everything by running:

```bash
docker compose up -d
```

On first run, the postgis database will be empty.  Users will need to source the LINZ addresses](https://data.linz.govt.nz/layer/105689-nz-addresses/) themselves, and load this as a one-time process.  
Assuming the addresses have been downloaded as a single geopackage, and saved to the folder `data/linz/nz-addresses.gpkg`, we access the database container by running:

```bash
docker exec -it postgis /bin/bash
```

We then populate our database by running:

```bash
bash /data/load.sh
```


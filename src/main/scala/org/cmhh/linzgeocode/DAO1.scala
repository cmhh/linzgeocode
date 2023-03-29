package org.cmhh.linzgeocode

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{Future, ExecutionContext}
import com.typesafe.config.{Config, ConfigFactory}
import dbextensions._

object DAO1 {
  type Result = (Address, Double)
  type Results = Seq[Result]

  val conf = ConfigFactory.load()
  val conn = Database.forConfig("db")
  val addresses = TableQuery[AddressTable]

  def searchQuery(x: AddressComponents, limit: Int) = {
    val q1 = addresses
      .filter(_.postcode isUnlessNull x.postcode)
      .filter(_.townCity.toLowerCase isUnlessNull x.townCity.map(_.toLowerCase))
      .filter(_.suburbLocality.toLowerCase isUnlessNull x.suburbLocality.map(_.toLowerCase))
      .filter(_.roadName.toLowerCase isUnlessNull x.roadName.map(_.toLowerCase))
      .filter(_.roadTypeName.toLowerCase isUnlessNull x.roadTypeName.map(_.toLowerCase))
      .filter(_.addressNumber isUnlessNull x.addressNumber.map(_.toInt))

    val q2 = q1
      .filter(_.addressNumberSuffix.toLowerCase isUnlessNull x.addressNumberSuffix.map(_.toLowerCase))
      .filter(_.unitValue.toLowerCase isUnlessNull x.unitValue.map(_.toLowerCase))

    q1.take(limit) ++ q2 .take(limit)
  }

  def search(
    x: AddressComponents, limit: Int
  )(implicit ec: ExecutionContext): Future[Results] = {
    conn
      .run(searchQuery(x, limit).result)
      .map(rs => rs.map(r => (r, x.relevance(r))))
      .map(x => x.sortBy(y => -y._2).take(limit).distinct)
  } 

  def searchAndFormat(
    x: AddressComponents, limit: Int, components: Boolean
  )(implicit ec: ExecutionContext): Future[String] = {
    search(x, limit).map(x => formatResults(x.sortBy(y => - y._2), components))
  }

  def formatResults(xs: Results, components: Boolean) = {
    "[" +
      xs.map(x => formatResult(x, components)).mkString(",") +
    "]"
  }

  def formatResult(r: Result, components: Boolean): String = {
    "{" +
       s""""linz_id": ${r._1.id},""" +
       s""""relevance":${r._2},""" +  
       s""""formatted_address":"${r._1.toString}",""" + 
       s""""coordinates":{"longitude":${r._1.lng.getOrElse("""null""")},"latitude":${r._1.lat.getOrElse("""null""")}}""" +
       { if (components) s""","components":${r._1.toJson}""" else "" } +
    "}"
  }

  def roads(): Future[Seq[(Option[String], Option[String], Option[String])]] = {    
    conn.run(
      addresses
        .map(x => (x.roadName.toLowerCase, x.roadTypeName.toLowerCase, x.roadSuffix.toLowerCase))
        .sortBy(_._1)
        .sortBy(_._2)
        .sortBy(_._3)
        .distinct
        .result
    )
  }

  def suburbs(): Future[Seq[(Option[String])]] = {    
    conn.run(
      addresses
        .filter(_.suburbLocality.isDefined)
        .map(_.suburbLocality.toLowerCase)
        .sortBy(x => x)
        .distinct
        .result
    )
  }

  def towns(): Future[Seq[(Option[String])]] = {
    conn.run(
      addresses
        .filter(_.townCity.isDefined)
        .map(_.townCity.toLowerCase)
        .sortBy(x => x)
        .distinct
        .result
    )
  }

  class AddressTable(tag: Tag) 
  extends Table[Address](tag, Some(conf.getString("data.schema")), conf.getString("data.table")) {
    def id = column[Long]("address_id", O.PrimaryKey)
    def unitType = column[Option[String]]("unit_type")
    def unitValue = column[Option[String]]("unit_value")
    def levelType = column[Option[String]]("level_type")
    def levelValue = column[Option[String]]("level_value")
    def addressNumber = column[Option[Int]]("address_number")
    def addressNumberSuffix = column[Option[String]]("address_number_suffix")
    def addressNumberHigh = column[Option[Int]]("address_number_high")
    def roadName = column[Option[String]]("road_name")
    def roadTypeName = column[Option[String]]("road_type_name")
    def roadSuffix = column[Option[String]]("road_suffix")
    def suburbLocality = column[Option[String]]("suburb_locality")
    def townCity = column[Option[String]]("town_city")
    def postcode = column[Option[String]](conf.getString("data.postcode"))
    def lng = column[Option[Double]](conf.getString("data.lng"))
    def lat = column[Option[Double]](conf.getString("data.lat"))

    def * = (
      id, unitType, unitValue, levelType, levelValue, 
      addressNumber, addressNumberSuffix, 
      addressNumberHigh, 
      roadName, roadTypeName, roadSuffix, 
      suburbLocality, townCity, postcode, 
      lng, lat
    ).mapTo[Address]
  }
}
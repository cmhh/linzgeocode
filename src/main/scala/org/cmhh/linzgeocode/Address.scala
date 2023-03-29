package org.cmhh.linzgeocode

case class Address(
  id: Long,
  unitType: Option[String],
  unitValue: Option[String],
  levelType: Option[String],
  levelValue: Option[String],
  addressNumber: Option[Int],
  addressNumberSuffix: Option[String],
  addressNumberHigh: Option[Int],
  roadName: Option[String],
  roadTypeName: Option[String],
  roadSuffix: Option[String],
  suburbLocality: Option[String],
  townCity: Option[String],
  postcode: Option[String],
  lng: Option[Double],
  lat: Option[Double]
) {
  lazy override val toString: String = _fullAddress.getOrElse("")

  lazy val toJson: String = 
    "{" +
    s""""id":$id,"unit_type":${s(unitType)},""" +
    s""""unit_value":${s(unitValue)},""" + 
    s""""level_type":${s(levelType)},""" +
    s""""level_value":${s(levelValue)},""" +
    s""""address_number":${n(addressNumber)},""" + 
    s""""address_number_suffix":${s(addressNumberSuffix)},""" + 
    s""""address_number_high":${n(addressNumberHigh)},""" + 
    s""""road_name":${s(roadName)},""" +
    s""""road_type_name":${s(roadTypeName)},""" + 
    s""""road_suffix":${s(roadSuffix)},""" +
    s""""suburb_locality":${s(suburbLocality)},""" +
    s""""postcode":${s(postcode)},""" +
    s""""town_city":${s(townCity)},""" +
    s""""lng":${n(lng)},""" +
    s""""lat":${n(lat)}""" + 
    "}"

  private lazy val _streetAddress: Option[String] = {
    _street match {
      case Some(s) => _streetNumber.map(x => s"$x $s")
      case None => None
    }
  }

  private lazy val _fullAddress: Option[String] = {
    _streetAddress match {
      case Some(s) => {
        if (suburbLocality.isDefined & _town.isDefined) {
          suburbLocality.flatMap(x => _town.map(y => s"$s, $x, $y"))
        } else if (suburbLocality.isDefined) {
          suburbLocality.map(x => s"$s, $x")
        } else if (_town.isDefined) {
          _town.map(x => s"$s, $x")
        } else {
          Some(s)
        }
      }
      case None => None
    }
  }

  private def s(x: Option[String]): String = x match {
    case Some(v) => s""""$v""""
    case None => "null"
  } 

  private def n[T](x: Option[T]): String = x match {
    case Some(v) => s"$v"
    case None => "null"
  } 

  private lazy val _level: Option[String] = {
    levelValue match {
      case Some(n) => 
        levelType match {
          case Some(l) => Some(s"$l $n")
          case None => Some(s"LEVEL $n")
        }
      case None => None
    }
  }

  private lazy val _n1: Option[String] = {
    addressNumber match {
      case Some(n) => {
        if (unitValue.isDefined & addressNumberSuffix.isDefined) {
          unitValue.flatMap(x => addressNumberSuffix.map(y => s"$x/$n$y"))
        } else if (unitValue.isDefined) {
          unitValue.map(x => s"$x/$n")
        } else if (addressNumberSuffix.isDefined) {
          addressNumberSuffix.map(x => s"$n$x")
        } else Some(s"$n")
      }
      case None => None
    }
  } 

  private lazy val _n2: Option[String] = {
    _n1 match {
      case Some(n) => 
        addressNumberHigh match {
          case Some(h) => Some(s"$n-$h")
          case None => Some(n)
        }
      case None => None
    }
  }

  private lazy val _streetNumber: Option[String] = {
    _n2 match {
      case Some(x) => 
        _level match {
          case Some(y) => Some(s"$y $x")
          case None => Some(x)
        }
      case None => None
    }
  }

  private lazy val _street: Option[String] = {
    roadName match {
      case Some(r) => {
        if (roadTypeName.isDefined & roadSuffix.isDefined) {
          roadTypeName.flatMap(x => roadSuffix.map(y => s"$r $x $y"))
        } else if (roadTypeName.isDefined) {
          roadTypeName.map(x => s"$r $x")
        } else if (roadSuffix.isDefined) {
          roadSuffix.map(x => s"$r $x")
        } else Some(r)
      }
      case None => None
    }
  }

  private lazy val _town: Option[String] = {
    townCity match {
      case Some(t) => 
        postcode match {
          case Some(p) => Some(s"$t $p")
          case None => Some(t)
        }
      case None => None
    }
  }
}

case object Address {
}
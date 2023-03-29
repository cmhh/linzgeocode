package org.cmhh.linzgeocode

object unittype{
  sealed trait UnitType
  case object APARTMENT extends UnitType { override val toString = "Apartment" }
  case object FLAT extends UnitType { override val toString = "Flat" }
  case object UNIT extends UnitType { override val toString = "Unit" }
  case object VILLA extends UnitType { override val toString = "Villa" }
  case object SUITE extends UnitType { override val toString = "Suite" }
  case object SHOP extends UnitType { override val toString = "Shop" }
}
package org.cmhh.linzgeocode

import org.deeplearning4j.nn.graph.ComputationGraph

case class AddressComponents(
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
  postcode: Option[String]
) {
  lazy val toJson: String = 
    "{" +
    s""""unit_type":${s(unitType)},""" +
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
    s""""town_city":${s(townCity)},""" +
    s""""postcode":${s(postcode)}""" +
    "}"

  def relevance(address: Address): Double = {
    def same[T](x: Option[T], y: Option[T]): Boolean = {
      x match {
        case None => 
          y match {
            case None => true
            case Some(b) => false
          }
        case Some(a) => 
          y match {
            case None => false
            case Some(b) => a == b
          }
      }
    }

    def l(x: Option[String]): Option[String] = x.map(_.toLowerCase)

    val numberMatch = 
      same(addressNumber, address.addressNumber) & 
      same(unitValue, address.unitValue) & 
      same(l(addressNumberSuffix), l(address.addressNumberSuffix)) & 
      same(levelValue, address.levelValue)

    val numberSimilar = 
      same(addressNumber, address.addressNumber) & 
      (
        same(unitValue, address.unitValue) | 
        same(addressNumberSuffix, address.addressNumberSuffix) | 
        same(levelValue, address.levelValue)
      )

    val roadMatch = 
      same(l(roadName), l(address.roadName)) & 
      same(l(roadTypeName), l(address.roadTypeName)) & 
      same(l(roadSuffix), l(address.roadSuffix))
    
    val roadSimilar = 
      same(l(roadName), l(address.roadName)) 

    val placeMatch = 
      (
        same(l(suburbLocality), l(address.suburbLocality)) & 
        same(l(townCity), l(address.townCity))
      ) | 
      same(postcode, address.postcode)

    val placeSimilar = 
      same(l(suburbLocality), l(address.suburbLocality)) |
      same(l(townCity), l(address.townCity))

    if (
      numberMatch & roadMatch & placeMatch
    ) {
      1.0
    } else if (
      numberMatch & roadMatch  & placeSimilar
    ) {
      0.95
    } else if (
      numberSimilar & roadMatch & placeMatch
    ) {
      0.9
    } else if (
      numberSimilar & roadSimilar & placeMatch
    ) {
      0.85
    } else if (
      numberMatch & roadMatch
    ) {
      0.8
    } else if (
      numberSimilar & roadSimilar
    ) {
      0.5
    } else 0.0
  }

  def repair: AddressComponents = {
    val (rname, rtype, rsuffix) = Roads.find(roadName, roadTypeName, roadSuffix)(0)
    val suburb = suburbLocality.flatMap(x => Suburbs.find(x)(0))
    val town = townCity.flatMap(x => Towns.find(x)(0))

    this
      .setRoadName(rname)
      .setRoadTypeName(rtype)
      .setRoadSuffix(rsuffix)
      .setSuburbLocality(suburb)
      .setTownCity(town)
  }

  def setUnitType(x: Option[String]): AddressComponents =  this.copy(unitType = x)
  def setUnitValue(x: Option[String]): AddressComponents = this.copy(unitValue = x)
  def setLevelValue(x: Option[String]): AddressComponents = this.copy(levelValue = x)
  def setAddressNumber(x: Option[Int]): AddressComponents = this.copy(addressNumber = x)
  def setAddressNumberSuffix(x: Option[String]): AddressComponents = this.copy(addressNumberSuffix = x)
  def setAddressNumberHigh(x: Option[Int]): AddressComponents = this.copy(addressNumberHigh = x)
  def setRoadName(x: Option[String]): AddressComponents = this.copy(roadName = x)
  def setRoadTypeName(x: Option[String]): AddressComponents = this.copy(roadTypeName = x)
  def setRoadSuffix(x: Option[String]): AddressComponents = this.copy(roadSuffix = x)
  def setSuburbLocality(x: Option[String]): AddressComponents = this.copy(suburbLocality = x)
  def setPostcode(x: Option[String]): AddressComponents = this.copy(postcode = x)
  def setTownCity(x: Option[String]): AddressComponents = this.copy(townCity = x)

  private def s(x: Option[String]): String = x match {
    case Some(v) => s""""$v""""
    case None => "null"
  } 

  private def n[T](x: Option[T]): String = x match {
    case Some(v) => s"$v"
    case None => "null"
  } 
}

case object AddressComponents {
  def apply(addr: String)(implicit m: ComputationGraph): AddressComponents = parse.parse(addr).getOrElse(empty)

  val empty: AddressComponents = AddressComponents(
    None, None, None, None, None, None, None, None, None, None, None, None, None
  )
}
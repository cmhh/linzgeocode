package org.cmhh.linzgeocode 

import org.deeplearning4j.util.ModelSerializer
import org.deeplearning4j.nn.graph.ComputationGraph
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import scala.util.{Try, Success, Failure}

object parse {
  implicit val m: ComputationGraph = 
    ModelSerializer.restoreComputationGraph(getClass.getResourceAsStream("/model.mdl"))

  def parse0(address: String): Try[AddressComponents] = {
    try {
      val p1 = "(.*), (.*), (.*) (\\d{4})".r
      val p2 = "(.*), (.*), (.*)".r
      val p3 = "(.*), (.*) (\\d{4})".r
      val p4 = "(.*), (.*)".r

      val (addr, suburb, city, postcode): 
        (Option[String], Option[String], Option[String], Option[String]) = 
        if (p1.matches(address)) {
          val p1(addr_, suburb_, city_, postcode_) = address
          (Some(addr_), Some(suburb_), Some(city_), Some(postcode_))
        } else if (p2.matches(address)) {
          val p2(addr_, suburb_, city_) = address
          (Some(addr_), Some(suburb_), Some(city_), None)
        } else if (p3.matches(address)) {
          val p3(addr_, city_, postcode_) = address
          (Some(addr_), None, Some(city_), Some(postcode_))
        } else if (p4.matches(address)) {
          val p4(addr_, suburb_) = address
          (Some(addr_), Some(suburb_), None, None)
        } else {
          (Some(address), None, None, None)
        }       

      val (unit, num, suffix, streetName, streetType) = addr match {
        case Some(a) => parseAddress(a)
        case None => (None, None, None, None, None)
      }

      Success(AddressComponents(
        None,
        unit,
        None,
        None,
        num,
        suffix,
        None,
        streetName,
        streetType,
        None,
        suburb,
        city,
        postcode
      ))
    } catch {
      case _: Throwable => Failure(new Exception("Failed to parse address string."))
    }
  }

  def parse(address: String)(implicit m: ComputationGraph): Try[AddressComponents] = Try {
    val yhat = m.output(encode(address))
    val labels = decodeLabels(yhat(0), 0)
    val tagged = address.toVector.zip(labels)

    def get(label: String): Option[String] = {
      val res = tagged.filter(_._2 == label).map(_._1).mkString
      if (res.size == 0) None else Some(res)
    }

    AddressComponents(
      get("unit_type"), get("unit_value"), 
      get("level_type"), get("level_value"), 
      get("address_number").map(_.toInt), 
      get("address_number_suffix"), 
      get("address_number_high").map(_.toInt),
      get ("road_name"), get("road_type_name"), get("road_suffix"), 
      get("suburb_locality"), get("town_city"), get("postcode")
    )
  }

  private def parseAddress(str: String): 
    (Option[String], Option[Int], Option[String], Option[String], Option[String]) = {
    val parts = str.split("\\s+").toList
    if (parts.size == 1) {
      val (unit, num, suffix) = parseStreetNum(str)
      (unit, num, suffix, None, None)
    } else {
      val (streetName, streetType) = parseStreet(parts.tail.mkString(" "))
      val (unit, num, suffix) = parseStreetNum(parts.head)
      (unit, num.map(_.toInt), suffix, streetName, streetType)
    }    
  }

  private def parseStreet(str: String): (Option[String], Option[String]) = {
    val parts = str.split("\\s+").toList
    if (parts.size == 1) (Some(str), None)
    else (Some(parts.dropRight(1).mkString(" ")), Some(parts.last))
  }

  private def parseStreetNum(str: String): (Option[String], Option[Int], Option[String]) = {
    val e1 = "\\d+".r
    val e2 = "(\\d+)([a-zA-z]+)".r
    val e3 = "(\\d+)/(\\d+)".r
    val e4 = "(\\d+)/(\\d+)([a-zA-z]+)".r

    if (e1.matches(str)) {
      (None, Some(str.toInt), None)
    } else if (e2.matches(str)) {
      val e2(num, suffix) = str
      (None, Some(num.toInt), Some(suffix))
    } else if (e3.matches(str)) {
      val e3(unit, num) = str
      (Some(unit), Some(num.toInt), None)
    } else if (e4.matches(str)) {
      val e4(unit, num, suffix) = str
      (Some(unit), Some(num.toInt), Some(suffix))
    } else (None, None, None)
  }

  private def encode(x: String): INDArray = {
    val onehot = x.toLowerCase.toArray.map(x => Vocab(x).toArray)
    Nd4j.pile(Nd4j.createFromArray(onehot).transpose)
  }

  private def decodeLabels(x: INDArray): Vector[String] = {
    val n = x.shape()(1).toInt
    val x_ = x.transpose()

    (0 until n).toVector.map(j => {
      val y = x_.slice(j).toFloatVector()
      Labels.names(y.indexOf(y.max))
    })
  }

  private def decodeLabels(x: INDArray, i: Int): Vector[String] = {
    val n = x.shape()(2).toInt
    val x_ = x.slice(i).transpose()

    (0 until n).toVector.map(j => {
      val y = x_.slice(j).toFloatVector()
      Labels.names(y.indexOf(y.max))
    })
  }
}
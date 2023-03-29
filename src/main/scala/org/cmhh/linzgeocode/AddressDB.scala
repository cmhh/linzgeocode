/*
package org.cmhh.linzgeocode

import java.io.{InputStream, FileInputStream}
import java.util.zip.GZIPInputStream
import scala.io.Source
import scala.collection.mutable.ArrayBuffer

case class AddressDB(data: Vector[Address])

case object AddressDB {
  def apply(source: Source): AddressDB = {
    val it = source.getLines()
    val hdr = it.next().split(',')

    def getInt(str: String): Option[Int] = {
      try {
        Some(str.toInt)
      } catch {
        case e: Exception => None
      }
    }

    def getDouble(str: String): Option[Double] = {
      try {
        Some(str.toDouble)
      } catch {
        case e: Exception => None
      }
    }

    def getStr(str: String): Option[String] = {
      if (str == "") None else Some(str)
    }

    def loop(it: Iterator[String], accum: Vector[Address]): Vector[Address] = {
      if (!it.nonEmpty) accum else {
        val line = it.next().split(',')
        
        getInt(line(hdr.indexOf("address_id"))) match {
          case Some(x) => {
            val addr = Address(
              x,
              getStr(line(hdr.indexOf("unit_type"))),
              getStr(line(hdr.indexOf("unit_value"))),
              getInt(line(hdr.indexOf("address_number"))),
              getStr(line(hdr.indexOf("address_number_suffix"))),
              getInt(line(hdr.indexOf("address_number_high"))),
              getStr(line(hdr.indexOf("road_name"))),
              getStr(line(hdr.indexOf("road_type_name"))),
              getStr(line(hdr.indexOf("road_suffix"))),
              getStr(line(hdr.indexOf("suburb_locality"))),
              getStr(line(hdr.indexOf("postcode"))),
              getStr(line(hdr.indexOf("town_city"))),
              getDouble(line(hdr.indexOf("lng"))),
              getDouble(line(hdr.indexOf("lat"))),
              getDouble(line(hdr.indexOf("x"))),
              getDouble(line(hdr.indexOf("y"))),
            )

            loop(it, accum :+ addr)
          }
          case None => loop(it, accum)
        }
      }
    }

    AddressDB(loop(it, Vector.empty))
  }

  def apply(gzinput: GZIPInputStream): AddressDB = apply(Source.fromInputStream(gzinput))
}
*/

/*
new GZIPInputStream(getClass.getResourceAsStream("/addresses.csv.gz"))
*/

/*
class AddressDB(gzinput: GZIPInputStream) {
  private val r = new scala.util.Random
  private val data: List[] = fromStream(gzinput)

  def this(inputstream: InputStream) {
    this(new GZIPInputStream(inputstream))
  }

  def this(inputstream: FileInputStream) {
    this(new GZIPInputStream(inputstream))
  }

  def this(dbname: String) {
    this(new FileInputStream(dbname))
  }

  private def fromStream(gzinput: GZIPInputStream): Vector[AddressComponents] = {
    val tmp: ArrayBuffer[(String, Double)] = new ArrayBuffer()
    Source.fromInputStream(gzinput).getLines.foreach((s: String) => {
      val line = s.split("\\s+")
      tmp.append((line(0).toUpperCase,line(1).toDouble))
    }) 
    val s = tmp.foldLeft(0.0)(_ + _._2)
    for (i <- 0 until tmp.length) tmp(i) = tmp(i).copy(_2 = tmp(i)._2 / s)
    tmp.toList
  }

  def apply(name: String) = {
    data.filter(_._1 == name)
  }

  def getByName(name: String) = {
    data.filter(_._1 == name)
  }

  def getData = {
    this.data
  }

  def length = {
    data.length
  }

  def bottom(n: Int) = {
    data.sortBy(_._2).take(n).map(_._1).toList
  }

  def top(n: Int) = {
    data.sortBy(_._2).takeRight(n).reverse.map(_._1).toList
  }

  def randomName = {
    new Name(data(r.nextInt(data.length - 1))._1)
  }

  def getRandom = {
    data(r.nextInt(data.length - 1))._1
  }
}

object AddressDB {
  implicit val apply: AddressDB = new AddressDB(new GZIPInputStream(getClass.getResourceAsStream("/addresses.csv.gz")))
}
*/
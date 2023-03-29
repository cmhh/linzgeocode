package org.cmhh.linzgeocode

import scala.concurrent.Await
import scala.concurrent.duration._

case object Suburbs {
  import stringmetric.compare

  private val data = Await.result(DAO2.suburbs(), Duration.Inf).toVector

  def find(suburb: String, f: (String, String) => Double): Vector[Option[String]] = {
    val s = suburb.toLowerCase()
    val scores = data.map(x => x.map(y => compare(y, s, f)).getOrElse(0.0))
    val mx = scores.max
    data.zip(scores).filter(_._2 == mx).map(_._1)
  }

  def find(town: String): Vector[Option[String]] = find(town, stringmetric.jaroWinkler())
}
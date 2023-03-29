package org.cmhh.linzgeocode

import scala.concurrent.Await
import scala.concurrent.duration._

case object Towns {
  import stringmetric.compare

  private lazy val data = Await.result(DAO2.towns(), Duration.Inf).toVector

  def find(town: String, f: (String, String) => Double): Vector[Option[String]] = {
    val t = town.toLowerCase()
    val scores = data.map(x => x.map(y => compare(y, t, f)).getOrElse(0.0))
    val mx = scores.max
    data.zip(scores).filter(_._2 == mx).map(_._1)
  }

  def find(town: String): Vector[Option[String]] = find(town, stringmetric.jaroWinkler())
}
package org.cmhh.linzgeocode

import scala.concurrent.Await
import scala.concurrent.duration._

case object Roads {
  import stringmetric.compare

  private val data: Vector[(Option[String], Option[String], Option[String])] = 
    Await.result(DAO2.roads(), Duration.Inf).toVector

  def find(
    roadName: Option[String], roadType: Option[String], roadSuffix: Option[String],
    f: (String, String) => Double
  ): Vector[(Option[String], Option[String], Option[String])] = {
    val x1 = roadName match {
      case None => data
      case Some(s) => 
        val s_ = s.toLowerCase()
        val scores = data.map(x => x._1.map(y => compare(y, s_, f)).getOrElse(0.0))
        val mx = scores.max
        data.zip(scores).filter(_._2 == mx).map(_._1)
    }

    val x2 = roadType match {
      case None => x1.map(x => (x._1, None, x._2)).distinct
      case Some(s) => 
        val s_ = s.toLowerCase()
        val scores = x1.map(x => x._2.map(y => compare(y, s_, f)).getOrElse(0.0))
        val mx = scores.max
        x1.zip(scores).filter(_._2 == mx).map(_._1)
    }

    roadSuffix match {
      case None => x2.map(x => (x._1, x._2, None)).distinct
      case Some(s) => 
        val s_ = s.toLowerCase()
        val scores = x2.map(x => x._3.map(y => compare(y, s_, f)).getOrElse(0.0))
        val mx = scores.max
        x2.zip(scores).filter(_._2 == mx).map(_._1)
    }    
  } 

  def find(
    roadName: Option[String], roadType: Option[String], roadSuffix: Option[String]
  ): Vector[(Option[String], Option[String], Option[String])] = 
    find(roadName, roadType, roadSuffix, stringmetric.jaroWinkler())
}
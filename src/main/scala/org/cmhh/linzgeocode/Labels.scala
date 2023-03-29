package org.cmhh.linzgeocode

object Labels {
  import org.cmhh.linzgeocode.utils

  def apply(x: String): Vector[Int] = onehot.getOrElse(x.toLowerCase, utils.zeros(names.size))

  val names: Vector[String] = Vector(
    "space", "separator", 
    "unit_type", "unit_value", "level_type", "level_value", 
    "address_number", "address_number_suffix", "address_number_high", 
    "road_name", "road_type_name", "road_suffix", 
    "suburb_locality", "postcode", "town_city"
  )

  val size = names.size

  val onehot: Map[String, Vector[Int]] = {
    def loop(d: Vector[String], accum: Map[String, Vector[Int]]): Map[String, Vector[Int]] = {
      if (d.size == 1) {
        accum + (d.head -> utils.onehot(d.head, names))
      } else {
        loop(d.tail,  accum + (d.head -> utils.onehot(d.head, names)))
      }
    }

    loop(names, Map.empty)
  }
}
package org.cmhh.linzgeocode

object Vocab {
  import org.cmhh.linzgeocode.utils

  val names: Vector[Char] = 
    Vector(' ', '\'', ',', '-', '/') ++ 
      {'0' to '9'}.toVector ++ {'a' to 'z'}.toVector ++ 
      Vector(257.toChar, 275.toChar, 299.toChar, 333.toChar, 363.toChar)

  val size = names.size

  def apply(x: Char): Vector[Int] = onehot.getOrElse(x.toLower, utils.zeros(names.size))

  val onehot: Map[Char, Vector[Int]] = {
    def loop(d: Vector[Char], accum: Map[Char, Vector[Int]]): Map[Char, Vector[Int]] = {
      if (d.size == 1) {
        accum + (d.head -> utils.onehot(d.head, names))
      } else {
        loop(d.tail,  accum + (d.head -> utils.onehot(d.head, names)))
      }
    }

    loop(names, Map.empty)
  }
}
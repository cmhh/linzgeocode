package org.cmhh.linzgeocode

object utils {
  def onehot[T](x: T, labels: Vector[T]): Vector[Int] = {
    val pos = labels.indexOf(x)

    if (pos < 0) {
      zeros(labels.size)
    } else {
      zeros(pos) ++ Vector(1) ++ zeros(labels.size - pos - 1)
    }
  }

  def zeros(n: Int): Vector[Int] = 
    if (n <= 0) {
      Vector.empty
    } else {
      {1 to n}.map(x => 0).toVector
    }
}
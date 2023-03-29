package org.cmhh.linzgeocode

import scala.collection.mutable.ArrayBuffer

object stringmetric {
  /**
   * Calculate similarity between 2 strings.
   * 
   * @param s1 a string
   * @param s2 a string
   * @param f a string metric function
   * @return a value ideally in the range [0, 1], with 1 indicating a perfect match
   * 
   * {{{
   * compare("SHACKLEFORD", "SHACKELFORD", org.cmhh.linzgeocode.stringmetric.jaro)
   * }}}
   */
  def compare(s1: String, s2: String, f: (String, String) => Double) = f(s1, s2)
  
  /**
   * Calculate similarity between 2 string sequences.
   * 
   * @param s1 a string sequence
   * @param s2 a string sequence
   * @param f a string metric function
   * @return a value ideally in the range [0, 1], with 1 indicating a perfect match
   * 
   * {{{
   * // returns 1.0
   * compare(
   *   Seq("Joseph", "Blogs"), 
   *   Seq("Joseph", "Michael", "Jordan", "Blogs"),
   *   org.cmhh.linzgeocode.stringmetric.jarowinkler()
   * )
   * }}}
   */
  def compare(s1: Seq[String], s2: Seq[String], f: (String, String) => Double) = {
    val shortest = if (s1.length < s2.length) s1 else s2
    val longest = if (s1.length >= s2.length) s1 else s2
    val scores = shortest.map(s => longest.map(t => f(s, t)).max)
    scores.sum.toDouble / scores.size.toDouble
  }
  
  /**
   * Calculate similarity between 2 strings containing one or more tokens.
   * 
   * Strings are split into arrays using whitespace sequences (i.e. using regex '\s+').
   * 
   * @param s1 a string
   * @param s2 a string
   * @param f a string metric function
   * @return a value ideally in the range [0, 1], with 1 indicating a perfect match
   * 
   * {{{
   * compareMult(
   *   "JOSEPH BLOGS",
   *   "JOSEPH MICHAEL JORDAN BLOGS",
   *   nz.govt.stats.StrinMetrics.functions.jarowinkler
   * )
   * }}}
   */
  def compareMult(s1: String, s2: String, f: (String, String) => Double) = 
    compare(s1.split("\\s+").toVector, s2.split("\\s+").toVector, f)

  /**
   * Calculate Jaro-Winkler similarity.
   * 
   * @param s1 a string
   * @param s2 a string
   * @param p scaling factor
   * @param b threshold--return jaro distince if below b
   * 
   * @return a value ideally in the range [0, 1], with 1 indicating a perfect match
   */
  def jaroWinkler(p: Double = 0.1, b: Double = 0.0)(s1: String, s2: String): Double = {
    val l1 = s1.length
    val l2 = s2.length
    var i = 0
    var j = 0
    var m = 0
    val w = math.max(0, (math.max(l1.toDouble, l2.toDouble)/2).floor - 1)
    val p1 = ArrayBuffer.fill(l1)(0)
    val p2 = ArrayBuffer.fill(l2)(0)
    for (i <- 0 until l2){
      val lo = math.max(0, i - w).toInt
      val hi = math.min(l1, i + w + 1).toInt
      j = lo
      var done = false
      while(j < hi & !done){
        if (p1(j)==0 && s2(i) == s1(j)){
          p1(j) = 1
          p2(i) = 1
          m += 1
          done = true
        }
        j += 1
      }
    }
    if (m == 0) 0d
    else{
      var k = 0
      var t: Int = 0
      for(i <- 0 until l2){
        if (p2(i)==1){
          j = k
          var done = false
          while (j < l1 & !done){
            if (p1(j)==1){
              k = j + 1
              done = true
            }
            if (!done) j += 1
          }
          j = math.min(j, l1 - 1)
          if (s2(i) != s1(j)) t += 1
        }
      }
      t = t / 2
      val d = (m.toDouble / l1.toDouble + m.toDouble / l2.toDouble + (m.toDouble - t.toDouble)/m.toDouble) / 3d
      val lo = 0
      val hi = math.min(4, math.min(l1, l2))
      var l = 0
      i = 0
      var done = false
      while(i < hi & !done){
        if (s1(i) == s2(i)) l += 1
        else done = true
        if (!done) i += 1
      }
      if (d < b) d
      else d + l.toDouble * p * (1d - d)
    }
  }

  /**
   * Calculate Jaro similarity.
   * 
   * @param s1 a string
   * @param s2 a string
   * 
   * @return a value ideally in the range [0, 1], with 1 indicating a perfect match
   */
  def jaro(s1: String, s2: String): Double = jaroWinkler(0.0, 0.0)(s1, s2)

  private def isSubstring(s1: String, s2: String): Boolean = {
    val n = List(s1.size, s2.size).min
    s1.take(n) == s2.take(n)
  }
}
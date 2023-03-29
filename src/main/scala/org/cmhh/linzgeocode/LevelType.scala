package org.cmhh.linzgeocode

object leveltype {
  sealed trait LevelType
  case object GROUND extends LevelType { override val toString = "Ground" }
  case object LEVEL extends LevelType { override val toString = "Level" }
  case object LOWERGROUND extends LevelType { override val toString = "Lower Ground" }
}
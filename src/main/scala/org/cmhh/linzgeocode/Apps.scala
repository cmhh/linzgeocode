package org.cmhh.linzgeocode

import scala.util.{Try, Success, Failure}

object AddressParse extends App {
  parse.parse(args(0))(parse.m) match {
    case Success(addr) => println(addr.toJson)
    case Failure(addr) => ;
  }
}
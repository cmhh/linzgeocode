package org.cmhh.linzgeocode

import slick.ast._
import slick.jdbc.PostgresProfile.api._

object dbextensions {
  implicit class RepExtensions[T](val rep: Rep[Option[T]]) {

    def is(targetValue: Option[T])(implicit slickRecognizedType: BaseTypedType[T]): Rep[Option[Boolean]] =
      targetValue match {
        case Some(value) => rep === value
        case None        => rep.isEmpty.?
      }

    def isUnlessNull(targetValue: Option[T])(implicit slickRecognizedType: BaseTypedType[T]): Rep[Option[Boolean]] =
      targetValue match {
        case Some(value) => rep === value
        case None        => Some(true)
      }
  }
}
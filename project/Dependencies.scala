import sbt._

object Dependencies {
  lazy val pg = "org.postgresql" % "postgresql" % "42.5.3"
  lazy val slick = "com.typesafe.slick" %% "slick" % "3.4.1"
  lazy val hikaricp = "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1"
  lazy val conf = "com.typesafe" % "config" % "1.4.2"
  lazy val slf4j = "org.slf4j" % "slf4j-nop" % "2.0.6" 
  lazy val akkaactor = "com.typesafe.akka" %% "akka-actor" % "2.7.0"
  lazy val akkastream = "com.typesafe.akka" %% "akka-stream" % "2.7.0"
  lazy val akkatest = "com.typesafe.akka" %% "akka-testkit" % "2.7.0" 
  lazy val akkahttp = "com.typesafe.akka" %% "akka-http" % "10.4.0"
  lazy val sprayjson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.4.0"
  lazy val scalatest = "org.scalatest" % "scalatest_2.13" % "3.2.15" 
  lazy val dl4jcore = "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-M1.1"
  lazy val nd4j = "org.nd4j" % "nd4j-native-platform" % "1.0.0-M1.1"
}
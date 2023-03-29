package org.cmhh.linzgeocode

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.{ Route, Directive0 }
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import scala.io.StdIn
import scala.util.{ Try, Success, Failure }
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._

/**
 * CORS handler... just in case.
 */
trait CORSHandler{
  private val corsResponseHeaders = List(
    headers.`Access-Control-Allow-Origin`.*,
    headers.`Access-Control-Allow-Credentials`(true),
    headers.`Access-Control-Allow-Headers`(
      "Authorization", "Content-Type", "X-Requested-With"
    )
  )
  
  private def addAccessControlHeaders: Directive0 = {
    respondWithHeaders(corsResponseHeaders)
  }
  
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK).
      withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
  }
  
  def corsHandler(r: Route): Route = addAccessControlHeaders {
    preflightRequestHandler ~ r
  }
  
  def addCORSHeaders(response: HttpResponse):HttpResponse =
    response.withHeaders(corsResponseHeaders)
}

object Service extends App with CORSHandler {
  import org.cmhh.linzgeocode._
  
  implicit val system = ActorSystem("linzgeocode")
  implicit val executionContext = system.dispatcher
  implicit val m = parse.m
  val conf = ConfigFactory.load()

  /*
  final case class ParseFailedException(message: String, cause: Throwable) extends Exception(message, cause)

  implicit def exceptionsHandler: ExceptionHandler = ExceptionHandler {
    case e: ParseFailedException =>
      complete(HttpResponse(StatusCodes.BadRequest, entity = e.cause.getStackTrace.mkString("\n")))
  }
  */

  object helpers {
    def parseaddr(text: String) = {
      parse.parse(text) match {
        case Failure(e) =>
          val r = HttpEntity(
            ContentTypes.`application/json`,
            s"""{"search_text":"$text", "status":"Failed to parse address."}"""
          )
          complete(StatusCodes.BadRequest -> r)
        case Success(components) =>
          complete(HttpEntity(
              ContentTypes.`application/json`, components.toJson
          ))
      }
    }

    def search(text: String, detailed: Option[Boolean]) = {
      parse.parse(text) match {
        case Failure(e) =>
          val r = HttpEntity(
            ContentTypes.`application/json`,
            s"""{"search_text":"$text", "status":"Failed to parse address."}"""
          )
          complete(StatusCodes.BadRequest -> r)
        case Success(components) => {
          val results = conf.getBoolean("data.hasPostcode") match {
            case true =>
              DAO1.searchAndFormat(
                components.repair, conf.getInt("search.maxResults"), detailed.getOrElse(false)
              )
            case false =>
              DAO2.searchAndFormat(
                components.repair, conf.getInt("search.maxResults"), detailed.getOrElse(false)
              )
          }

          onComplete(results) {
            case Success(r) =>                
              complete(HttpEntity(
                ContentTypes.`application/json`,
                r
              ))
            case Failure(e) => 
              val r = HttpEntity(
                ContentTypes.`application/json`,
                s"""{"search_text":"$text", "status":"${e.getMessage()}"}"""
              )
              complete(StatusCodes.InternalServerError -> r)
          }
        }
      }
    }
  }

  object routes {
    val version = path("version") {
      complete(HttpEntity(
        ContentTypes.`application/json`, 
        s"""["${conf.getString("app.version")}"]"""
      ))
    }

    val parse_get = path("parse") {
      parameters(Symbol("text")){ (text) => 
        helpers.parseaddr(text)
      }
    }

    val parse_post = path("parse") {
      entity(as[String]) { entity =>
        helpers.parseaddr(entity)
      }
    }

    val geocode_get = path("geocode") {
      parameters(
        Symbol("text"), 
        Symbol("detailed").as[Boolean].?
      ){ (text, detailed) =>
        helpers.search(text, detailed)
      }
    }

    val geocode_post = path("geocode") {
      entity(as[String]) { entity =>
        case class params(text: String, detailed: Option[Boolean])
        implicit val paramsfmt: JsonFormat[params] = jsonFormat2(params)
        val p = entity.parseJson.convertTo[params]
        helpers.search(p.text, p.detailed)
      }
    }
  }

  val route = 
    pathPrefix(conf.getString("app.prefix")) { 
      corsHandler(
        get {
          routes.version ~ routes.parse_get ~ routes.geocode_get
        } ~ 
        post {
          routes.parse_post ~ routes.geocode_post
        }
      )
    }

  val bindingFuture = Http().newServerAt("0.0.0.0", conf.getInt("app.port")).bindFlow(route)
  val url = s"""http://localhost:${conf.getString("app.port")}/${conf.getString("app.prefix")}"""

  println(s"Server online at $url\nPress ENTER to stop...")
  StdIn.readLine() 

  bindingFuture
    .flatMap(_.unbind()) 
    .onComplete(_ => system.terminate()) 
}
package sanskrit_coders.vedavaapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, Uri, headers}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success}
//import akka.http.scaladsl.model._
//import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import akka.http.scaladsl.model.headers.HttpCookie
import akka.stream.ActorMaterializer

class UllekhanamClient {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)
  implicit val system = ActorSystem("my-Actor")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val httpClient = Http().outgoingConnection(host = "api.vedavaapi.org")
  var sessionCookieHeaderOpt: Option[HttpHeader] = None

  def passwordLogin() = {
    // Set sessionCookieHeaderOpt here.
  }

  def callApi(uri: String) : Unit = {
    // Tips: https://stackoverflow.com/questions/38792846/akka-http-client-set-cookie-on-a-httprequest
    // val request = HttpRequest(uri = Uri(uri), headers = sessionCookieHeaderOpt.map(List(_)).getOrElse(List()))
    val request = HttpRequest(uri = Uri(uri))
    log.debug(request.toString())
    val flow = Source.single(request)
      .via(httpClient)
//      .mapAsync(1)(r => Unmarshal(r.entity).to[List[Post]])
      .runWith(Sink.head)

    flow.andThen {
      case Success(list) => println(s"request succeded ${list}")
      case Failure(_) => println("request failed")
    }.andThen {
      case _ => system.terminate()
    }
  }

}

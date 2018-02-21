package sanskrit_coders.vedavaapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import org.slf4j.{Logger, LoggerFactory}
import sanskrit_coders.RichHttpClient

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
//import akka.http.scaladsl.model._
//import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import akka.http.scaladsl.model.headers.HttpCookie
import akka.stream.ActorMaterializer

class UllekhanamClient {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)
  implicit val system: ActorSystem = ActorSystem("my-Actor")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val simpleClient: HttpRequest => Future[HttpResponse] = Http(system).singleRequest(_: HttpRequest)
  private val redirectingClient: HttpRequest => Future[HttpResponse] = RichHttpClient.httpClientWithRedirect(simpleClient)
  var sessionCookieHeaderOpt: Option[HttpHeader] = None

  def passwordLogin(): Unit = {
    // Set sessionCookieHeaderOpt here.
  }

  def callApi(uri: String) : Future[String] = {
    // Tips: https://stackoverflow.com/questions/38792846/akka-http-client-set-cookie-on-a-httprequest
    // val request = HttpRequest(uri = Uri(uri), headers = sessionCookieHeaderOpt.map(List(_)).getOrElse(List()))
    // See https://doc.akka.io/docs/akka/2.5.3/scala/stream/stream-flows-and-basics.html for a good intro to akka streams.
    val request = HttpRequest(uri = Uri(uri))
    log.debug(request.toString())
    RichHttpClient.httpResponseToString(redirectingClient(HttpRequest(uri = uri)))
  }

}

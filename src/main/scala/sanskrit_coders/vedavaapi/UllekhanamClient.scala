package sanskrit_coders.vedavaapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import org.slf4j.{Logger, LoggerFactory}
import sanskrit_coders.RichHttpClient

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import akka.http.scaladsl.model._
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

import scala.io.BufferedSource

//import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import akka.http.scaladsl.model.headers.HttpCookie
import akka.stream.ActorMaterializer

case class ServerConfig(base_uri: Option[String], user_id: Option[String], user_secret: Option[String])

class UllekhanamClient {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)
  implicit val system: ActorSystem = ActorSystem("my-Actor")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val simpleClient: HttpRequest => Future[HttpResponse] = Http(system).singleRequest(_: HttpRequest)
  private val redirectingClient: HttpRequest => Future[HttpResponse] = RichHttpClient.httpClientWithRedirect(simpleClient)
  var sessionCookieHeaderOpt: Option[HttpHeader] = None

  // For parsing serverConfigSource
  private implicit val formats: DefaultFormats.type = DefaultFormats
  private val serverConfigSource: BufferedSource = scala.io.Source.fromResource("server_config_local.json")
  private val serverConfig: ServerConfig = Serialization.read[ServerConfig](serverConfigSource.mkString)



  def passwordLogin(): Unit = {
    val uri = serverConfig.base_uri.get + "/auth/v1/password_login"
    val formData = FormData(("user_id", serverConfig.user_id.get), ("user_secret", serverConfig.user_secret.get))
    val request = HttpRequest(method = HttpMethods.POST, uri = Uri(uri), entity = formData.toEntity)

    log.debug(request.toString())
    val responseFuture = redirectingClient(HttpRequest(uri = uri))
    responseFuture.foreach(response => {
      log.debug(response.toString())
      log.debug(response.toString())
    })
    // Set sessionCookieHeaderOpt here.
  }

  def callApi(uriSuffix: String) : Future[String] = {
    // Tips: https://stackoverflow.com/questions/38792846/akka-http-client-set-cookie-on-a-httprequest
    // val request = HttpRequest(uri = Uri(uri), headers = sessionCookieHeaderOpt.map(List(_)).getOrElse(List()))
    // See https://doc.akka.io/docs/akka/2.5.3/scala/stream/stream-flows-and-basics.html for a good intro to akka streams.
    val request = HttpRequest(uri = Uri(serverConfig.base_uri.get + uriSuffix))

    log.debug(request.toString())
    RichHttpClient.httpResponseToString(redirectingClient(HttpRequest(uri = uriSuffix)))
  }

}

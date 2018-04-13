package sanskrit_coders.vedavaapi


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import org.slf4j.{Logger, LoggerFactory}
import sanskrit_coders.RichHttpClient

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.http.scaladsl.model._
// For the Domain specific language (DSL) used in parsing headers.
import akka.http.scaladsl.model.headers._
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

import scala.io.BufferedSource

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



  def passwordLogin(): Future[Seq[HttpCookie]] = {
    val finalUrl = serverConfig.base_uri.get + "/auth/v1/password_login"
    val formData = FormData(("user_id", serverConfig.user_id.get), ("user_secret", serverConfig.user_secret.get))
    val request = HttpRequest(method = HttpMethods.POST, uri = Uri(finalUrl), entity = formData.toEntity)

    log.debug(request.toString())
    val responseFuture = redirectingClient(request)
    responseFuture.map(response => {
      // log.debug(response.toString())
      val cookies: Seq[HttpCookie] = response.headers.collect {
        case `Set-Cookie`(cookie) => cookie
      }
      // log.debug("Cookies: " + cookies)
      cookies
    })
  }

  def callApi(uriSuffix: String) : Future[String] = {
    // Tips: https://stackoverflow.com/questions/38792846/akka-http-client-set-cookie-on-a-httprequest
    // val request = HttpRequest(uri = Uri(uri), headers = sessionCookieHeaderOpt.map(List(_)).getOrElse(List()))
    // See https://doc.akka.io/docs/akka/2.5.3/scala/stream/stream-flows-and-basics.html for a good intro to akka streams.
    val finalUrl = serverConfig.base_uri.get + "/" + uriSuffix
    val request = HttpRequest(uri = Uri(finalUrl))

    log.debug(request.toString())
    RichHttpClient.httpResponseToString(redirectingClient(request))
  }

}

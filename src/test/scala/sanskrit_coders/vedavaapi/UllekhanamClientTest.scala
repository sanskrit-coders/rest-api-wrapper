package sanskrit_coders.vedavaapi

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.FlatSpec
import org.slf4j.{Logger, LoggerFactory}
//import akka.http.scaladsl.model._
//import akka.http.scaladsl.unmarshalling.Unmarshal

class UllekhanamClientTest  extends FlatSpec {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)
  private val client = new UllekhanamClient()
  "callApi" should "work with listBooks API" in {
    val resultFuture = client.callApi(uri = "http://api.vedavaapi.org/py/ullekhanam/v1/dbs/ullekhanam_test/books")
    resultFuture.map(responseString => {
        log.debug(responseString)
      })
  }
}

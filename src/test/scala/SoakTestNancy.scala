
import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import net.liftweb.json.{DefaultFormats, parse}

import scala.concurrent.duration._
import scalaj.http.Http

class SoakTestNancy extends Simulation {

  val baseUrl = ConfigFactory.load().getString("baseURL")
  val countUser = ConfigFactory.load().getInt("countUserNancy")
  val soakTestTime= ConfigFactory.load().getInt("soakTestTimeInMinutes")
  val stressTestTime = ConfigFactory.load().getInt("stressTestTimeInMinutes")
  val influxDbURL = ConfigFactory.load().getString("influxDbURL")
  val countUserMaximum = ConfigFactory.load().getInt("countUserMaximum")


  val httpProtocol = http
    .baseURL(baseUrl)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .contentTypeHeader("text/plain")
    .userAgentHeader("PostmanRuntime/7.1.1")


  val uri1 = baseUrl +"/TestNancyModule/Example"

  val scn = scenario("SoakTestNancy")
    .exec(http("TestNancyModule/Example")
      .post("/TestNancyModule/Example")
      .body(RawFileBody("SoakTest_0000_request.txt")))

  setUp(scn.inject(constantUsersPerSec(countUser) during(soakTestTime minutes)).protocols(httpProtocol))

  after {
    if (!influxDbURL.equals("")) {
      getMemory()
    }
    println("Base URL "+baseUrl)
  }

  def getMemory() {
    implicit val formats = DefaultFormats
    var url = influxDbURL+"/query?q=SELECT+max(%22used_percent%22)+FROM+%22mem%22+Where+time+%3E%3D+now()+-+"+soakTestTime+"m&db=telegraf"
    val memoryPersentMax = Http(url).asString
    url = influxDbURL+"/query?q=SELECT+min(%22used_percent%22)+FROM+%22mem%22+Where+time+%3E%3D+now()+-+"+soakTestTime+"m&db=telegraf"
    val memoryPersentMin = Http(url).asString
    url = influxDbURL+"/query?q=SELECT+mean(%22used_percent%22)+FROM+%22mem%22+Where+time+%3E%3D+now()+-+"+soakTestTime+"m&db=telegraf"
    val memoryPersentAvg = Http(url).asString



    val max = parse(memoryPersentMax.body).extract[InfluxResponse]
    val min = parse(memoryPersentMin.body).extract[InfluxResponse]
    val avg = parse(memoryPersentAvg.body).extract[InfluxResponse]

    println("******Memory******")
    println("Memory MAX % "+max.results(0).series(0).values(0).get(1))
    println("Memory MIN % "+min.results(0).series(0).values(0).get(1))
    println("Memory AVG % "+avg.results(0).series(0).values(0).get(1))
    println("******************")

  }

}
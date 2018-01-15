
import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class StressTest extends Simulation {

	val baseUrl = ConfigFactory.load().getString("baseURL")
	val countUser = ConfigFactory.load().getInt("countUser")
	val soakTestTime = ConfigFactory.load().getInt("soakTestTime")
	val stressTestTime = ConfigFactory.load().getInt("stressTestTime")

	val httpProtocol = http
		.baseURL(baseUrl)
		.inferHtmlResources()
		.acceptHeader("*/*")
		.contentTypeHeader("text/plain")
		.userAgentHeader("PostmanRuntime/7.1.1")

	val headers_0 = Map(
		"Postman-Token" -> "f953fc8c-c9e1-4b2c-a9c5-1a79f886f391",
		"accept-encoding" -> "gzip, deflate",
		"cache-control" -> "no-cache",
		"content-length" -> "70",
		"cookie" -> "__NCTRACE=a882e5db-f7f6-46f9-9f6f-cf356db2f603")

    val uri1 = baseUrl +"/TestMicroservice/Example"

	val scn = scenario("StressTest")
		.exec(http("TestMicroservice/Example")
			.post("/TestMicroservice/Example")
			.headers(headers_0)
			.body(RawFileBody("SoakTest_0000_request.txt")))


	setUp(scn.inject(rampUsersPerSec(1) to countUser*2 during(stressTestTime minutes)).protocols(httpProtocol))

}
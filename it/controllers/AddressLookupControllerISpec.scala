package controllers

import java.util.UUID

import itutil.IntegrationSpecBase
import model.{ConfirmableAddress, ConfirmableAddressDetails, JourneyConfig, JourneyData}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeApplication
import uk.gov.hmrc.address.v2.Country

/**
  * Created by daveg on 25/10/18.
  */
class AddressLookupControllerISpec extends IntegrationSpecBase {
  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  val testConfig = JourneyData(JourneyConfig(continueUrl="A url"))
  val csrfToken = () => UUID.randomUUID().toString

  "The lookup page" should {
    "pre-pop the postcode and filter on the view when they are passed in as query parameters " in {

      stubKeystore("Jid123", Json.toJson(testConfig).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/lookup?postcode=AB11+1AB&filter=bar").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("postcode").`val` shouldBe "AB11 1AB"
      doc.getElementById("filter").`val` shouldBe "bar"

    }

    "pre-pop the postcode only on the view when it is passed in as a query parameters " in {

      stubKeystore("Jid123", Json.toJson(testConfig).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/lookup?postcode=AB11 1AB").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("postcode").`val` shouldBe "AB11 1AB"
      doc.getElementById("filter").`val` shouldBe ""

    }

    "pre-pop the filter only on the view when it is passed in as a query parameters " in {

      stubKeystore("Jid123", Json.toJson(testConfig).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/lookup?filter=bar").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("postcode").`val` shouldBe ""
      doc.getElementById("filter").`val` shouldBe "bar"

    }


    "not pre-pop the filter or postcode fields when no query parameters are used " in {

      stubKeystore("Jid123", Json.toJson(testConfig).as[JsObject], 200)
      val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
      val fResponse = buildClient("/lookup").
        withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
        get()
      val res = await(fResponse)
      val doc = Jsoup.parse(res.body)
      res.status shouldBe 200
      doc.getElementById("postcode").`val` shouldBe ""
      doc.getElementById("filter").`val` shouldBe ""

    }

    "The edit page" should {
      "redirect to the UK edit page if country exists and is UK AND UK mode is false " in {
        val testConfigWithAddressAndNotUkMode = testConfig.copy(
          selectedAddress = Some(
            ConfirmableAddress("foo", Some("bar"),ConfirmableAddressDetails(Some(List("wizz","bang")),Some("fooP"),Some(Country("GB", "United Kingdom"))))
          ), config = testConfig.config.copy(ukMode = Some(false))
        )

        stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndNotUkMode).as[JsObject], 200)
        val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
        val fResponse = buildClient("/edit").
          withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
          get()
        val res = await(fResponse)
        val doc = Jsoup.parse(res.body)
        res.status shouldBe 200
        doc.getElementById("ukModeBack").html shouldBe "Back"
      }

      "redirect to the UK edit page with the lookup postcode if country exists and is UK AND UK mode is false " in {
        val testConfigWithAddressAndNotUkMode = testConfig.copy(
          selectedAddress = None, config = testConfig.config.copy(ukMode = Some(false))
        )

        stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndNotUkMode).as[JsObject], 200)
        val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
        val fResponse = buildClient("/edit?lookUpPostCode=ZZ1+++1ZZ").
          withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
          get()
        val res = await(fResponse)
        val doc = Jsoup.parse(res.body)
        res.status shouldBe 200
        doc.getElementById("nonUkModeBack").html shouldBe "Back"
        doc.getElementById("postcode").`val` shouldBe "ZZ1 1ZZ"
      }

      "redirect to the UK edit page with no pre-popped postcode if lookup postcode doesn't exist AND UK mode is false " in {
              val testConfigWithAddressAndNotUkMode = testConfig.copy(
                selectedAddress = None, config = testConfig.config.copy(ukMode = Some(false))
              )

              stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndNotUkMode).as[JsObject], 200)
              val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
              val fResponse = buildClient("/edit").
                withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
                get()
              val res = await(fResponse)
              val doc = Jsoup.parse(res.body)
              res.status shouldBe 200
              doc.getElementById("nonUkModeBack").html shouldBe "Back"
              doc.getElementById("postcode").`val` shouldBe ""
            }

      "redirect to the UK edit page if country exists and is UK AND UK mode is true " in {

        val testConfigWithAddressAndUkMode = testConfig.copy(
          selectedAddress = Some(
            ConfirmableAddress("foo", Some("bar"),ConfirmableAddressDetails(Some(List("wizz","bang")),Some("fooP"),Some(Country("GB", "United Kingdom"))))
          ), config = testConfig.config.copy(ukMode = Some(true))
        )
        stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndUkMode).as[JsObject], 200)
        val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
        val fResponse = buildClient("/edit").
          withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
          get()
        val res = await(fResponse)
        val doc = Jsoup.parse(res.body)
        res.status shouldBe 200
        doc.getElementById("ukModeBack").html shouldBe "Back"
        }

      "redirect to the UK edit page if country doesn't exist and is UK AND UK mode is true " in {
        val testConfigWithAddressAndUkMode = testConfig.copy(
          selectedAddress = Some(
            ConfirmableAddress("foo", Some("bar"),ConfirmableAddressDetails(Some(List("wizz","bang")),Some("fooP")))
          ), config = testConfig.config.copy(ukMode = Some(true))
        )
        stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndUkMode).as[JsObject], 200)
        val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
        val fResponse = buildClient("/edit").
          withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
          get()
        val res = await(fResponse)
        val doc = Jsoup.parse(res.body)
        res.status shouldBe 200
        doc.getElementById("ukModeBack").html shouldBe "Back"
      }

      "redirect to the International edit page if Uk mode is false and country is not UK " in {
        val testConfigWithAddressAndNotUkMode = testConfig.copy(
          selectedAddress = Some(
            ConfirmableAddress("foo", Some("bar"),ConfirmableAddressDetails(Some(List("wizz","bang")),Some("fooP"),Some(Country("FR", "France"))))
          ), config = testConfig.config.copy(ukMode = Some(false))
        )
        stubKeystore("Jid123", Json.toJson(testConfigWithAddressAndNotUkMode).as[JsObject], 200)
        val sessionCookie = getSessionCookie(Map("csrfToken" -> csrfToken()))
        val fResponse = buildClient("/edit").
          withHeaders(HeaderNames.COOKIE -> sessionCookie, "Csrf-Token" -> "nocheck").
          get()
        val res = await(fResponse)
        val doc = Jsoup.parse(res.body)
        res.status shouldBe 200
        doc.getElementById("nonUkModeBack").html shouldBe "Back"


      }
    }
  }
}
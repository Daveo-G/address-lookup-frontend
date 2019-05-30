package controllers

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.test.FakeApplication


class AddressLookupConfirmPageISpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  "The confirm page" should {
    "pre-pop with an address and all elements are correct for an empty journey config model" in {
      stubKeystore(testJourneyId, testConfigWithAddressNotUkModeAsJson, OK)

      val fResponse = buildClientLookupAddress(path = "confirm")
        .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
        .get()
      val res = await(fResponse)
      val doc = getDocFromResponse(fResponse)
      doc.getElementById("pageHeading").text() shouldBe "Review and confirm"
      doc.getElementById("changeLink").text() shouldBe "Edit this address"
      doc.getElementById("continue").text() shouldBe "Confirm and continue"
      doc.getElementById("line1").text() shouldBe "1 High Street"
      doc.getElementById("line2").text() shouldBe "Telford"
      doc.getElementById("postCode").text() shouldBe "AB11 1AB"
      doc.getElementById("country").text() shouldBe "France"
      doc.getElementsByClass("back-link").first().text() shouldBe "Back"
      doc.getElementsByTag("title").first().text() shouldBe "Confirm the address"
      doc.getElementsByTag("h2").select(":containsOwn(Your selected address)").size() shouldBe 0
      doc.getElementsByTag("p").select(":containsOwn(This is how your address will look. Please double-check it and, if accurate, click on the Confirm button.)").size() shouldBe 0
      testElementDoesntExist(res,"searchAgainLink")
      testElementDoesntExist(res,"confirmChangeText")

      res.status shouldBe OK
    }
  }
}
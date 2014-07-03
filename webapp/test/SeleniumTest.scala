import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import org.fluentlenium.core.filter.FilterConstructor._

@RunWith(classOf[JUnitRunner])
class SeleniumTest extends Specification {

  "Application" should {

    "work from within a browser" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/");

        browser.$(".headerwrapcontent h1").getTexts().get(0) must equalTo("Start vanaf vandaag met autodelen!");

        browser.goTo("http://localhost:3333/logout");
        browser.goTo("http://localhost:3333/login");
        browser.$("#email").text("hannesbelen@gmail.com");
        browser.$("#password").text("opensesame");
        browser.click("#login");

        browser.$(".navbar-brand").getText() must equalTo("Dégage");

      }
    }

  }

}
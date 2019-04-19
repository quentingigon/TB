import controllers.ScreenController;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;

import static org.assertj.core.api.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;

public class ScreenUnitTest {

	@Test
	public void testAuthScreenKnown() {
		final ScreenController controller = new ScreenController();
		final String mac = "1234";
		Http.RequestBuilder request = fakeRequest("GET", "/screens/auth?mac=" + mac);
		Http.RequestBuilder tokenRequest = play.api.test.CSRFTokenHelper.addCSRFToken(request);
		Result result = controller.authentification(tokenRequest.build());
		assertThat(contentAsString(result)).contains("Ecrans");
	}

	@Test
	public void testAuthScreenUnknown() {
		final ScreenController controller = new ScreenController();
		final String mac = "1235";
		Http.RequestBuilder request = fakeRequest("GET", "/screens/auth?mac=" + mac);
		Http.RequestBuilder tokenRequest = play.api.test.CSRFTokenHelper.addCSRFToken(request);
		Result result = controller.authentification(
			tokenRequest.build());
		assertThat(contentAsString(result)).contains("Screen not yet registered");
	}
}

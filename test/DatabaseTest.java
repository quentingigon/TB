import controllers.UserController;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.Application;
import play.db.Database;
import play.db.Databases;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;

public class DatabaseTest {

	@Inject
	UserController userController;

	@Mock
	private Http.Request request;

	public static Application fakeApp;

	Database database;

	@BeforeClass
	public static void startApp() {
		fakeApp = Helpers.fakeApplication();
		Helpers.start(fakeApp);
	}

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		database = Databases.createFrom("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/test_tb");


	}

	@Test
	public void testUserCreated() {
		Http.Request mockRequest = mock(Http.Request.class);
		Result result = userController.register(mockRequest);
	}


	@After
	public void shutdownDatabase() {
		database.shutdown();
	}


}

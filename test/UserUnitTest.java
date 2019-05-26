import models.db.Admin;
import models.db.TeamMember;
import models.db.User;
import models.entities.UserData;
import models.repositories.interfaces.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import services.UserService;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserUnitTest {

	@Mock
	private UserRepository mockUserRepository;

	private int teamId;
	private int userId;
	private String email;
	private String password;
	private User userToReturn;
	private TeamMember memberToReturn;
	private Admin adminToReturn;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		teamId = 1;
		userId = 42;
		email = "test";
		password = "123";
		userToReturn = new User(email, password);
		memberToReturn = new TeamMember(userToReturn);
		memberToReturn.setUserId(userId);
		adminToReturn = new Admin(userId);
	}

	// Creation
	@Test
	public void testCreateUser() {
		when(mockUserRepository.create(any(User.class))).thenReturn(userToReturn);
		UserService service = new UserService(mockUserRepository);

		User newUser = new User(email, password);

		assertEquals(userToReturn.getEmail(), service.createUser(newUser).getEmail());
	}

	@Test
	public void testCreateMember() {
		TeamMember memberToReturn = new TeamMember(email, password);
		memberToReturn.setTeamId(teamId);
		when(mockUserRepository.createMember(any(TeamMember.class))).thenReturn(memberToReturn);
		UserService service = new UserService(mockUserRepository);

		TeamMember newMember = new TeamMember(email, password);

		assertEquals(memberToReturn.getTeamId(), service.createTeamMember(newMember).getTeamId());
	}

	@Test
	public void testCreateAdmin() {
		int userId = 2;
		Admin adminToReturn = new Admin(userId);
		when(mockUserRepository.createAdmin(any(Admin.class))).thenReturn(adminToReturn);
		UserService service = new UserService(mockUserRepository);

		Admin newAdmin = new Admin(userId);

		assertEquals(adminToReturn.getUserId(), service.createAdmin(newAdmin).getUserId());
	}

	// Update
	@Test
	public void testUpdateUser() {
		when(mockUserRepository.update(any(User.class))).thenReturn(userToReturn);
		UserService service = new UserService(mockUserRepository);

		User newUser = new User(email, password);

		assertEquals(userToReturn.getEmail(), service.updateUser(newUser).getEmail());
	}

	// Getters

	@Test
	public void testGetUserByEmailAndPassword() {
		when(mockUserRepository.get(email, password)).thenReturn(userToReturn);
		UserService service = new UserService(mockUserRepository);

		assertEquals(email, service.getUserByEmailAndPassword(email, password).getEmail());
	}

	@Test
	public void testGetUserByEmailAndPasswordFail() {
		when(mockUserRepository.get(email, password)).thenReturn(userToReturn);
		UserService service = new UserService(mockUserRepository);

		// same with wrong email
		assertNull(service.getUserByEmailAndPassword(email, "wrong password"));
	}

	@Test
	public void testGetUserByEmail() {
		when(mockUserRepository.getByEmail(email)).thenReturn(userToReturn);
		UserService service = new UserService(mockUserRepository);

		assertEquals(email, service.getUserByEmail(email).getEmail());
	}

	@Test
	public void testGetUserByEmailFail() {
		when(mockUserRepository.getByEmail(email)).thenReturn(userToReturn);
		UserService service = new UserService(mockUserRepository);

		assertNull(service.getUserByEmail("wrong email"));
	}

	@Test
	public void testGetMemberByEmail() {
		when(mockUserRepository.getMemberByUserEmail(email)).thenReturn(memberToReturn);
		UserService service = new UserService(mockUserRepository);

		assertEquals(userId, (int) service.getMemberByUserEmail(email).getUserId());
	}

	@Test
	public void testGetMemberByEmailFail() {
		when(mockUserRepository.getMemberByUserEmail(email)).thenReturn(memberToReturn);
		UserService service = new UserService(mockUserRepository);

		assertNull(service.getMemberByUserEmail("wrong email"));
	}

	@Test
	public void testGetAdminByEmail() {
		when(mockUserRepository.getAdminByUserEmail(email)).thenReturn(adminToReturn);
		UserService service = new UserService(mockUserRepository);

		assertEquals(userId, (int) service.getAdminByUserEmail(email).getUserId());
	}

	@Test
	public void testGetAdminByEmailFail() {
		when(mockUserRepository.getAdminByUserEmail(email)).thenReturn(adminToReturn);
		UserService service = new UserService(mockUserRepository);

		assertNull(service.getAdminByUserEmail("wrong email"));
	}

	@Test
	public void testGetTeamIdOfUserByEmail() {
		TeamMember member = new TeamMember();
		member.setTeamId(teamId);
		when(mockUserRepository.getMemberByUserEmail(email)).thenReturn(member);
		UserService service = new UserService(mockUserRepository);

		assertEquals(teamId, (int) service.getTeamIdOfUserByEmail(email));
	}

	@Test
	public void testGetTeamIdOfUserByEmailFail() {
		TeamMember member = new TeamMember();
		String email = "test";
		int teamId = 1;
		member.setTeamId(teamId);
		when(mockUserRepository.getMemberByUserEmail(email)).thenReturn(member);
		UserService service = new UserService(mockUserRepository);

		assertNotEquals(teamId, (int) service.getTeamIdOfUserByEmail("wrong email"));
	}


	@Test
	public void testGetAllUsers() {

		List<User> users = new ArrayList<>();
		users.add(new User(email, password));

		List<UserData> userData = new ArrayList<>();
		userData.add(new UserData(users.get(0)));

		when(mockUserRepository.getAll()).thenReturn(users);
		UserService service = new UserService(mockUserRepository);

		assertEquals(userData.get(0).getEmail(), service.getAllUsers().get(0).getEmail());
	}

	@Test
	public void testGetAllAdminsOfTeam() {
		List<Integer> admins = new ArrayList<>();
		int adminId = 42;
		admins.add(adminId);

		TeamMember member = new TeamMember();


		List<UserData> adminsData = new ArrayList<>();
		adminsData.add(new UserData(email));

		when(mockUserRepository.getAllAdminIdsOfTeam(teamId)).thenReturn(admins);
		when(mockUserRepository.getMemberByUserId(adminId)).thenReturn(member);
		when(mockUserRepository.getById(any(Integer.class))).thenReturn(new User(email, password));
		UserService service = new UserService(mockUserRepository);

		assertEquals(adminsData.get(0).getEmail(), service.getAllAdminsOfTeam(teamId).get(0).getEmail());
	}


	@Test
	public void testGetAllMembersOfTeam() {
		List<Integer> members = new ArrayList<>();
		int memberId = 42;
		members.add(memberId);

		TeamMember member = new TeamMember();


		List<UserData> membersData = new ArrayList<>();
		membersData.add(new UserData(email));

		when(mockUserRepository.getAllMemberIdsOfTeam(teamId)).thenReturn(members);
		when(mockUserRepository.getMemberByUserId(memberId)).thenReturn(member);
		when(mockUserRepository.getById(any(Integer.class))).thenReturn(new User(email, password));
		UserService service = new UserService(mockUserRepository);

		assertEquals(membersData.get(0).getEmail(), service.getAllMembersOfTeam(teamId).get(0).getEmail());
	}
}

package services;

import models.db.Admin;
import models.db.TeamMember;
import models.db.User;
import models.entities.UserData;
import models.repositories.interfaces.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the service used to make operations on the database for Users.
 */
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getUserByEmailAndPassword(String email, String password) {
		return userRepository.get(email, password);
	}

	public User getUserByEmail(String email) {
		return userRepository.getByEmail(email);
	}

	public User createUser(User user) {
		return userRepository.create(user);
	}

	public TeamMember createTeamMember(TeamMember teamMember) {
		return userRepository.createMember(teamMember);
	}

	public TeamMember getMemberByUserEmail(String email) {
		return userRepository.getMemberByUserEmail(email);
	}

	public Admin createAdmin(Admin admin) {
		return userRepository.createAdmin(admin);
	}

	public Admin getAdminByUserEmail(String email) {
		return userRepository.getAdminByUserEmail(email);
	}

	public User updateUser(User user) {
		return userRepository.update(user);
	}

	public void deleteUser(User user) {
		userRepository.delete(user);
	}

	public Integer getTeamIdOfUserByEmail(String email) {
		if (userRepository.getMemberByUserEmail(email) != null) {
			return userRepository
				.getMemberByUserEmail(email)
				.getTeamId();
		}
		else
			return -1;
	}

	public List<UserData> getAllMembersOfTeam(int teamId) {
		List<UserData> data = new ArrayList<>();
		for (Integer userId : userRepository.getAllMemberIdsOfTeam(teamId)) {
			if (userRepository.getMemberByUserId(userId) != null)
				data.add(new UserData(userRepository.getById(userId)));
		}
		return data;
	}

	public List<UserData> getAllAdminsOfTeam(int teamId) {
		List<UserData> data = new ArrayList<>();
		for (Integer userId : userRepository.getAllAdminIdsOfTeam(teamId)) {
			if (userRepository.getMemberByUserId(userId) != null)
				data.add(new UserData(userRepository.getById(userId)));
		}
		return data;
	}

	public List<UserData> getAllUsers() {
		List<UserData> data = new ArrayList<>();
		for (User u: userRepository.getAll()) {
			data.add(new UserData(u));
		}
		return data;
	}
}

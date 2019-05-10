package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Admin;
import models.db.TeamMember;
import models.db.User;

import java.util.List;

/**
 * This interface provides a non-blocking API for possibly blocking operations.
 */
@ImplementedBy(JPAUserRepository.class)
public interface UserRepository {

	User create(User user);
	User get(String email, String password);
	User getByEmail(String email);
	User getById(Integer id);

	List<User> getAll();
	List<Integer> getAllMemberIdsOfTeam(Integer id);

	void delete(User user);
	void update(User user);

	TeamMember createMember(TeamMember member);
	TeamMember getMemberByUserEmail(String email);

	Admin createAdmin(Admin admin);
	Admin getAdminByUserEmail(String email);
}

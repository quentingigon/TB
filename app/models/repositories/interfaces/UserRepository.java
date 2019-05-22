package models.repositories.interfaces;

import com.google.inject.ImplementedBy;
import models.db.Admin;
import models.db.TeamMember;
import models.db.User;
import models.repositories.JPAUserRepository;

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
	List<Integer> getAllAdminIdsOfTeam(Integer id);

	void delete(User user);
	User update(User user);

	TeamMember createMember(TeamMember member);
	TeamMember getMemberByUserEmail(String email);
	TeamMember getMemberByUserId(Integer id);

	Admin createAdmin(Admin admin);
	Admin getAdminByUserEmail(String email);
	Admin getAdminByUserId(Integer id);
}

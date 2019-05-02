package models.repositories;

import com.google.inject.ImplementedBy;
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

	List<User> getAll();

	void delete(User user);
	void update(User user);

	void createMember(TeamMember member);
}

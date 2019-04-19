package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.User;

/**
 * This interface provides a non-blocking API for possibly blocking operations.
 */
@ImplementedBy(JPAUserRepository.class)
public interface UserRepository {

	User create(User user);
	User get(String email, String password);
	User getByEmail(String email);
}

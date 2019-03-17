package models.repositories;

import com.google.inject.ImplementedBy;
import models.User;

import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

/**
 * This interface provides a non-blocking API for possibly blocking operations.
 */
@ImplementedBy(JPAUserRepository.class)
public interface UserRepository {

	CompletionStage<User> add(User user);
	CompletionStage<User> getByEmail(String email);
	CompletionStage<Stream<User>> list();
}

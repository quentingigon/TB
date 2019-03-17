package models.repositories;

import models.User;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Singleton
public class JPAUserRepository implements UserRepository{

	private final JPAApi jpaApi;

	@Inject
	public JPAUserRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public CompletionStage<User> add(User user) {
		return supplyAsync(() -> wrap(em -> insert(em, user)));
	}

	@Override
	public CompletionStage<User> getByEmail(String email) {
		return supplyAsync(() -> wrap(em -> get(em, email)));
	}

	@Override
	public CompletionStage<Stream<User>> list() {
		return supplyAsync(() -> wrap(em -> list(em)));
	}

	private <T> T wrap(Function<EntityManager, T> function) {
		return jpaApi.withTransaction(function);
	}

	private User insert(EntityManager em, User user) {
		em.persist(user);
		return user;
	}

	private Stream<User> list(EntityManager em) {
		List<User> users = em.createQuery("select u from User u", User.class).getResultList();
		return users.stream();
	}

	private User get(EntityManager em, String email) {
		return em.createQuery("select from User where email == " + email, User.class).getSingleResult();
	}
}

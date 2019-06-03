@Singleton
public class JPAUserRepository implements UserRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPAUserRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public User create(User user) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(user);
		});
		return user;
	}

	@Override
	public User get(String email, String password) {
		return jpaApi.withTransaction(entityManager -> {
			String mail = "'" + email + "'";
			String pw = "'" + password + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM users WHERE email = " + mail
					+ " and password = " + pw, User.class);
			try {
				return (User) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public void delete(User user) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
		});
	}

	@Override
	public User update(User user) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(user);
		});
		return user;
	}
}

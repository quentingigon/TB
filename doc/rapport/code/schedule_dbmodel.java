@Entity
@Table(name="schedule", schema="public")
public class Schedule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="schedule_id")
	private Integer id;

	@Column(name="name")
	private String name;
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> fluxes;

	public Schedule() {
	
	}
}
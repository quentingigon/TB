@Mock
private FluxRepository mockFluxRepository;
private FLux fluxToReturn;
...

@Before
public void setUp() {
	MockitoAnnotations.initMocks(this);
	...
}

@Test
public void testGetFluxByName() {
	when(mockFluxRepository.getByName("fluxName")).thenReturn(fluxToReturn);
	FluxService service = new FluxService(mockFluxRepository);

	assertEquals(fluxName, service.getFluxByName("fluxName").getName());
}
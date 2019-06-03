public class FluxChecker implements WSBodyReadables, WSBodyWritables {
    private final WSClient ws;

    @Inject
    public FluxChecker(WSClient ws) {
        this.ws = ws;
    }
    // Cette fonction fait une requete vers l'URL du micro-serveur
    // en fournissant l'id du flux vise et le type de validation requise
    // Le Json recu est transforme en ValidityInterval pour tester les valeurs recues
    // return true si la date courante est dans l'interal
    public boolean checkIfFluxHasSomethingToDisplayByDateTime(Flux flux) {
        final JsonNode[] jsonNode = new JsonNode[1];

        ws.url("data check URL")
            .addQueryParameter("fluxId", String.valueOf(flux.getId()))
            .addQueryParameter("type", "time")
            .get()
            .thenApply(r -> jsonNode[0] = r.getBody(json()));

        if (jsonNode[0] != null) {
            ValidityInterval interval = Json.fromJson(jsonNode[0], ValidityInterval.class);
            DateTime dt = new DateTime();

            // true if date of today is in the interval returned
            return interval.beginning.compareTo(dt) <= 0 &&
                interval.end.compareTo(dt) >= 0;
        }
        return false;
    }
}
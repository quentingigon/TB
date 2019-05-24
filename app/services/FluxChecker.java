package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.db.Flux;
import org.joda.time.DateTime;
import play.libs.Json;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSBodyWritables;
import play.libs.ws.WSClient;

public class FluxChecker implements WSBodyReadables, WSBodyWritables {

	private final WSClient ws;

	@Inject
	public FluxChecker(WSClient ws) {
		this.ws = ws;
	}

	// This function make a request to the URL for data check of the flux,
	// with the fluxId or name as parameter and get a JSON containing the datetime bounds.
	// return true if today is within bounds
	public boolean checkIfFluxHasSomethingToDisplayByDateTime(Flux flux) {

		final JsonNode[] jsonNode = new JsonNode[1];

		ws.url(flux.getDataCheckUrl())
			//.setContentType("application/x-www-form-urlencoded")
			//.addHeader("headerKey", "headerValue")
			.addQueryParameter("fluxId", String.valueOf(flux.getId()))
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

package filters;

import akka.stream.Materializer;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static controllers.JwtUtils.validateJWT;
import static play.mvc.Results.forbidden;

public class JwtFilter extends Filter {

	@Inject
	public JwtFilter(Materializer mat) {
		super(mat);
	}

	@Override
	public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> next, Http.RequestHeader rh) {

		Optional<String> jwtToken = rh.getHeaders().get("JWT");

		/*
		if (!jwtToken.filter(ah -> ah.contains(BEARER)).isPresent()) {
			return CompletableFuture.completedFuture(forbidden("ERR_AUTHORIZATION_HEADER"));
		}*/

		// token is present and valid
		if (jwtToken.isPresent() && validateJWT(jwtToken.get())) {
			return next.apply(rh);
		}
		else {
			return CompletableFuture.completedFuture(forbidden("ERR_AUTHORIZATION_HEADER"));
		}
	}


}

package filters;

import akka.stream.Materializer;
import play.api.routing.HandlerDef;

import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static play.mvc.Results.forbidden;
import static play.routing.Router.Attrs.HANDLER_DEF;

public class LogginFilter extends Filter {

	@Inject
	public LogginFilter(Materializer mat) {
		super(mat);
	}

	@Override
	public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> next, Http.RequestHeader rh) {

		String logged = rh.cookie("logged").value();

		/*
		if (!jwtToken.filter(ah -> ah.contains(BEARER)).isPresent()) {
			return CompletableFuture.completedFuture(forbidden("ERR_AUTHORIZATION_HEADER"));
		}*/

		HandlerDef handlerDef = rh.attrs().get(HANDLER_DEF);

		String controller = handlerDef.controller().toString();

		// token is present and valid
		if ( logged.equals("true")) {
			return next.apply(rh);
		}
		else {
			return CompletableFuture.completedFuture(forbidden("ERR_AUTHORIZATION_HEADER"));
		}
	}


}

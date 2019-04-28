/*
 * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package controllers;

import akka.stream.javadsl.Source;
import com.google.common.collect.Lists;
import models.repositories.FluxRepository;
import models.repositories.ScreenRepository;
import play.libs.EventSource;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.FluxManager;
import views.html.eventsource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.*;

@Singleton
public class EventSourceController extends Controller implements Observer {

    @Inject
    FluxRepository fluxRepository;

    @Inject
    ScreenRepository screenRepository;

    private static Source<String, ?> source;
    private Source<String, ?> oldSource;

    private boolean updated = false;

    @Inject
    EventSourceController() {

        FluxManager fluxManager = FluxManager.getInstance();
        fluxManager.addObserver(this);

        // TODO maybe optimize ?
        Thread t = new Thread(fluxManager);
        // t.start();
    }

    public Result index() {
        return ok(eventsource.render())
            .withHeader("Access-Control-Allow-Credentials", "true")
            .withHeader("Access-Control-Expose-Headers", "*")
            .withHeader("Access-Control-Allow-Origin", "localhost");
    }

    private static Http.Cookie[] toArray(final Http.Cookies cookies) {
    	Http.Cookie[] cookieArray = new Http.Cookie[1];
		return Lists.newArrayList(cookies.iterator()).toArray(cookieArray);
	}
    @Override
    @SuppressWarnings("unchecked")
    public synchronized void update(Observable o, Object arg) {

        if (source == null) {
            source = Source.tick(Duration.ZERO, Duration.ofSeconds(5), "tick");
        }
        else {
            List<String> list = new ArrayList<>();
            list.add((String) arg);
            Source<String, ?> s = Source.from(list);
            // source.merge(s);
            // source.flatMapMerge(1, s);
        }
    	//source = Source.single((String) arg);
        // System.out.println("Source was updated");
    	updated = true;
    }

    public Result events() {

        // TODO error page
        while (source != null) {
            // System.out.println("source is null");
        }

        //final Source<EventSource.Event, ?> eventSource;

        return ok().chunked(source
            //.map(t -> EventSource.Event.event(t.concat("\nretry: 10000\\n")))
            .map(EventSource.Event::event)
            .via(EventSource.flow()))
            .as(Http.MimeTypes.EVENT_STREAM);

        /*
        final Result bkp = ok();

        Result result = ok().chunked(eventSource.via(EventSource.flow()));
        if (bkp.flash() != null)
        	result = result.withFlash(bkp.flash());
        if (bkp.cookies() != null) {
			Iterator<Http.Cookie> iterator = bkp.cookies().iterator();
			while (iterator.hasNext())
				result = result.withCookies(iterator.next());
		}
        if (bkp.session() != null)
        	result = result.withSession(bkp.session());
        return result.as(Http.MimeTypes.EVENT_STREAM);*/

    }
}

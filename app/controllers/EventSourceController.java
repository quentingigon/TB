/*
 * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package controllers;

import akka.actor.Cancellable;
import akka.stream.javadsl.Source;
import models.ScheduleTicker;
import models.db.Flux;
import models.db.Screen;
import models.repositories.FluxRepository;
import models.repositories.ScreenRepository;
import play.libs.EventSource;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.eventsource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class EventSourceController extends Controller implements Observer {

    @Inject
    FluxRepository fluxRepository;

    @Inject
    ScreenRepository screenRepository;

    private Source<String, Cancellable> source;

    public Result index() {
        return ok(eventsource.render())
            .withHeader("Access-Control-Allow-Credentials", "true")
            .withHeader("Access-Control-Expose-Headers", "*")
            .withHeader("Access-Control-Allow-Origin", "localhost");
    }

    public Result events() {

        // TODO make that elsewhere
        List<Screen> screens = new ArrayList<>();
        screens.add(screenRepository.getByMacAddress("1234"));

        Queue<Flux> fluxes = new LinkedList<>();
        fluxes.add(fluxRepository.getByName("flux1"));

        ScheduleTicker ticker = new ScheduleTicker(fluxes, screens);
        ticker.addObserver(this);
        Thread thread1 = new Thread(ticker, "Thread 1");
        thread1.start();

        if (source != null) {
            final Source<EventSource.Event, ?> eventSource = source.map(EventSource.Event::event);

            return ok().chunked(eventSource.via(EventSource.flow())).as(Http.MimeTypes.EVENT_STREAM);
        }
        else {
            // TODO change
            return redirect(routes.HomeController.index());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(Observable o, Object arg) {
        source = (Source<String, Cancellable>) arg;
    }
}

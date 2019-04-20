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

    @Override
    @SuppressWarnings("unchecked")
    public void update(Observable o, Object arg) {
        source = (Source<String, Cancellable>) arg;
    }

    public Result events() {

        // TODO make that elsewhere
        List<Screen> screens1 = new ArrayList<>();
        screens1.add(screenRepository.getByMacAddress("1234"));

        Queue<Flux> fluxes1 = new LinkedList<>();
        fluxes1.add(fluxRepository.getByName("flux1"));

        ScheduleTicker ticker1 = new ScheduleTicker(fluxes1, screens1);

        List<Screen> screens2 = new ArrayList<>();
        screens2.add(screenRepository.getByMacAddress("test"));

        Queue<Flux> fluxes2 = new LinkedList<>();
        fluxes2.add(fluxRepository.getByName("flux2"));

        ScheduleTicker ticker2 = new ScheduleTicker(fluxes2, screens2);

        ticker1.addObserver(this);
        ticker2.addObserver(this);
        Thread thread1 = new Thread(ticker1, "Thread 1");
        Thread thread2 = new Thread(ticker2, "Thread 2");
        thread1.start();
        thread2.start();

        while (source == null) {
            // System.out.println("source is null");
        }

        final Source<EventSource.Event, ?> eventSource = source.map(EventSource.Event::event);

        return ok().chunked(eventSource.via(EventSource.flow())).as(Http.MimeTypes.EVENT_STREAM);
    }
}

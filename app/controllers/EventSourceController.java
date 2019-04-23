/*
 * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package controllers;

import akka.stream.javadsl.Source;
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
import java.util.Observable;
import java.util.Observer;

@Singleton
public class EventSourceController extends Controller implements Observer {

    @Inject
    FluxRepository fluxRepository;

    @Inject
    ScreenRepository screenRepository;

    private Source<String, ?> source;

    @Inject
    EventSourceController() {

        FluxManager fluxManager = FluxManager.getInstance();
        fluxManager.addObserver(this);

        // TODO maybe optimize ?
        Thread t = new Thread(fluxManager);
        t.start();
    }

    public Result index() {
        return ok(eventsource.render())
            .withHeader("Access-Control-Allow-Credentials", "true")
            .withHeader("Access-Control-Expose-Headers", "*")
            .withHeader("Access-Control-Allow-Origin", "localhost");
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void update(Observable o, Object arg) {
        source = (Source<String, ?>) arg;
    }

    public Result events() {

        if (source == null) {
            // System.out.println("source is null");
        }

        final Source<EventSource.Event, ?> eventSource = source.map(EventSource.Event::event);

        return ok().chunked(eventSource.via(EventSource.flow())).as(Http.MimeTypes.EVENT_STREAM);
    }
}

/*
 * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package controllers;

import akka.stream.javadsl.Source;
import models.db.Flux;
import models.db.Screen;
import models.repositories.FluxRepository;
import models.repositories.ScreenRepository;
import play.libs.EventSource;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.FluxEvent;
import services.FluxScheduler;
import views.html.eventsource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@Singleton
public class EventSourceController extends Controller implements Observer {

    @Inject
    FluxRepository fluxRepository;

    @Inject
    ScreenRepository screenRepository;

    private Source<String, ?> source;
    private boolean updated = false;

    @Inject
    EventSourceController() {

        Screen screen1 = new Screen("1234");
        Screen screen2 = new Screen("test");

        Flux flux1 = new Flux("flux1", "https://heig-vd.ch/");
        Flux flux2 = new Flux("flux2", "https://hes-so.ch/");


        FluxEvent fe1 = new FluxEvent(flux1, screen1);
        FluxEvent fe2 = new FluxEvent(flux2, screen2);

        List<FluxEvent> events = new ArrayList<>();
        events.add(fe1);
        events.add(fe2);

        FluxScheduler fluxScheduler = new FluxScheduler(events);

        fluxScheduler.addObserver(this);


        Thread t = new Thread(fluxScheduler);
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
    public void update(Observable o, Object arg) {
        source = (Source<String, ?>) arg;
        updated = true;
    }

    public Result events() {

        /*
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

        ticker1.run();
        ticker2.run();

        while (source == null) {
            // System.out.println("source is null");
        }
        */

        final Source<EventSource.Event, ?> eventSource = source.map(EventSource.Event::event);

        return ok().chunked(eventSource.via(EventSource.flow())).as(Http.MimeTypes.EVENT_STREAM);
    }
}

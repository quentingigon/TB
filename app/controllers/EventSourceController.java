/*
 * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package controllers;

import akka.stream.javadsl.Source;
import models.db.Flux;
import models.db.RunningSchedule;
import models.db.Screen;
import models.repositories.FluxRepository;
import models.repositories.ScreenRepository;
import play.libs.EventSource;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.FluxEvent;
import services.FluxManager;
import services.RunningScheduleService;
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

    @Inject
    EventSourceController() {

        Screen screen1 = new Screen("1234");
        Screen screen2 = new Screen("test");
        List<Screen> screens1 = new ArrayList<>();
        List<Screen> screens2 = new ArrayList<>();
        screens1.add(screen1);
        screens2.add(screen2);

        Flux flux1 = new Flux("flux1", 10000, "https://heig-vd.ch/");
        Flux flux2 = new Flux("flux2", 10000, "https://hes-so.ch/");
        List<Flux> fluxes1 = new ArrayList<>();
        List<Flux> fluxes2 = new ArrayList<>();
        fluxes1.add(flux1);
        // fluxes1.add(flux2);
        fluxes2.add(flux2);

        FluxEvent fe1 = new FluxEvent(flux1, screen1);
        FluxEvent fe2 = new FluxEvent(flux2, screen2);

        List<FluxEvent> events = new ArrayList<>();
        events.add(fe1);
        events.add(fe2);

        FluxManager fluxManager = new FluxManager();
        fluxManager.addObserver(this);

        RunningSchedule rs1 = new RunningSchedule();
        rs1.setScreens(screens1);
        rs1.setFluxes(fluxes1);

        RunningSchedule rs2 = new RunningSchedule();
        rs2.setScreens(screens2);
        rs2.setFluxes(fluxes2);

        RunningScheduleService service1 = new RunningScheduleService(rs1);
        service1.addObserver(fluxManager);

        RunningScheduleService service2 = new RunningScheduleService(rs2);
        service2.addObserver(fluxManager);

        Thread t = new Thread(fluxManager);
        t.start();

        // executorService.execute(service1);
        // executorService.execute(service2);
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

        while (source == null) {
            // System.out.println("source is null");
        }

        final Source<EventSource.Event, ?> eventSource = source.map(EventSource.Event::event);

        return ok().chunked(eventSource.via(EventSource.flow())).as(Http.MimeTypes.EVENT_STREAM);
    }
}

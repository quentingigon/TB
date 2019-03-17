/*
 * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package controllers;

import akka.stream.javadsl.Source;
import play.libs.EventSource;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.eventsource;

import javax.inject.Singleton;

@Singleton
public class EventSourceController extends Controller implements JavaTicker {

    public Result index() {
        return ok(eventsource.render())
            .withHeader("Access-Control-Allow-Credentials", "true")
            .withHeader("Access-Control-Expose-Headers", "*")
            .withHeader("Access-Control-Allow-Origin", "localhost");
    }

    public Result events() {
        final Source<EventSource.Event, ?> eventSource = getUrlsSource().map(EventSource.Event::event);

        return ok().chunked(eventSource.via(EventSource.flow())).as(Http.MimeTypes.EVENT_STREAM);
    }
}

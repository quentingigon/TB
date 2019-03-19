/*
 * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package controllers;

import akka.actor.Cancellable;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;

public interface JavaTicker {

    default Source<String, ?> getUrlsSource() {
        final ArrayList<String> urls = new ArrayList<>();
        urls.add("https://player.rts.ch/p/rts/portal-detail?urn=urn:rts:video:1967124&autoplay=true");
        urls.add("http://www.heig-vd.ch");
        urls.add("http://www.hes-so.ch");

        String[] macAdresses = new String[] {"test", "test2"};

        final Random generator = new Random();
        final Source<String, Cancellable> tickSource =
            Source.tick(
                Duration.ZERO,
                Duration.of(20000L, ChronoUnit.MILLIS), // 20 seconds
                "TICK");
        return tickSource.map((tick) -> urls.get(generator.nextInt(3)) + "|" + String.join(",", macAdresses));
    }

}

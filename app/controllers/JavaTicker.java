/*
 * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package controllers;

import akka.actor.Cancellable;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;

public interface JavaTicker {

    default Source<String, ?> getStringSource() {
        final ArrayList<String> urls = new ArrayList<>();
        urls.add("https://player.rts.ch/p/rts/portal-detail?urn=urn:rts:video:1967124&autoplay=true");
        urls.add("http://www.heig-vd.ch");
        urls.add("http://www.hes-so.ch");
        final Random generator = new Random();
        final Source<String, Cancellable> tickSource =
            Source.tick(
                Duration.ZERO,
                Duration.of(10000L, ChronoUnit.MILLIS),
                "TICK");
        return tickSource.map((tick) -> urls.get(generator.nextInt(3)));
    }

    default Source<JsonNode, ?> getJsonSource() {
        final DateTimeFormatter df = DateTimeFormatter.ISO_INSTANT;
        final Source<String, Cancellable> tickSource = Source.tick(
            Duration.ZERO,
            Duration.of(100L, ChronoUnit.MILLIS),
            "TICK");
        return tickSource.map((tick) -> {
            ObjectNode result = Json.newObject();
            result.put("timestamp", df.format(ZonedDateTime.now()));
            return result;
        });
    }

}

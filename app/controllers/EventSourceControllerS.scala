package controllers

import akka.stream.scaladsl.{Source}
import akka.util.ByteString
import javax.inject.Inject
import play.api.http.ContentTypes
import play.api.mvc._
import play.libs.EventSource
import services.{EventObserver, FluxManager}

class EventSourceControllerS @Inject()
(cc: ControllerComponents)
extends AbstractController(cc){

  var observer : EventObserver = EventObserver.getInstance()

  @Inject
  def EventSourceControllerS() {
    val fluxManager = FluxManager.getInstance
    fluxManager.addObserver(EventObserver.getInstance())

    // TODO maybe optimize ?
    val t = new Thread(fluxManager)
    // t.start()
  }

  def index = Action {
    Ok("It works!")
  }

  def events = Action {

    while (observer.getSource == null) {

    }

    if (observer.getSource != null) {
      val eventSource = Source.fromGraph(observer.getSource.map(EventSource.Event.event))
      Ok.chunked[ByteString](eventSource via EventSource.flow).as(ContentTypes.EVENT_STREAM)
    }
    else {
      Ok("Source is null").as(ContentTypes.EVENT_STREAM)
    }

    //return ok().chunked(eventSource.via(EventSource.flow())).as(Http.MimeTypes.EVENT_STREAM);

  }

}
package controllers

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import controllers.EventActorManager.{Register, SendMessage, UnRegister}
import javax.inject.{Inject, Singleton}
import play.api.http.ContentTypes
import play.api.mvc._
import play.libs.EventSource

import scala.collection.mutable
import scala.concurrent.ExecutionContext

@Singleton
class EventSourceControllerS @Inject() (system: ActorSystem,
                                        cc: ControllerComponents)
                                       (implicit executionContext: ExecutionContext)
extends AbstractController(cc) {


  private[this] val manager = system.actorOf(EventActorManager.props)

  def send(event: String) =  {
    print("Send event to screens : " + event)
    manager ! SendMessage(event)
    Ok
  }

  def index = Action {
    Ok("It works!")
  }

  def events = Action {

    val source  =
      Source
        .actorRef[String](32, OverflowStrategy.dropHead)
        .watchTermination() { case (actorRef, terminate) =>
          manager ! Register(actorRef)
          terminate.onComplete(_ => manager ! UnRegister(actorRef))
          actorRef
        }

    val eventSource = Source.fromGraph(source.map(EventSource.Event.event))

    Ok.chunked(eventSource via EventSource.flow).as(ContentTypes.EVENT_STREAM)

  }
}

class EventActor extends Actor {

  private[this] val actors = mutable.Set.empty[ActorRef]

  def receive = {
    case Register(actorRef)   => actors += actorRef
    case UnRegister(actorRef) => actors -= actorRef
    case SendMessage(message) => actors.foreach(_ ! message)
  }

}

object EventActorManager {
  def props: Props = Props[EventActor]

  case class SendMessage(message: String)

  case class Register(actorRef: ActorRef)
  case class UnRegister(actorRef: ActorRef)
}



@Singleton
class EventSourceController @Inject() (system: ActorSystem,
                                        cc: ControllerComponents)
                                       (implicit executionContext: ExecutionContext)
extends AbstractController(cc) {

  private[this] val manager = system.actorOf(EventActorManager.props)
  implicit def pair[E]: EventNameExtractor[E] = EventNameExtractor[E](_ => Some("test1"))

  def send(event: String) =  {
    print("Send event to screens : " + event)
    manager ! SendMessage(event)
    Ok
  }

  def index = Action {
    Ok(eventsource.render())
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




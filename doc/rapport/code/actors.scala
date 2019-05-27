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
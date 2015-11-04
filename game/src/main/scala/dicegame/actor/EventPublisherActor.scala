package dicegame.actor

import akka.stream.actor.ActorPublisher
import dicegame.domain.GameEvent
import akka.stream.actor.ActorPublisherMessage.Request

/**
 * Created by nigelnindo on 11/4/15.
 */
class EventPublisherActor extends ActorPublisher[GameEvent]{

  var eventCache: List[GameEvent] = Nil

  override def receive = {
    case Request(n) =>
      while(isActive && totalDemand > 0 && eventCache.nonEmpty){
        val (head :: tail ) = eventCache
        onNext(head)
        eventCache = tail
      }
    case event: GameEvent =>
      if (isActive && totalDemand > 0)
        onNext(event)
      else
        eventCache :+= event
  }

}

package com.gvolpe.prototypes.processor.actors

import akka.actor.{ActorLogging, Actor, Props}

object EventsManager {
  case class ProcessedEvent(id: Long)
  def props = Props[EventsManager]
}

class EventsManager extends Actor with ActorLogging {

  import Consumer._
  import Processor._

  var events = Set[Consumer.Event]()

  def receive = {
    case event: Event =>
      events += event
      val processor = context.actorOf(Processor.props)
      processor ! Process(event)
    case Done(event: Event) =>
      log.info("[DONE] Event >> " + event)
      events -= event
    case _ =>
      log.warning("Unknown message")
  }

}

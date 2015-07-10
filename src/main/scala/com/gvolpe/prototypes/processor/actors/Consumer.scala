package com.gvolpe.prototypes.processor.actors

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import play.api.libs.json.Json

object Consumer {
  implicit val format = Json.format[Event]

  case class Event(id: Long, message: String) {
    override def toString = id + " - " + message
  }

  def props = Props[Consumer]
}

class Consumer extends Actor with ActorLogging {

  import Consumer._

  private var eventActors = Map[Long, ActorRef]()

  def receive = {
    case event: Event =>
      val actorName = event.id
      eventActors.get(actorName) match {
        case Some(destination) =>
          destination ! event
        case None =>
          val destination = context.actorOf(EventsManager.props, s"d-$actorName")
          eventActors += (actorName -> destination)
          destination ! event
      }
  }

}

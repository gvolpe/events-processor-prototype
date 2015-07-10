package com.gvolpe.prototypes.processor.actors

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import akka.pattern.pipe
import com.gvolpe.prototypes.processor.http.AsyncWebClient

object Processor {
  case class Result(sender: ActorRef, event: Consumer.Event, body: String)
  case class Process(event: Consumer.Event)
  case class Done(event: Consumer.Event)
  def props = Props[Processor]
}

class Processor extends Actor with ActorLogging {

  import Processor._

  implicit val exec = context.dispatcher

  def receive = {
    case Process(event) =>
      log.info("Event >> " + event)
      val ref = sender()
      val url = "http://www.paddypower.com"
      // Just an http operation to simulate latency
      AsyncWebClient get url map (Result(ref, event, _)) pipeTo self
    case Result(ref, event, _) =>
      ref ! Done(event)
  }

}

package com.gvolpe.prototypes.processor

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.gvolpe.prototypes.processor.flows.{ConsumerProcessorFlow, ConsumerFlow, EventsGeneratorFlow}
import io.scalac.amqp.Connection

object Prototype extends App {

  implicit val system = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer()

  val connection = Connection()

  EventsGeneratorFlow(connection).run()
  //ConsumerFlow(connection).run()
  ConsumerProcessorFlow(connection).run()

  //JSON {"id":123456,"message":"hola"}

}

package com.gvolpe.prototypes.processor.flows

import java.nio.ByteOrder

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.util.ByteString
import com.gvolpe.prototypes.processor.actors.Consumer.Event
import io.scalac.amqp.{Connection, Message}

import scala.concurrent.Future

object EventsGeneratorFlow extends JsonMappingUtils[Event] {

  def apply(connection: Connection)(implicit system: ActorSystem): RunnableGraph[Unit] = {

    import system.dispatcher

    FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
      import FlowGraph.Implicits._
      implicit val order = ByteOrder.LITTLE_ENDIAN

      val events = Source(1 to 1000).mapAsync(100)(n => Future(Event(n.toLong, "Hello!")))
      val bytes = Flow[Event] map (e => toJson(e)) map (j => Message(body = ByteString(j)))
      val queue = connection.publishDirectly(queue = "events")
      val out = Sink(queue)

      events ~> bytes ~> out
    }

  }

}
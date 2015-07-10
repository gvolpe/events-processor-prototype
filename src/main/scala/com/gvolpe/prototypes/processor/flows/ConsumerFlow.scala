package com.gvolpe.prototypes.processor.flows

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import com.gvolpe.prototypes.processor.actors.Consumer
import Consumer.Event
import io.scalac.amqp.Connection

import scala.util.Try

object ConsumerFlow extends JsonMappingUtils[Event] {

  def apply(connection: Connection)(implicit system: ActorSystem): RunnableGraph[Unit] = {

    val queue = connection.consume(queue = "events")
    val consumer = system.actorOf(Consumer.props, "consumer")

    FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
      import FlowGraph.Implicits._

      val mq = Source(queue) map (d => fromJson(d.message.body.utf8String))

      val broadcast = builder.add(Broadcast[Try[Option[Event]]](2))
      val events = Flow[Try[Option[Event]]] filter (_.isSuccess) map (_.get) filter (_.isDefined) map (_.get)
      val errors = Flow[Try[Option[Event]]] filter (_.isFailure) map (_.failed.get)

      val processor = Sink.actorRef(consumer, "")
      val errorsConsole = Sink.foreach(println)

      mq ~> broadcast ~> events ~> processor
            broadcast ~> errors ~> errorsConsole
    }
  }

  //val exchange: Subscriber[Message] = connection.publish(exchange = "destinations", routingKey = "events")
  //Source(queue) map (_.message) to (Sink(exchange)) run()

  //  val source: Source[SampleMessage, ActorRef] = Source.actorRef[SampleMessage](1000, OverflowStrategy.fail)
  //  val ref: ActorRef = Flow[SampleMessage].to(Sink.foreach(println)).runWith(source)

}

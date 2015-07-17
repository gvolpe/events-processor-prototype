package com.gvolpe.prototypes.processor.flows

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import com.gvolpe.prototypes.processor.actors.Consumer.Event
import com.gvolpe.prototypes.processor.http.AsyncWebClient
import io.scalac.amqp.Connection

import scala.concurrent.Future
import scala.util.Try

object ConsumerProcessorFlow extends JsonMappingUtils[Event] {

  def apply(connection: Connection)(implicit system: ActorSystem): RunnableGraph[Unit] = {

    import system.dispatcher

    val eventsQueue = connection.consume(queue = "events")

    def simulateLatency(event: Event): Future[String] = {
      val url = "http://www.paddypower.com"
      AsyncWebClient get url map (_.concat(s"-$event"))
    }

    FlowGraph.closed() { implicit builder: FlowGraph.Builder[Unit] =>
      import FlowGraph.Implicits._

      //val eventsJson = Source(eventsQueue) map (d => fromJson(d.message.body.utf8String))

      // Ordered pipeline using mapAsync
      val eventsJson = Source(eventsQueue).mapAsync(100)(d => Future(fromJson(d.message.body.utf8String)))

      val broadcast = builder.add(Broadcast[Try[Option[Event]]](2))
      val events = Flow[Try[Option[Event]]] filter (_.isSuccess) map (_.get) filter (_.isDefined) map (_.get)
      val errors = Flow[Try[Option[Event]]] filter (_.isFailure) map (_.failed.get)

      val processedEvents = events.mapAsync(100)(e => simulateLatency(e))

      val console = Sink.foreach(println)

      eventsJson ~> broadcast ~> processedEvents ~> console
                    broadcast ~> errors ~> console
    }

  }

}

package com.gvolpe.prototypes.processor

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.stream.testkit.scaladsl.TestSource
import akka.testkit.{ImplicitSender, TestKit}
import com.gvolpe.prototypes.processor.actors.Consumer
import com.gvolpe.prototypes.processor.flows.JsonMappingUtils
import Consumer.Event
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

object ConsumerFlowSpec extends JsonMappingUtils[Event] {

  def randomJson: String = {
    val n = (Math.random() * 10).toLong
    s""" {"id":$n,"message":"Hello"} """
  }

}

class ConsumerFlowSpec extends TestKit(ActorSystem("consumerFlowSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  import ConsumerFlowSpec._

  implicit val materializer = ActorMaterializer()

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A consumer flow" must {

    "deliver the right event" in {
      val numbersToEvents = Flow[Int] map (n => s""" {"id":$n,"message":"Hello"} """) map (j => fromJson(j))
      val events = Flow[Try[Option[Event]]] filter (_.isSuccess) map (_.get.get)

      val (probe, future) = TestSource.probe[Int]
        .via(numbersToEvents)
        .via(events)
        .toMat(Sink.head[Event])(Keep.both)
        .run()

      probe.sendNext(12)
      Await.ready(future, 500 millis)
      val e = future.value.get.get
      assert(e.id == 12)
      assert(e.message == "Hello")
    }

    "throw a json parsing exception" in {
      val numbersToEvents = Flow[Int] map (n => s""" {"id":$n,"message": """) map (j => fromJson(j))
      val errors = Flow[Try[Option[Event]]] filter (_.isFailure) map (_.failed.get)

      val (probe, future) = TestSource.probe[Int]
        .via(numbersToEvents)
        .via(errors)
        .toMat(Sink.head[Throwable])(Keep.both)
        .run()

      probe.sendNext(12)
      Await.ready(future, 500 millis)
      val t = future.value.get.get
      assert(t.getMessage.contains("Unexpected end-of-input within/between OBJECT entries"))
    }

  }

}

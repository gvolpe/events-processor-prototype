organization := "com.paddypower"

name := """events-processor-prototype"""

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

val akkaStreamV = "1.0-RC4"
val akkaV= "2.3.12"

libraryDependencies ++= Seq(
  "io.scalac" %% "reactive-rabbit" % "1.0.0",
  "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
  "com.typesafe.play" %% "play-json" % "2.3.4",
  "com.ning" % "async-http-client" % "1.7.19",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.typesafe.akka" %% "akka-stream-testkit-experimental" % akkaStreamV % "test"
)
name := "EventSourcedGame"

version := "1.0"

val commonSettings = Seq(
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")
)

val akkaVersion = "2.3.9"
val akkaStreamVersion = "1.0-M3"
val reactiveRabbitVersion = "0.2.2"
val sprayVersion = "1.3.1"
val json4sVersion = "3.2.11"
val logbackVersion = "1.1.2"


// Using fork of reactive-rabbit until new version of original project is available.
// Uncomment original dependencies and remove this one once it's ready.
lazy val reactiveRabbitFork = RootProject(uri("git://github.com/LukasGasior1/reactive-rabbit.git"))

lazy val game = project.in(file("game"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamVersion,
      // "io.scalac" %% "reactive-rabbit" % reactiveRabbitVersion,
      "io.spray" %% "spray-routing" % sprayVersion,
      "io.spray" %% "spray-can" % sprayVersion,
      "org.json4s" %% "json4s-native" % json4sVersion,
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "ch.qos.logback" % "logback-classic" % logbackVersion
    ),
    fork := true
  )
  .dependsOn(reactiveRabbitFork)

lazy val statistics = project.in(file("statistics"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamVersion,
      "io.spray" %% "spray-routing" % sprayVersion,
      "io.spray" %% "spray-can" % sprayVersion,
      // "io.scalac" %% "reactive-rabbit" % reactiveRabbitVersion,
      "org.json4s" %% "json4s-native" % json4sVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion
    ),
    fork := true
  )
  .dependsOn(reactiveRabbitFork)
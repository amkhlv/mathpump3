

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.andreimikhailov",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "MathPump",
    mainClass in assembly := Some("diffpump.Main"),
    libraryDependencies ++= Seq(
      "org.bitbucket.cowwoc" % "diff-match-patch" % "1.1",
      "com.typesafe.akka" %% "akka-actor" % "2.5.8",
      "com.rabbitmq" % "amqp-client" % "5.1.1",
      "log4j" % "log4j" % "1.2.17",
      "org.yaml" % "snakeyaml" % "1.19",
      "joda-time" % "joda-time" % "2.9.9"
    )
  )



lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.andreimikhailov",
      scalaVersion := "2.12.11",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "MathPump",
    assembly / mainClass := Some("diffpump.Main"),
    assembly / assemblyOutputPath := file(System.getenv("HOME") + "/.local/lib/mathpump/mathpump-assembly.jar"),
    libraryDependencies ++= Seq(
      "org.bitbucket.cowwoc" % "diff-match-patch" % "1.2",
      "com.typesafe.akka" %% "akka-actor" % "2.5.23",
      "com.rabbitmq" % "amqp-client" % "5.7.3",
      "log4j" % "log4j" % "1.2.17",
      "org.slf4j" % "slf4j-simple" % "1.7.27",
      "joda-time" % "joda-time" % "2.10.3"
    )
  )

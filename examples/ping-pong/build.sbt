
name := "Ping-Pong-Example"

scalaVersion := "2.11.7"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.2"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.2" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.4.2"

libraryDependencies += "io.kamon" %% "kamon-core" % "0.6.0"

libraryDependencies += "io.kamon" %% "kamon-akka" % "0.6.0"

libraryDependencies += "io.kamon" %% "kamon-log-reporter" % "0.6.0"

libraryDependencies += "org.aspectj" % "aspectjweaver" % "1.8.6" withSources()

fork in run := true

kamon.aspectj.sbt.AspectjRunner.testSettings

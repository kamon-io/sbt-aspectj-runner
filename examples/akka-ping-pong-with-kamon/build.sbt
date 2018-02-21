name := "Ping-Pong-Example"

scalaVersion := "2.12.2"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.8"
libraryDependencies += "io.kamon" %% "kamon-core" % "1.0.1"
libraryDependencies += "io.kamon" %% "kamon-akka-2.5" % "1.0.1"
libraryDependencies += "io.kamon" %% "kamon-prometheus" % "1.0.0"

fork in run := true


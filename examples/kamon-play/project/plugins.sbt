// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers +=  "Kamon Repository Snapshots"  at "http://snapshots.kamon.io"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.10")

addSbtPlugin("io.kamon" % "aspectj-play-23-runner" % "0.1.0-SNAPSHOT")

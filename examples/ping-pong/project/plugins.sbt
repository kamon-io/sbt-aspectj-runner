// Comment to get more information during initialization
logLevel := Level.Warn

resolvers +=  "Kamon Repository Snapshots"  at "http://snapshots.kamon.io"

addSbtPlugin("io.kamon" % "aspectj-runner" % "0.1.4")

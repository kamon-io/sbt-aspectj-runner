addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.11")

resolvers += Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon" % "sbt-aspectj-runner-play-2.6" % "1.1-73454753f2290789913ff1f4bba8a620c7142f77")

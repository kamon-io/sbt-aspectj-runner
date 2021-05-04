addSbtPlugin("io.kamon" % "kamon-sbt-umbrella" % "0.0.16-SNAPSHOT")

resolvers += "Artifactory Realm" at "https://iadvize.jfrog.io/iadvize/iadvize-sbt"

credentials += Credentials(
  "Artifactory Realm",
  "iadvize.jfrog.io",
  (sys.env.get("ARTIFACTORY_USERNAME") orElse sys.props.get("ARTIFACTORY_USERNAME")).getOrElse(""),
  (sys.env.get("ARTIFACTORY_PASS") orElse sys.props.get("ARTIFACTORY_PASS")).getOrElse("")
)

addSbtPlugin("com.iadvize"       % "sbt-iadvize-plugin"  % "4.6.2")

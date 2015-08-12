name := "kamon-play"

version := "1.0"

scalaVersion := "2.11.7"

val kamonVersion = "0.4.0"

val dependencies = Seq(
  "io.kamon"    %% "kamon-core"           % kamonVersion,
  "io.kamon"    %% "kamon-play"           % kamonVersion,
  "io.kamon"    %% "kamon-log-reporter"   % kamonVersion
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
                                      .settings(libraryDependencies ++= dependencies)


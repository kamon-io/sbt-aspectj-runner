name := "kamon-play"

version := "1.0"

scalaVersion := "2.11.7"

val kamonVersion = "0.4.1-SNAPSHOT"

val resolutionRepos = Seq("Kamon Repository Snapshots" at "http://snapshots.kamon.io")

val dependencies = Seq(
  "io.kamon"    %% "kamon-core"           % kamonVersion,
  "io.kamon"    %% "kamon-play-24"        % kamonVersion,
  "io.kamon"    %% "kamon-log-reporter"   % kamonVersion
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
                                      .settings(resolvers ++= resolutionRepos)
                                      .settings(libraryDependencies ++= dependencies)


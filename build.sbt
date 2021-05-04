/*
 * =========================================================================================
 * Copyright © 2013-2018 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

import sbt._
import sbt.Keys._

val publishSettings = Seq(
  publishTo := Some("Artifactory Realm" at "https://iadvize.jfrog.io/iadvize/iadvize-sbt"),
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishMavenStyle := true
)

def crossSbtDependency(module: ModuleID, sbtVersion: String, scalaVersion: String): ModuleID = {
  Defaults.sbtPluginExtra(module, sbtVersion, scalaVersion)
}

val aspectjTools = "org.aspectj" % "aspectjtools" % "1.8.13"
val playSbtPluginFor26 = "com.typesafe.play" % "sbt-plugin" % "2.6.11"
val playSbtPluginFor27 = "com.typesafe.play" % "sbt-plugin" % "2.7.0"


lazy val sbtAspectjRunner = Project("root", file("."))
  .aggregate(aspectjRunner, aspectjRunnerPlay26, aspectjRunnerPlay27)
  .settings(noPublishing: _*)

lazy val aspectjRunner = Project("sbt-aspectj-runner", file("sbt-aspectj-runner"))
  .settings(
    sbtPlugin := true,
    libraryDependencies ++= Seq(aspectjTools),
  ).settings(publishSettings: _*)


lazy val aspectjRunnerPlay26 = Project("sbt-aspectj-runner-play-26", file("sbt-aspectj-runner-play-2.6"))
  .dependsOn(aspectjRunner)
  .settings(
    sbtPlugin := true,
    moduleName := "sbt-aspectj-runner-play-2.6",
    libraryDependencies ++= Seq(
      aspectjTools,
      crossSbtDependency(playSbtPluginFor26, (sbtBinaryVersion in pluginCrossBuild).value, scalaBinaryVersion.value)
    )
  ).settings(publishSettings: _*)

lazy val aspectjRunnerPlay27 = Project("sbt-aspectj-runner-play-27", file("sbt-aspectj-runner-play-2.7"))
  .dependsOn(aspectjRunner)
  .settings(
    sbtPlugin := true,
    moduleName := "sbt-aspectj-runner-play-2.7",
    libraryDependencies ++= Seq(
      aspectjTools,
      crossSbtDependency(playSbtPluginFor27, (sbtBinaryVersion in pluginCrossBuild).value, scalaBinaryVersion.value)
    )
  ).settings(publishSettings: _*)

scalaVersion in ThisBuild := "2.12.13"

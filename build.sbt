/*
 * =========================================================================================
 * Copyright © 2013-2015 the kamon project <http://kamon.io/>
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

val aspectjTools = "org.aspectj" % "aspectjtools" % "1.8.10"
val playSbtPluginFor24 = pluginExtra("com.typesafe.play" % "sbt-plugin" % "2.4.2")
def pluginExtra(module:ModuleID):ModuleID = Defaults.sbtPluginExtra(module, "0.13", "2.10")


lazy val sbtAspectjRunner = Project("root", file("."))
  .settings(sbtPlugin := true)
  .settings(noPublishing: _*)
  .aggregate(aspectjRunner, aspectjPlay24Runner)

lazy val aspectjRunner = Project("sbt-aspectj-runner", file("sbt-aspectj-runner"))
  .settings(sbtPlugin := true)
  .settings(libraryDependencies ++= Seq(aspectjTools))

lazy val aspectjPlay24Runner = Project("sbt-aspectj-play-runner", file("sbt-aspectj-play-runner"))
  .dependsOn(aspectjRunner)
  .settings(sbtPlugin := true)
  .settings(libraryDependencies ++= Seq(aspectjTools, playSbtPluginFor24))






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

import Dependencies._

import Settings._

val noPublishing = Seq(publish := (), publishLocal := (), publishArtifact := false)

lazy val sbtAspectjRunner = Project("sbt-aspectj-runner", file("."))
  .settings(basicSettings: _*)
  .settings(formatSettings: _*)
  .settings(noPublishing: _*)
  .aggregate(aspectjRunner, aspectjPlayRunner)

lazy val aspectjRunner = Project("aspectj-runner", file("aspectj-runner"))
  .settings(basicSettings: _*)
  .settings(formatSettings: _*)
  .settings(libraryDependencies ++= Seq(aspectjTools))

lazy val aspectjPlayRunner = Project("aspectj-play-runner", file("aspectj-play-runner"))
  .dependsOn(aspectjRunner)
  .settings(basicSettings: _*)
  .settings(formatSettings: _*)
  .settings(libraryDependencies ++= Seq(aspectjTools, playSbtPlugin))



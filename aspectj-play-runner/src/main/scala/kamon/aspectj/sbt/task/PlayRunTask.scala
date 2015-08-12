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
 * Taken from: https://github.com/typesafehub/sbt-echo/blob/master/play/src/main/scala-sbt-0.13/com/typesafe/sbt/echo/EchoPlaySpecific.scala#L23
 */

package kamon.aspectj.sbt.task

import kamon.aspectj.sbt.AspectjRunner
import kamon.aspectj.sbt.runner.PlayRunner
import play.Play._
import play.PlayImport.PlayKeys._
import sbt.Keys._
import sbt._

object PlayRunTask {
  import AspectjRunner.AspectjRunnerKeys._
  import AspectjRunner.Runner

  def runnerPlaySettings(): Seq[Setting[_]] = Seq(
    playRunHooks in Runner <<= playRunHooks,
    playRunHooks in Runner <+= PlayRunner.createRunHook,
    run in Runner <<= runnerPlayRunTask)

  def runnerPlayRunTask: Def.Initialize[InputTask[Unit]] = Def.inputTask {
    playRunTask(playRunHooks in Runner,
      externalDependencyClasspath in Runner,
      aspectjWeaverClassLoader in Runner,
      playReloaderClasspath,
      playReloaderClassLoader,
      playAssetsClassLoader).evaluated
  }
}

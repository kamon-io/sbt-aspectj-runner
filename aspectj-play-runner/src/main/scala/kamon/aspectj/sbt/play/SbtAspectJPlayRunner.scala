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

package kamon.aspectj.sbt.play

import _root_.play.sbt.PlayImport.PlayKeys._
import _root_.play.sbt.PlayInternalKeys._
import _root_.play.sbt.{Colors, Play, PlayRunHook}
import kamon.aspectj.sbt.SbtAspectJRunner
import org.aspectj.weaver.loadtime.WeavingURLClassLoader
import sbt.Keys._
import sbt._

object SbtAspectJPlayRunner extends AutoPlugin {
  import SbtAspectJRunner.Keys.aspectjWeaver

  override def trigger = AllRequirements
  override def requires = Play && SbtAspectJRunner

  override def projectSettings: Seq[Setting[_]] = Seq(
    playRunHooks += runningWithAspectJNoticeHook.value,
    playDependencyClassLoader := weavingClassLoaderCreator.value,
    playReloaderClassLoader := weavingClassLoaderCreator.value,
    javaOptions in Runtime += "-Dorg.aspectj.tracing.factory=default"
  )

  def weavingClassLoaderCreator =  Def.task {
    (name: String, urls: Array[URL], parent: ClassLoader) ⇒
      val weaverPath = Path.toURLs(Seq(aspectjWeaver.value))
      new WeavingURLClassLoader(weaverPath ++ urls, parent)
  }

  def runningWithAspectJNoticeHook: Def.Initialize[Task[RunningWithAspectJNotice]] = Def.task {
    new RunningWithAspectJNotice(streams.value.log)
  }

  class RunningWithAspectJNotice(log: Logger) extends PlayRunHook {
    override def beforeStarted(): Unit = {
      log.info(Colors.green("Running the application with Aspectj Weaver"))
    }
  }
}

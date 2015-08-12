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

package kamon.aspectj.sbt
package runner

import kamon.aspectj.sbt.task.PlayRunTask
import org.aspectj.weaver.loadtime.WeavingURLClassLoader
import play.runsupport.Reloader.ClassLoaderCreator
import sbt._

object PlayRunner {
  import AspectjRunner.AspectjRunnerKeys._
  import AspectjRunner.Runner

  def playRunSettings(): Seq[Setting[_]] = Seq(aspectjWeaverClassLoader in Runner := createWeavingClassLoader) ++ PlayRunTask.runnerPlaySettings

  def createWeavingClassLoader: ClassLoaderCreator = (name, urls, parent) ⇒ new WeavingURLClassLoader(urls, parent)

  def createRunHook: Def.Initialize[Task[RunHook]] = Def.task { new RunHook() }

  class RunHook extends play.sbt.PlayRunHook {
    override def beforeStarted(): Unit = {
      println(s"""\u001B[32m Running Play application with Aspectj Weaver. \u001B[0m""")
      System.setProperty("org.aspectj.tracing.factory", "default")
      sys.props.getOrElseUpdate("config.resource", "application.conf")
    }
    override def afterStopped(): Unit = {}
  }
}

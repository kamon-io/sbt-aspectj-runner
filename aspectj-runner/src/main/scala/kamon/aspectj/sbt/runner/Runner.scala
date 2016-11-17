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

import kamon.aspectj.sbt.process.RunInAspectjClassLoader
import sbt.Keys._
import sbt._

object Runner {

  import AspectjRunner.AspectjRunnerKeys._

  val AspectjVersion = "1.8.9"
  val WeaverCompileConfiguration = config("weaver-compile-configuration").extend(Configurations.RuntimeInternal).hide
  val WeaverScope = config("weaver-scope").hide

  def aspectjWeaverDependency(version: String) = Seq("org.aspectj" % "aspectjweaver" % version % WeaverScope.name)

  def findAspectjWeaver: Def.Initialize[Task[Option[File]]] = update map { report ⇒
    report.matching(moduleFilter(organization = "org.aspectj", name = "aspectjweaver")
      // Make sure to only select binary jars.
      && artifactFilter(`type` = "jar")) headOption
  }

  def findAspectjArtifact(dependencies: Seq[ModuleID]): Seq[ModuleID] = {
    val aspectjArtifact = (dependencies: Seq[ModuleID]) ⇒ {
      dependencies find { module ⇒ module.organization == "org.aspectj" && module.name == "aspectjrt" }
    }

    aspectjArtifact(dependencies) map (_ ⇒ Seq.empty[ModuleID]) getOrElse {
      val revision = dependencies find {
        module ⇒ module.organization == "org.aspectj" && module.name == "aspectjweaver"
      } map (_.revision) getOrElse AspectjVersion
      Seq("org.aspectj" % "aspectjrt" % revision)
    }
  }

  def runnerJavaOptions(weaver: Option[File]): Seq[String] =
    weaver.toSeq map (weaver ⇒ s"-javaagent:${weaver.getAbsolutePath}")

  def aspectjWeaverRunner: Def.Initialize[Task[ScalaRun]] = Def.task {
    val forkConfig = ForkOptions(javaHome.value, outputStrategy.value, Seq.empty, Some(baseDirectory.value), javaOptions.value ++ aspectjRunnerOptions.value, connectInput.value)
    if (fork.value) new ForkRun(forkConfig)
    else new RunInAspectjClassLoader(trapExit.value)
  }
}


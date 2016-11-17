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

import sbt._
import Keys._
import Keys.{runner => r}

object AspectjRunner extends AutoPlugin {
  import runner.Runner._

  val Runner = config("aspectj-runner").extend(Compile)
  val RunnerTest = config("aspectj-runner-test").extend(Runner,Test)

  object AspectjRunnerKeys {
    val aspectjRunnerOptions = TaskKey[Seq[String]]("aspectj-runner-options")
    val aspectjOptionalArtifact = TaskKey[Seq[ModuleID]]("aspectj-optional-artifact")
    val aspectjVersion = SettingKey[String]("aspectj-version")
    val aspectjWeaver = TaskKey[Option[File]]("aspectj-weaver")
    val aspectjWeaverClassLoader = TaskKey[(String, Array[URL], ClassLoader) => ClassLoader]("weaver-class-loader")
  }

  import AspectjRunnerKeys._

  override def trigger = AllRequirements
  override def requires = plugins.JvmPlugin

  override lazy val projectSettings: Seq[Setting[_]] = compileSettings

  def compileSettings: Seq[Setting[_]] =
    inConfig(Runner)(defaultSettings(Runtime)) ++
    inConfig(Runner)(runSettings(Runtime)) ++
      otherSettings

  def testSettings: Seq[Setting[_]] =
    inConfig(RunnerTest)(defaultSettings(Test)) ++
      inConfig(RunnerTest)(runSettings(Test))

  def defaultSettings(currentScope: Configuration): Seq[Setting[_]] = Seq(
    aspectjVersion := AspectjVersion,
    aspectjWeaver := findAspectjWeaver.value,
    aspectjRunnerOptions := (aspectjWeaver map runnerJavaOptions).value,
    aspectjOptionalArtifact := (libraryDependencies map findAspectjArtifact).value,
    unmanagedClasspath := (unmanagedClasspath in currentScope).value,
    managedClasspath := (managedClasspath in currentScope).value,
    internalDependencyClasspath  := (internalDependencyClasspath in currentScope).value,
    externalDependencyClasspath := Classpaths.concat(unmanagedClasspath, managedClasspath).value,
    dependencyClasspath := Classpaths.concat(internalDependencyClasspath, externalDependencyClasspath).value,
    exportedProducts := (exportedProducts in currentScope).value,
    fullClasspath := Classpaths.concatDistinct(exportedProducts, dependencyClasspath).value
  )

  def runSettings(currentScope: Configuration): Seq[Setting[_]] = Seq(
    mainClass in run := (mainClass in run in currentScope).value,
    inTask(run)(Seq(r <<= aspectjWeaverRunner)).head,
    run <<= Defaults.runTask(fullClasspath, mainClass in run, r in run),
    runMain <<= Defaults.runMainTask(fullClasspath, r in run)
  )

  def otherSettings: Seq[Setting[_]] = Seq(
    ivyConfigurations ++= Seq(WeaverCompileConfiguration, WeaverScope),
    libraryDependencies ++=  (aspectjVersion in Runner)(aspectjWeaverDependency).value,
    allDependencies ++= (aspectjOptionalArtifact in Runner).value)
}

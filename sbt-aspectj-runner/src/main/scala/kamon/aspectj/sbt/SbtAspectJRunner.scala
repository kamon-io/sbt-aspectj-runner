/*
 * =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
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

import java.lang.reflect.Method
import java.lang.reflect.Modifier.{isPublic, isStatic}

import sbt._
import Keys._
import org.aspectj.weaver.loadtime.WeavingURLClassLoader
import sbt.classpath._

object SbtAspectJRunner extends AutoPlugin {

  val AspectJRunner = config("aspectj-runner")
  val DefaultAspectJVersion = "1.8.10"

  object Keys {
    val aspectjVersion = SettingKey[String]("aspectj-version")
    val aspectjWeaver = TaskKey[File]("aspectj-weaver")
    val aspectjRunnerJvmForkOptions = TaskKey[Seq[String]]("aspectj-runner-options")
  }

  import Keys._

  override def trigger = AllRequirements
  override def requires = plugins.JvmPlugin
  override def projectConfigurations: Seq[Configuration] = Seq(AspectJRunner)

  override def projectSettings: Seq[Setting[_]] = Seq(
    aspectjVersion := DefaultAspectJVersion,
    aspectjWeaver := findAspectJWeaver.value,
    aspectjRunnerJvmForkOptions := jvmForkOptions.value,
    libraryDependencies += aspectjWeaverDependency.value,
    runner in run in Compile := aspectjWeaverRunner.value
  )

  def aspectjWeaverDependency = Def.setting {
    "org.aspectj" % "aspectjweaver" % aspectjVersion.value % AspectJRunner.name
  }

  def findAspectJWeaver = Def.task {
    update.value.matching(
      moduleFilter(organization = "org.aspectj", name = "aspectjweaver") &&
      artifactFilter(`type` = "jar")
    ).head
  }

  def jvmForkOptions = Def.task {
    Seq(s"-javaagent:${aspectjWeaver.value.getAbsolutePath}")
  }

  def aspectjWeaverRunner: Def.Initialize[Task[ScalaRun]] = Def.task {
    if ((fork in run).value) {
      val forkOptions = ForkOptions(
        javaHome.value,
        outputStrategy.value,
        Seq.empty,
        Some(baseDirectory.value),
        javaOptions.value ++ aspectjRunnerJvmForkOptions.value,
        connectInput.value
      )
      new ForkRun(forkOptions)

    } else {
      new RunWithAspectJ(aspectjWeaver.value, scalaInstance.value, trapExit.value, taskTemporaryDirectory.value)
    }
  }


  /**
    *   This class is a dirty copy of sbt.Run, with all required dependencies to make sure we are using the
    *   WeavingURLClassLoader instead of a plain URLClassLoader.
    *
    */
  class RunWithAspectJ(aspectjWeaver: File, instance: ScalaInstance, trapExit: Boolean, nativeTmp: File) extends ScalaRun {
    /** Runs the class 'mainClass' using the given classpath and options using the scala runner.*/
    def run(mainClass: String, classpath: Seq[File], options: Seq[String], log: Logger) = {
      log.info("Running " + mainClass + " " + options.mkString(" "))
      val classpathWithWeaver = aspectjWeaver +: classpath

      def execute() =
        try { run0(mainClass, classpathWithWeaver, options, log) }
        catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }
      def directExecute() = try { execute(); None } catch { case e: Exception => log.trace(e); Some(e.toString) }

      if (trapExit) Run.executeTrapExit(execute(), log) else directExecute()
    }

    private def run0(mainClassName: String, classpath: Seq[File], options: Seq[String], log: Logger): Unit = {
      log.debug("  Classpath:\n\t" + classpath.mkString("\n\t"))
      val weaverLoader = makeLoader(classpath, instance, nativeTmp)
      val main = getMainMethod(mainClassName, weaverLoader)
      invokeMain(weaverLoader, main, options)
    }

    private def createClasspathResources(appPaths: Seq[File], bootPaths: Seq[File]): Map[String, String] = {
      def make(name: String, paths: Seq[File]) = name -> Path.makeString(paths)
      Map(make(ClasspathUtilities.AppClassPath, appPaths), make(ClasspathUtilities.BootClassPath, bootPaths))
    }

    private def makeLoader(classpath: Seq[File], instance: ScalaInstance, nativeTemp: File): ClassLoader =
      toLoader(classpath, createClasspathResources(classpath, instance.jars), nativeTemp)

    private def javaLibraryPaths: Seq[File] = IO.parseClasspath(System.getProperty("java.library.path"))

    private def toLoader(paths: Seq[File], resourceMap: Map[String, String], nativeTemp: File): ClassLoader =
      new WeavingURLClassLoader(Path.toURLs(paths), null) with RawResources with NativeCopyLoader {
        override def resources = resourceMap
        override val config = new NativeCopyConfig(nativeTemp, paths, javaLibraryPaths)
        override def toString =
          s"""|WeavingURLClassLoader with NativeCopyLoader with RawResources(
              |  urls = $paths,
              |  resourceMap = ${resourceMap.keySet},
              |  nativeTemp = $nativeTemp
              |)""".stripMargin
      }

    private def invokeMain(loader: ClassLoader, main: Method, options: Seq[String]): Unit = {
      val currentThread = Thread.currentThread
      val oldLoader = Thread.currentThread.getContextClassLoader
      currentThread.setContextClassLoader(loader)
      try { main.invoke(null, options.toArray[String]) }
      finally { currentThread.setContextClassLoader(oldLoader) }
    }

    private def getMainMethod(mainClassName: String, loader: ClassLoader) = {
      val mainClass = Class.forName(mainClassName, true, loader)
      val method = mainClass.getMethod("main", classOf[Array[String]])
      // jvm allows the actual main class to be non-public and to run a method in the non-public class,
      //  we need to make it accessible
      method.setAccessible(true)
      val modifiers = method.getModifiers
      if (!isPublic(modifiers)) throw new NoSuchMethodException(mainClassName + ".main is not public")
      if (!isStatic(modifiers)) throw new NoSuchMethodException(mainClassName + ".main is not static")
      method
    }
  }
}
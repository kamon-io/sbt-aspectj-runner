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
 *
 * Taken from: https://github.com/typesafehub/sbt-echo/blob/master/akka/src/main/scala/com/typesafe/sbt/echo/EchoProcess.scala#L58
 */

package kamon.aspectj.sbt.process

import java.lang.reflect.{ Method, Modifier }
import org.aspectj.weaver.loadtime.WeavingURLClassLoader
import sbt._

class RunMain(loader: ClassLoader, mainClass: String, options: Seq[String]) {
  def run(trapExit: Boolean, log: Logger): Option[String] = {
    if (trapExit) {
      Run.executeTrapExit(runMain, log)
    } else {
      try { runMain; None }
      catch { case e: Exception ⇒ log.trace(e); Some(e.toString) }
    }
  }

  def runMain: Unit = {
    try {
      val main = getMainMethod(mainClass, loader)
      invokeMain(loader, main, options)
    } catch {
      case e: java.lang.reflect.InvocationTargetException ⇒ throw e.getCause
    }
  }

  def getMainMethod(mainClass: String, loader: ClassLoader): Method = {
    val main = Class.forName(mainClass, true, loader)
    val method = main.getMethod("main", classOf[Array[String]])
    val modifiers = method.getModifiers
    if (!Modifier.isPublic(modifiers)) throw new NoSuchMethodException(mainClass + ".main is not public")
    if (!Modifier.isStatic(modifiers)) throw new NoSuchMethodException(mainClass + ".main is not static")
    method
  }

  def invokeMain(loader: ClassLoader, main: Method, options: Seq[String]): Unit = {
    val currentThread = Thread.currentThread
    val oldLoader = currentThread.getContextClassLoader
    currentThread.setContextClassLoader(loader)
    try { main.invoke(null, options.toArray[String]) }
    finally { currentThread.setContextClassLoader(oldLoader) }
  }
}

class RunInAspectjClassLoader(trapExit: Boolean) extends ScalaRun {
  override def run(mainClass: String, classpath: Seq[File], options: Seq[String], log: Logger): Option[String] = {
    log.info(s"Running $mainClass ${options.mkString(" ")}")
    System.setProperty("org.aspectj.tracing.factory", "default")
    val loader = new WeavingURLClassLoader(Path.toURLs(classpath), null)
    new RunMain(loader, mainClass, options).run(trapExit, log)
  }
}
package kamon.aspectj.sbt

import org.aspectj.weaver.loadtime.WeavingURLClassLoader
import sbt._
import sbt.internal.inc.classpath._

import scala.util.Try

object SbtCross {
  type ScalaInstance = sbt.internal.inc.ScalaInstance

  def directExecute(execute: => Unit, log: Logger):Try[Unit] = {
    val result = Try(execute)
    result.failed.foreach(e => log.trace(e))
    result
  }

  private def javaLibraryPaths: Seq[File] = IO.parseClasspath(System.getProperty("java.library.path"))

  def toLoader(paths: Seq[File], resourceMap: Map[String, String], nativeTemp: File): ClassLoader =
    new WeavingURLClassLoader(Path.toURLs(paths), null) with RawResources with NativeCopyLoader {
      override def resources = resourceMap
      override val config = new NativeCopyConfig(nativeTemp.toPath, paths.map(_.toPath), javaLibraryPaths.map(_.toPath))
      override def toString =
        s"""|WeavingURLClassLoader with NativeCopyLoader with RawResources(
            |  urls = $paths,
            |  resourceMap = ${resourceMap.keySet},
            |  nativeTemp = $nativeTemp
            |)""".stripMargin
    }

  val AppClassPath = ClasspathUtilities.AppClassPath

  val BootClassPath = ClasspathUtilities.BootClassPath
}

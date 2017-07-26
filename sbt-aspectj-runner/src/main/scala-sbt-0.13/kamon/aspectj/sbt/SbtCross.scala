package kamon.aspectj.sbt

import org.aspectj.weaver.loadtime.WeavingURLClassLoader
import sbt._
import sbt.classpath._

object SbtCross {
  type ScalaInstance = sbt.ScalaInstance

  def directExecute(execute: => Unit, log: Logger):Option[String] = {
    try { execute; None } catch { case e: Exception => log.trace(e); Some(e.toString) }
  }

  private def javaLibraryPaths: Seq[File] = IO.parseClasspath(System.getProperty("java.library.path"))

  def toLoader(paths: Seq[File], resourceMap: Map[String, String], nativeTemp: File): ClassLoader =
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

  val AppClassPath = ClasspathUtilities.AppClassPath

  val BootClassPath = ClasspathUtilities.BootClassPath

}

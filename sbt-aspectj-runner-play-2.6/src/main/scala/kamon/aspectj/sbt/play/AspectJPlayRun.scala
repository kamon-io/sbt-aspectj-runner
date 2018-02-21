package kamon.aspectj.sbt.play

import play.core.BuildLink
import play.dev.filewatch.{SourceModificationWatch => PlaySourceModificationWatch, WatchState => PlayWatchState}
import play.sbt.PlayImport.PlayKeys._
import play.sbt.PlayInternalKeys._
import play.sbt.run.PlayReload
import play.sbt.run.PlayRun.{generatedSourceHandlers, getPollInterval, getSourcesFinder, sleepForPoolDelay}
import play.sbt.{Colors, PlayNonBlockingInteractionMode}
import sbt.Keys._
import sbt.{AttributeKey, Compile, Def, InputTask, Keys, Project, State, TaskKey, Watched}

import scala.annotation.tailrec

object AspectJPlayRun {

  // This file was copied and modified from the URL bellow since there was no other sensible way to our
  // current knowledge of changing the Classloaders to support AspectJ as we did for Play 2.4/2.5
  //
  // https://github.com/playframework/playframework/blob/master/framework/src/sbt-plugin/src/main/scala/play/sbt/run/PlayRun.scala#L49-L180

  val playWithAspectJRunTask = playRunTask(playRunHooks, playDependencyClasspath,
    playReloaderClasspath, playAssetsClassLoader)

  def playRunTask(
    runHooks: TaskKey[Seq[play.sbt.PlayRunHook]],
    dependencyClasspath: TaskKey[Classpath],
    reloaderClasspath: TaskKey[Classpath],
    assetsClassLoader: TaskKey[ClassLoader => ClassLoader]
  ): Def.Initialize[InputTask[Unit]] = Def.inputTask {

    val args = Def.spaceDelimited().parsed

    val state = Keys.state.value
    val scope = resolvedScoped.value.scope
    val interaction = playInteractionMode.value

    val reloadCompile = () => PlayReload.compile(
      () => Project.runTask(playReload in scope, state).map(_._2).get,
      () => Project.runTask(reloaderClasspath in scope, state).map(_._2).get,
      () => Project.runTask(streamsManager in scope, state).map(_._2).get.toEither.right.toOption
    )

    lazy val devModeServer = AspectJReloader.startDevMode(
      runHooks.value,
      (javaOptions in sbt.Runtime).value,
      playCommonClassloader.value,
      dependencyClasspath.value.files,
      reloadCompile,
      assetsClassLoader.value,
      playMonitoredFiles.value,
      fileWatchService.value,
      generatedSourceHandlers,
      playDefaultPort.value,
      playDefaultAddress.value,
      baseDirectory.value,
      devSettings.value,
      args,
      (mainClass in (Compile, Keys.run)).value.get,
      AspectJPlayRun
    )

    interaction match {
      case nonBlocking: PlayNonBlockingInteractionMode =>
        nonBlocking.start(devModeServer)
      case blocking =>
        devModeServer

        println()
        println(Colors.green("(Server started, use Enter to stop and go back to the console...)"))
        println()

        // If we have both Watched.Configuration and Watched.ContinuousState
        // attributes and if Watched.ContinuousState.count is 1 then we assume
        // we're in ~ run mode
        val maybeContinuous = for {
          watched <- state.get(Watched.Configuration)
          watchState <- state.get(Watched.ContinuousState)
          if watchState.count == 1
        } yield watched

        maybeContinuous match {
          case Some(watched) =>
            // ~ run mode
            interaction doWithoutEcho {
              twiddleRunMonitor(watched, state, devModeServer.buildLink, Some(PlayWatchState.empty))
            }
          case None =>
            // run mode
            interaction.waitForCancel()
        }

        devModeServer.close()
        println()
    }
  }

  /**
    * Monitor changes in ~run mode.
    */
  @tailrec
  private def twiddleRunMonitor(watched: Watched, state: State, reloader: BuildLink, ws: Option[PlayWatchState] = None): Unit = {
    val ContinuousState = AttributeKey[PlayWatchState]("watch state", "Internal: tracks state for continuous execution.")
    def isEOF(c: Int): Boolean = c == 4

    @tailrec def shouldTerminate: Boolean = (System.in.available > 0) && (isEOF(System.in.read()) || shouldTerminate)

    val sourcesFinder: PlaySourceModificationWatch.PathFinder = getSourcesFinder(watched, state)
    val watchState = ws.getOrElse(state get ContinuousState getOrElse PlayWatchState.empty)

    val (triggered, newWatchState, newState) =
      try {
        val (triggered: Boolean, newWatchState: PlayWatchState) = PlaySourceModificationWatch.watch(sourcesFinder, getPollInterval(watched), watchState)(shouldTerminate)
        (triggered, newWatchState, state)
      } catch {
        case e: Exception =>
          val log = state.log
          log.error("Error occurred obtaining files to watch.  Terminating continuous execution...")
          log.trace(e)
          (false, watchState, state.fail)
      }

    if (triggered) {
      //Then launch compile
      Project.synchronized {
        val start = System.currentTimeMillis
        Project.runTask(compile in Compile, newState).get._2.toEither.right.map { _ =>
          val duration = System.currentTimeMillis - start
          val formatted = duration match {
            case ms if ms < 1000 => ms + "ms"
            case seconds => (seconds / 1000) + "s"
          }
          println("[" + Colors.green("success") + "] Compiled in " + formatted)
        }
      }

      // Avoid launching too much compilation
      sleepForPoolDelay

      // Call back myself
      twiddleRunMonitor(watched, newState, reloader, Some(newWatchState))
    } else {
      ()
    }
  }

}

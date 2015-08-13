sbt-aspectj-runner   [![Build Status](https://travis-ci.org/kamon-io/sbt-aspectj-runner.png)](https://travis-ci.org/kamon-io/sbt-aspectj-runner)
=========

This project contains two [sbt] plugins to perform [Load-time weaving] (LTW) with Aspectj in a fast and easy way in all projects and also we will be able to do a [run in development mode] in [play] based projects and everything will be fine. In order to run those plugins will we require sbt **0.13.x**.

##Sbt Projects

###Configure

Add the `aspectj-runner` plugin to `project/plugins.sbt`. It should looks like this:

```scala

addSbtPlugin("io.kamon" % "aspectj-runner" % "0.1.0")

```
###Run

To run your application with `LTW` support, the only thing that we need is call the run task. This uses the same underlying settings for the regular run tasks, and also add the configuration needed to instrument your application, depending if the task needs `fork` or not.

To run the default or discovered main class use:

```scala

aspectj-runner:run

```
We have two cases:
* When the task forks: In this case, the forked process will run with the `-javaagent:<jarpath>` and that is all.
* When the task Non forks: Here we will load the application with a custom classloader called [WeavingURLClassLoader] that allow instantiate a weaver and weave classes after loading and before defining them in the JVM. This enables load-time weaving to be supported in environments where no weaving agent is available.

##Play Projects
When we try run a Play applicacion with LTW we will find with some issues:

* Play has a `dynamic class-loading` mechanism that loads dependency classes differently then it
does the classes in your source code in order to be able to reload changes in dev mode, this breaks some [Load-time Weaving Requirements].
* Also by design Play doesn't and can not fork, hence setting `javaOptions` has no effect.
* In the case that we be able to also fork Play run to attach the `-javaagent:<jarpath>`, but we'll lose the dev mode reloading in that case.

Having said that, **let’s get into the rabbit hole!**.

In order to achieve the LTW support in  Play's dev run, we will use the same  approach used in the `Activator Inspect`, we will configure a custom classloader, in our case  it will be the [WeavingURLClassLoader] rather than a java agent.  (and Play run will set up to allow the class loader to be configured). The magic is [here].

###Configure

Add the `aspectj-play-runner` plugin to `project/plugins.sbt`. It should looks like this:

```scala

addSbtPlugin("io.kamon" % "aspectj-play-runner" % "0.1.0")

```

the code above is for **Play 2.4.x**, in the case of **Play 2.3.x**, It should looks like this:

```scala

addSbtPlugin("io.kamon" % "aspectj-play-23-runner" % "0.1.0")

```

###Run

```scala

aspectj-play-runner:run

```
Enjoy!.

##Examples

There are full [runnable examples][examples].

## Branches to look

[The 'master' branch](https://github.com/kamon-io/sbt-aspectj-runner/tree/master) is where the development of the latest major version lives on and the the main diference with the [The 'play-2.3.x'  branch](https://github.com/kamon-io/sbt-aspectj-runner/tree/play-2.3.x) is the play version.

[sbt]: https://github.com/sbt/sbt
[play]: https://www.playframework.com
[aspectj]: http://www.eclipse.org/aspectj
[WeavingURLClassLoader]: https://eclipse.org/aspectj/doc/next/weaver-api/org/aspectj/weaver/loadtime/WeavingURLClassLoader.html
[run in development mode]: https://www.playframework.com/documentation/2.4.2/PlayConsole#Running-the-server-in-development-mode
[Load-time weaving]: https://eclipse.org/aspectj/doc/released/devguide/ltw.html#ltw-introduction
[examples]: https://github.com/kamon-io/sbt-aspectj-runner/tree/master/examples
[here]:https://github.com/kamon-io/sbt-aspectj-runner/blob/master/aspectj-play-runner/src/main/scala/kamon/aspectj/sbt/task/PlayRunTask.scala#L38
[Load-time Weaving Requirements]:https://eclipse.org/aspectj/doc/released/devguide/ltw-rules.html

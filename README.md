sbt-aspectj-runner   [![Build Status](https://travis-ci.org/kamon-io/sbt-aspectj-runner.png)](https://travis-ci.org/kamon-io/sbt-aspectj-runner)
=========


This project contains two [sbt] plugins to perform [Load-time weaving] \(LTW\) with Aspectj in a fast and easy way in our sbt projects. With these plugins we will be able to do a [run in development mode] in [play] based projects seamlessly. Only versions **0.13.x** and upper of sbt are currently supported.


##Sbt Projects

###Configure

Add the `aspectj-runner` plugin to `project/plugins.sbt`. It should look like this:

```scala

addSbtPlugin("io.kamon" % "aspectj-runner" % "0.1.0")

```
###Run

To run your application with `LTW` support, the only thing we need is to call `run` or `run-main` tasks. These use the same underlying settings for the regular run task, but also add the configuration needed to instrument your application, depending whether the task should `fork` or not.

To run the default or discovered main class use:

```scala

aspectj-runner:run

```

To run a specific main class:

```scala

aspectj-runner:run-main my.awesome.package.MainClass

```

Additionally, we can run tests with LTW. For that we need to include the test-specific settings in our build:

```scala

kamon.aspectj.sbt.AspectjRunner.testSettings

```
and finally

```scala

aspectj-runner-test:run

```

We have two scenerios:
* **fork in run := true**: In this case, the forked process will run with the `-javaagent:<jarpath>` and that's all.
* **fork in run := false**: Here we will load the application with a custom classloader called [WeavingURLClassLoader] that instantiates a weaver and weaves classes after loading, and before defining them in the JVM. This enables load-time weaving support in environments where no weaving agent is available.


##Play Projects
If try to run a Play application with LTW we will face some issues:

* Play has a `dynamic class-loading` mechanism that loads dependency classes differently from classes in your source code in order to be able to reload changes in dev mode; This breaks some [Load-time Weaving Requirements].
* Also, by design, Play can not fork, hence setting `javaOptions` has no effect.
* If we manage to fork Play somehow and run with `-javaagent:<jarpath>` attached, we'll lose dev mode reloading.


Having said that, **let’s get into the rabbit hole!**.

In order to achieve LTW support in  Play's dev run, we will use the same approach used in `Activator Inspect` and `sbt-echo`, taking heavily inspiration from the latter.

**The basic idea is**: configure a custom classloader, in our case  it will be the [WeavingURLClassLoader] rather than a java agent. (and Play run will be set up to allow the class loader to be configured). The magic is [here].

###Configure

Add the `aspectj-play-runner` plugin to `project/plugins.sbt`. It should look like this:

```scala
addSbtPlugin("io.kamon" % "aspectj-play-runner" % "0.1.0")

```

the code above is for **Play 2.4.x**, in the case of **Play 2.3.x**, it should look like this:

```scala

addSbtPlugin("io.kamon" % "aspectj-play-23-runner" % "0.1.0")

```

###Run

```scala

aspectj-play-runner:run

```
###Enjoy!

##Examples

There are full [runnable examples][examples].

## Branches to look

The [master] branch is where the development of the latest version lives on and the the main difference with the [Play-2.3.x] branch is the play version.

[sbt]: https://github.com/sbt/sbt
[play]: https://www.playframework.com
[aspectj]: http://www.eclipse.org/aspectj
[WeavingURLClassLoader]: https://eclipse.org/aspectj/doc/next/weaver-api/org/aspectj/weaver/loadtime/WeavingURLClassLoader.html
[run in development mode]: https://www.playframework.com/documentation/2.4.2/PlayConsole#Running-the-server-in-development-mode
[Load-time weaving]: https://eclipse.org/aspectj/doc/released/devguide/ltw.html#ltw-introduction
[examples]: https://github.com/kamon-io/sbt-aspectj-runner/tree/master/examples
[here]:https://github.com/kamon-io/sbt-aspectj-runner/blob/master/aspectj-play-runner/src/main/scala/kamon/aspectj/sbt/task/PlayRunTask.scala#L38
[Load-time Weaving Requirements]:https://eclipse.org/aspectj/doc/released/devguide/ltw-rules.html
[master]:https://github.com/kamon-io/sbt-aspectj-runner/tree/master
[play-2.3.x]:https://github.com/kamon-io/sbt-aspectj-runner/tree/play-2.3.x

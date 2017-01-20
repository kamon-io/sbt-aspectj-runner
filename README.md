sbt-aspectj-runner
=========
[![Build Status](https://travis-ci.org/kamon-io/sbt-aspectj-runner.png)](https://travis-ci.org/kamon-io/sbt-aspectj-runner)
[![Download](https://api.bintray.com/packages/kamon-io/sbt-plugins/sbt-aspectj-runner/images/download.svg)](https://bintray.com/kamon-io/sbt-plugins/sbt-aspectj-runner/_latestVersion)


This project contains two [sbt] plugins that automatically configure your build to perform [Load-time weaving] \(LTW\)
with Aspectj when running your applicaction. These plugins enable you to seamlessly run both regular applications and
[play] projects [in development mode] and ensure that your aspects will always be woven as expected. Only **0.13.x**
versions of sbt are currently supported.


## Regular Projects (non-Play)

### Configure

Add the `aspectj-runner` plugin to `project/plugins.sbt`, as well as our sbt-plugins repository. It should look like
this:

```scala
resolvers += Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon" % "aspectj-runner" % "1.0.0")
```

### Run

Just `run`!

Here is what the plugin will do depending on your `fork` settings:
* **fork in run := true**: In this case, the forked process will run with the `-javaagent:<jarpath>` and that's all.
* **fork in run := false**: Here we will load the application with a custom classloader called [WeavingURLClassLoader]
  that instantiates a weaver and weaves classes after loading, and before defining them in the JVM.


## Play Projects
If try to run a Play application with LTW we will face some issues:

* Play has a `dynamic class-loading` mechanism that loads dependency classes differently from classes in your source
  code in order to be able to reload changes in dev mode; This breaks some [Load-time Weaving Requirements].
* Also, by design, Play can not fork, hence setting `javaOptions` has no effect.

Having said that, **let’s get into the rabbit hole!**.

In order to achieve LTW support in  Play's dev run, we will use the same approach used in `Activator Inspect` and `sbt-
echo`, taking heavily inspiration from the latter.

**The basic idea is**: configure a custom classloader, in our case  it will be the [WeavingURLClassLoader] rather than a
java agent, and Play run will be set up to use this custom classloader. The magic is [here].

### Configure

Add the `aspectj-play-runner` plugin to `project/plugins.sbt`. It should look like this:

```scala
resolvers += Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon" % "aspectj-play-runner" % "1.0.0")

```

This plugin has been tested with **Play 2.4.8** and **Play 2.5.10**.

### Run

Just execute `run` on the SBT console, everything should already be in place for you.


##Examples

There are full [runnable examples][examples].

[sbt]: https://github.com/sbt/sbt
[play]: https://www.playframework.com
[aspectj]: http://www.eclipse.org/aspectj
[WeavingURLClassLoader]: https://eclipse.org/aspectj/doc/next/weaver-api/org/aspectj/weaver/loadtime/WeavingURLClassLoader.html
[in development mode]: https://www.playframework.com/documentation/2.4.2/PlayConsole#Running-the-server-in-development-mode
[Load-time weaving]: https://eclipse.org/aspectj/doc/released/devguide/ltw.html#ltw-introduction
[examples]: https://github.com/kamon-io/sbt-aspectj-runner/tree/master/examples
[here]:https://github.com/kamon-io/sbt-aspectj-runner/blob/master/aspectj-play-runner/src/main/scala/kamon/aspectj/sbt/task/PlayRunTask.scala#L38
[Load-time Weaving Requirements]:https://eclipse.org/aspectj/doc/released/devguide/ltw-rules.html
[master]:https://github.com/kamon-io/sbt-aspectj-runner/tree/master
[play-2.3.x]:https://github.com/kamon-io/sbt-aspectj-runner/tree/play-2.3.x

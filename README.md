sbt-aspectj-runner
=========
[![Build Status](https://travis-ci.org/kamon-io/sbt-aspectj-runner.png)](https://travis-ci.org/kamon-io/sbt-aspectj-runner)
[![Download](https://api.bintray.com/packages/kamon-io/sbt-plugins/sbt-aspectj-runner/images/download.svg)](https://bintray.com/kamon-io/sbt-plugins/sbt-aspectj-runner/_latestVersion)


This project contains two [sbt] plugins that automatically configure your build to perform [Load-time weaving] \(LTW\)
with Aspectj when running your application from within SBT, both for regular applications and Play Framework projects
[in development mode] and ensure that your aspects will always be woven as expected.

SBT versions 0.13 and 1.0 are supported.

## Why this plugin?

First and foremost, simplicity. Although adding the AspectJ Weaver agent is just about adding the `-javaagent` option
to the JVM, doing so can be challenging when running from SBT. These plugins take care of the corner cases and ensure
that hitting `run` will just work, regardless your project type or whether you are forking the JVM or not.



## Regular Projects (non-Play)

### Configuring

Add the `sbt-aspectj-runner` plugin to your `project/plugins.sbt` file using the code bellow:

```scala
resolvers += Resolver.bintrayRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon" % "sbt-aspectj-runner" % "1.1.1")
```

### Running

Just `run`, like you do all the time!

Here is what the plugin will do depending on your `fork` settings:
* **fork in run := true**: The forked process will run with the `-javaagent:<jarpath>` and that's all.
* **fork in run := false**: A custom classloader called [WeavingURLClassLoader] will be used. This classloader will
  perform the same load-time weaving duties done by the AspectJ Weaver agent.


## Play Projects

### Configuring

For Play Framework 2.6 projects add the `sbt-aspectj-runner-play-2.6` to your `project/plugins.sbt` file:

```scala
resolvers += Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon" % "sbt-aspectj-runner-play-2.6" % "1.1.1")

```

For Play 2.4 and 2.5 you can use the older `sbt-aspectj-play-runner` plugin:

```scala
resolvers += Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon" % "sbt-aspectj-play-runner" % "1.0.4")

```

This plugin has been tested with **Play 2.4.8**, **Play 2.5.10** and **Play 2.6.11**.

### Running

Just `run`, like you do all the time! A notice will be shown saying that you are running your application with the
AspectJ Weaver.

The Play Framework SBT plugin will not allow the JVM to be forked so this plugin will override the way class loaders are
created to use [WeavingURLClassLoader] instead, making sure that aspects will be woven when running on Development mode.





## Examples

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

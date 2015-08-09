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

import sbt._
import Keys._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
import Publish.{settings => publishSettings}
import Release.{settings => releaseSettings }

object Settings {

  lazy val basicSettings: Seq[Setting[_]] = Defaults.defaultSettings ++ Seq(
    sbtPlugin := true,
    resolvers ++= Dependencies.resolutionRepos,
    version <<= version in ThisBuild,
    scalacOptions  := Seq(
      "-encoding",
      "utf8",
      "-g:vars",
      "-feature",
      "-unchecked",
      "-optimise",
      "-deprecation",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-Yinline-warnings",
      "-Xlog-reflective-calls"
    )
  ) ++ publishSettings ++ releaseSettings

  lazy val formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test := formattingPreferences
  )

  def formattingPreferences =
    FormattingPreferences()
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(AlignParameters, false)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)

  //def baseVersions: Seq[Setting[_]] = SbtGit.versionWithGit
}
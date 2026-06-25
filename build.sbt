enablePlugins(SbtPlugin)
name         := "sbt-sass"
organization := "com.jamesward"

scalaVersion := "3.8.4"

crossScalaVersions := Seq("2.12.20", "3.8.4")

pluginCrossBuild / sbtVersion := {
  scalaBinaryVersion.value match {
    case "2.12" => "1.12.11"
    case _      => "2.0.0"
  }
}

addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0")

libraryDependencies ++= Seq(
  "de.larsgrefer.sass" % "sass-embedded-host"    % "4.4.0",
  "de.larsgrefer.sass" % "sass-embedded-bundled" % "4.4.0",
  "org.webjars"        % "webjars-locator-core"  % "0.59",
)

licenses := Seq("MIT License" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/jamesward/sbt-sass"))

developers := List(
  Developer(
    "jamesward",
    "James Ward",
    "james@jamesward.com",
    url("https://jamesward.com")
  )
)

javacOptions ++= Seq("-source", "17", "-target", "17")
scalacOptions ++= (scalaBinaryVersion.value match {
  case "2.12" => Seq.empty // Scala 2.12 cannot target > JDK 8
  case _      => Seq("-release", "17")
})

scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
scriptedBufferLog := false

versionScheme := Some("semver-spec")

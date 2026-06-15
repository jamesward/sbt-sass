sbtPlugin    := true
name         := "sbt-sass"
organization := "org.webjars"

scalaVersion := "2.12.20"

crossScalaVersions := Seq("2.12.20", "3.8.4")

pluginCrossBuild / sbtVersion := {
  scalaBinaryVersion.value match {
    case "2.12" => "1.12.11"
    case _      => "2.0.0"
  }
}

libraryDependencies ++= Seq(
  "de.larsgrefer.sass" % "sass-embedded-host"    % "4.4.0",
  "de.larsgrefer.sass" % "sass-embedded-bundled" % "4.4.0",
  // Required by dart-sass-java's bundled `WebjarsImporter` — not transitive
  // through `sass-embedded-host`'s pom even though the class hard-references it.
  "org.webjars"        % "webjars-locator-core"  % "0.59",
)

licenses := Seq("MIT License" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/webjars/sbt-sass"))

developers := List(
  Developer(
    "jamesward",
    "James Ward",
    "james@jamesward.com",
    url("https://jamesward.com")
  )
)

ThisBuild / versionScheme := Some("semver-spec")

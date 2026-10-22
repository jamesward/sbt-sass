lazy val check = taskKey[Unit]("Verify .sass (indented syntax) compilation output")

scalaVersion := "3.8.4"

libraryDependencies += "org.webjars.npm" % "bootstrap" % "5.3.8" % "sass"

check := {
  val files = sassCompile.value
  val names = files.map(_.getName).toSet
  assert(names.contains("app.css"), s"app.css not found in output: $names")
  assert(names.contains("plain.css"), s"plain.css not found in output: $names")
  assert(!names.exists(_.contains(".sass")), s"output named after .sass source: $names")
  assert(!names.contains("_theme.css"), s"partial compiled as entry: $names")
  val css = IO.read(files.find(_.getName == "app.css").get)
  assert(css.contains("#ff6600"), s"local .sass partial not applied:\n$css")
  assert(css.contains("color"), s"bootstrap variable not applied:\n$css")
}

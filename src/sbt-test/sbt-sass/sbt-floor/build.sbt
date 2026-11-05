lazy val check = taskKey[Unit]("Verify sass compilation output")

scalaVersion := "3.8.4"

libraryDependencies += "org.webjars.npm" % "bootstrap" % "5.3.8" % "sass"

check := {
  val cssFiles = sassCompile.value
  assert(cssFiles.nonEmpty, "sassCompile produced no files")
  val css = cssFiles.find(_.getName == "app.css")
  assert(css.isDefined, "app.css not found in output")
  val content = IO.read(css.get)
  assert(content.contains("color"), s"CSS does not contain expected 'color' rule")
}

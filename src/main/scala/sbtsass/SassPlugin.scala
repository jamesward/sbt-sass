package sbtsass

import sbt._
import sbt.Keys._
import sbt.librarymanagement.ConfigRef

import de.larsgrefer.sass.embedded.SassCompilerFactory

import java.net.URLClassLoader

/**
 * Compiles SCSS to CSS at build time using dart-sass-embedded (no Node).
 *
 * Adds a hidden `sass` Ivy configuration. WebJar dependencies declared with
 * the `Sass` scope are made available to SCSS via [[WebJarsScssImporter]],
 * so `@import "bootstrap/scss/functions";` resolves against the bootstrap
 * webjar JAR on the build classpath (trying SCSS suffix conventions like
 * `_functions.scss`).
 *
 * Generated CSS lands in Compile / resourceManaged / "public", which the
 * runtime serves under /assets/.
 *
 * The `% Set(Sass, ...)` syntax used in `build.sbt` for declaring Sass-scoped
 * WebJar deps is provided by the `sbt-webjars` plugin's `ModuleIDOps`
 * extension. Add `sbt-webjars` alongside this plugin if you want that
 * sugar; otherwise use the plain string form, e.g.
 *   `"org.webjars.npm" % "bootstrap" % "5.3.8" % "sass"`.
 */
object SassPlugin extends AutoPlugin {
  // Auto-enabled on every JVM project.
  override def trigger  = allRequirements
  override def requires = sbt.plugins.JvmPlugin

  object autoImport {
    val Sass = config("sass").hide

    val sassSource  = settingKey[File]("Root SCSS source dir")
    val sassTarget  = settingKey[File]("Output dir for compiled CSS")
    @transient val sassCompile = taskKey[Seq[File]]("Compile SCSS to CSS")
  }
  import autoImport._

  override def projectConfigurations: Seq[Configuration] = Seq(Sass)

  override def projectSettings: Seq[Setting[?]] =
    inConfig(Sass)(Defaults.configSettings) ++ Seq(
      ivyConfigurations += Sass,
      sassSource := (Compile / sourceDirectory).value / "scss",
      sassTarget := (Compile / resourceManaged).value / "public",

      sassCompile := {
        val log    = streams.value.log
        val srcDir = sassSource.value
        val outDir = sassTarget.value
        // Walk the resolved UpdateReport rather than `dependencyClasspath`
        // so the code compiles unchanged on both sbt 1.x (where `Classpath`
        // is `Seq[Attributed[File]]`) and sbt 2.x (where it is
        // `Seq[Attributed[HashedVirtualFileRef]]` and would need a
        // `FileConverter`). `ModuleReport.artifacts` stays `(Artifact, File)`
        // in both versions.
        val report = (Sass / update).value
        val cpJars: Seq[File] = report
          .configuration(ConfigRef(Sass.name))
          .toSeq
          .flatMap(_.modules.filterNot(_.evicted))
          .flatMap(_.artifacts.map(_._2))
          .distinct

        if (!srcDir.exists) {
          log.info(s"[sass] no SCSS sources at $srcDir, skipping")
          Seq.empty[File]
        } else {
          IO.createDirectory(outDir)
          val loader = new URLClassLoader(cpJars.map(_.toURI.toURL).toArray, getClass.getClassLoader)
          val compiler = SassCompilerFactory.bundled()
          try {
            compiler.registerImporter(new WebJarsScssImporter(loader))
            // Emit source maps so DevTools can show SCSS line numbers, and
            // inline the SCSS sources so the .map is self-contained (no extra
            // request per partial).
            compiler.setGenerateSourceMaps(true)
            compiler.setSourceMapIncludeSources(true)
            val entries = (srcDir ** "*.scss").get().filterNot(_.getName.startsWith("_"))
            entries.flatMap { src =>
              val rel = IO.relativize(srcDir, src).getOrElse(src.getName)
              val out = outDir / rel.stripSuffix(".scss").concat(".css")
              val map = file(out.getAbsolutePath + ".map")
              IO.createDirectory(out.getParentFile)
              val result = compiler.compileFile(src)
              // Append the sourceMappingURL trailer so browsers know where to
              // find the .map (sass-embedded does not emit it by default).
              IO.write(out, result.getCss + s"\n/*# sourceMappingURL=${map.getName} */\n")
              val sourceMap = Option(result.getSourceMap).filter(_.nonEmpty)
              sourceMap.foreach(IO.write(map, _))
              log.info(s"[sass] $src -> $out${sourceMap.fold("")(_ => s" (+ ${map.getName})")}")
              out +: sourceMap.map(_ => map).toSeq
            }
          } finally {
            compiler.close()
          }
        }
      },

      Compile / resourceGenerators += sassCompile.taskValue,
    )
}

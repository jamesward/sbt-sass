package sbtsass

import de.larsgrefer.sass.embedded.importer.ClasspathImporter
import org.webjars.{NotFoundException, WebJarAssetLocator}

import java.net.URL

/**
 * Resolves SCSS `@import` against WebJar JARs on the given classloader.
 *
 * Two cases:
 *
 *  1. **Artifact-prefixed imports** like `@import "bootstrap/scss/functions"`
 *     from your own SCSS — first path segment is the WebJar artifact name.
 *     Resolved via [[WebJarAssetLocator.getFullPath]].
 *
 *  2. **Relative imports** between SCSS files within a WebJar — once
 *     case 1 returns a `jar:file:` URL, dart-sass resolves subsequent
 *     `@import "variables-dark"` against the containing jar URL, producing
 *     `jar:file:.../scss/variables-dark`. We probe Sass suffix variants
 *     (`_name.{sass,scss,css}`, `name.{…}`, `name/_index.{…}`,
 *     `name/index.{…}`) on that URL.
 *
 * The bundled `WebjarsImporter` only handles exact filenames (no suffix
 * resolution), which is why we replace it.
 */
class WebJarsScssImporter(loader: ClassLoader) extends ClasspathImporter(loader) {
  private val locator = new WebJarAssetLocator(loader)

  override def canonicalizeUrl(url: String): URL = {
    if (url.startsWith("jar:") || url.startsWith("file:")) {
      probeUrl(url).orNull
    } else {
      val cleaned    = url.stripPrefix("classpath:").stripPrefix("/")
      val firstSlash = cleaned.indexOf('/')
      if (firstSlash > 0) {
        val artifact = cleaned.substring(0, firstSlash)
        val rest     = cleaned.substring(firstSlash + 1)
        val resolved = WebJarsScssImporter.candidates(rest).iterator.flatMap { variant =>
          try Option(locator.getFullPath(artifact, variant))
          catch { case _: NotFoundException => None }
        }.find(loader.getResource(_) != null)
        resolved.map(loader.getResource).orNull
      } else null
    }
  }

  /** Try Sass suffix variants on an absolute URL (jar:/file:). */
  private def probeUrl(base: String): Option[URL] = {
    // First candidate that opens cleanly wins. Returning the first matching
    // element of an `Iterator[URL]` cross-compiles between Scala 2.12 (no
    // `nextOption`) and Scala 3 (no `toStream`) via `find(_ => true)`.
    WebJarsScssImporter.candidates(base).iterator.flatMap { c =>
      try {
        val u    = new java.net.URI(c).toURL
        val conn = u.openConnection()
        conn.setUseCaches(false)
        val is = conn.getInputStream
        is.close()
        Some(u)
      } catch {
        case _: Throwable => None
      }
    }.find(_ => true)
  }
}

object WebJarsScssImporter {
  /** Extensions Sass loads, in its resolution order: indented, SCSS, plain CSS. */
  private val extensions = Seq("sass", "scss", "css")

  /**
   * Sass file-resolution order applied to a path (relative or absolute URL):
   * partial and non-partial files for each extension, then `_index` / `index`
   * files inside a directory of that name.
   */
  def candidates(path: String): Seq[String] = {
    val i = path.lastIndexOf('/')
    val (dir, name) =
      if (i < 0) ("", path)
      else (path.substring(0, i + 1), path.substring(i + 1))
    def withExts(base: String) = extensions.map(ext => s"$base.$ext")
    withExts(s"${dir}_$name") ++
      withExts(s"$dir$name") ++
      withExts(s"$dir$name/_index") ++
      withExts(s"$dir$name/index")
  }
}

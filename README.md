# sbt-sass

An sbt plugin that compiles SCSS to CSS at build time using
[dart-sass-embedded](https://github.com/sass/dart-sass-embedded) (no Node
required), with a custom importer that resolves `@import` statements
against [WebJar](https://www.webjars.org/) JARs on the build classpath.

Supports cross-publishing for **sbt 1.x** and **sbt 2.x**.

## Install

In `project/plugins.sbt`:

```scala
addSbtPlugin("org.webjars" % "sbt-sass" % "<version>")
```

The plugin is auto-enabled on every JVM project — you do **not** need
`enablePlugins(SassPlugin)`.

If you also want the `% Set(Sass, WebJar, Test)` syntax shown below for
multi-scoped WebJar dependencies, add
[`sbt-webjars`](https://github.com/webjars/sbt-webjars) alongside it. Without
`sbt-webjars`, declare Sass-scoped deps with the plain string form
(`% "sass"`).

## Use

1. Put your SCSS sources in `src/main/scss` (configurable via `sassSource`).
2. Declare WebJar dependencies with the `Sass` scope (and optionally
   `WebJar` / `Test` if you also use `sbt-webjars`):

   ```scala
   // With sbt-webjars's Set(...) sugar:
   libraryDependencies +=
     "org.webjars.npm" % "bootstrap" % "5.3.8" % Set(WebJar, Sass, Test)

   // Or, with plain sbt configuration strings:
   libraryDependencies +=
     "org.webjars.npm" % "bootstrap" % "5.3.8" % "sass"
   ```

3. Author SCSS that imports from those WebJars — the artifact name is the
   first path segment:

   ```scss
   // src/main/scss/app.scss
   @import "bootstrap/scss/bootstrap";
   ```

4. CSS is generated into `Compile / resourceManaged / "public"` and added
   to the resource generators automatically, so it ends up on the
   classpath at runtime under `/public/...`. Source maps are emitted next
   to the CSS, with the SCSS sources inlined so DevTools can show them
   without extra requests.

## Settings & tasks

| Key             | Type             | Default                                          |
|-----------------|------------------|--------------------------------------------------|
| `Sass`          | `Configuration`  | hidden Ivy config the plugin adds                |
| `sassSource`    | `File`           | `src/main/scss`                                  |
| `sassTarget`    | `File`           | `Compile / resourceManaged / "public"`           |
| `sassCompile`   | `Seq[File]`      | task that compiles SCSS, wired into resources    |

`sassCompile` is added to `Compile / resourceGenerators`, so a normal
`compile` (or `package`, `run`, etc.) will produce CSS without extra
configuration. If you'd like to invoke it directly:

```
sbt sassCompile
```

## How `@import` resolution works

`WebJarsScssImporter` overrides dart-sass's `ClasspathImporter` so it can
walk the `META-INF/resources/webjars/<artifact>/<version>/` paths inside
WebJar JARs. It tries the standard SCSS file-resolution order for each
import:

1. `_<name>.scss`
2. `<name>.scss`
3. `<name>/_index.scss`
4. `<name>/index.scss`

For artifact-prefixed imports (the first path segment is a WebJar
artifact name) it uses
[`WebJarAssetLocator`](https://github.com/webjars/webjars-locator-core)
to find the right path inside the jar. For relative imports between SCSS
files within a WebJar (which dart-sass canonicalizes against the
containing `jar:file:` URL) it probes those same suffix variants on the
absolute URL.

## Development

```
./sbt +compile     # cross-build for sbt 1.x and sbt 2.x
./sbt +test        # run tests across both
./sbt +publishLocal
```

The `+` prefix triggers cross-building across `crossScalaVersions`, which
in turn drives `pluginCrossBuild / sbtVersion` to flip between
`1.12.x` / Scala 2.12 and `2.0.x` / Scala 3.

## License

[MIT](https://opensource.org/licenses/MIT)

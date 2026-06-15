# sbt-sass

An sbt plugin that compiles SCSS to CSS at build time using
[dart-sass-embedded](https://github.com/sass/dart-sass) (no Node required),
with a custom importer that resolves `@import` / `@use` statements against
[WebJar](https://www.webjars.org/) JARs on the build classpath.

Supports **sbt 1.x** and **sbt 2.x**.

## Install

In `project/plugins.sbt`:

```scala
addSbtPlugin("com.jamesward" % "sbt-sass" % "<version>")
```

The plugin auto-enables on every JVM project — no `enablePlugins(...)` needed.

## Usage

1. Put SCSS sources in `src/main/scss/`:

   ```
   src/main/scss/app.scss
   ```

2. Add WebJar dependencies scoped to `"sass"`:

   ```scala
   libraryDependencies += "org.webjars.npm" % "bootstrap" % "5.3.8" % "sass"
   ```

   Or, if you also use [sbt-webjars](https://github.com/webjars/sbt-webjars):

   ```scala
   libraryDependencies += "org.webjars.npm" % "bootstrap" % "5.3.8" % Set(WebJar, Sass, Test)
   ```

3. Import from WebJars using the artifact name as the first path segment:

   ```scss
   // src/main/scss/app.scss
   @import "bootstrap/scss/bootstrap";
   ```

4. Run `compile` — CSS appears at runtime on the classpath under `public/`:

   ```
   target/.../resource_managed/main/public/app.css
   target/.../resource_managed/main/public/app.css.map
   ```

   Source maps are emitted with SCSS sources inlined for DevTools.

## Settings & Tasks

| Key           | Type        | Default                                |
|---------------|-------------|----------------------------------------|
| `Sass`        | `Configuration` | Hidden Ivy config added by the plugin |
| `sassSource`  | `File`      | `src/main/scss`                        |
| `sassTarget`  | `File`      | `Compile / resourceManaged / "public"` |
| `sassCompile` | `Seq[File]` | Task that compiles SCSS → CSS          |

`sassCompile` is wired into `Compile / resourceGenerators`, so `compile`,
`run`, `package`, etc. all trigger SCSS compilation automatically.

## How WebJar imports work

The plugin ships `WebJarsScssImporter`, which resolves imports using
[webjars-locator-core](https://github.com/webjars/webjars-locator-core).
For each import it tries the standard SCSS file-resolution order:

1. `_<name>.scss`
2. `<name>.scss`
3. `<name>/_index.scss`
4. `<name>/index.scss`

This means `@import "bootstrap/scss/functions"` resolves to
`_functions.scss` inside the bootstrap WebJar JAR — just like a
local Sass install would.

## Development

```bash
./sbt +compile        # cross-build for sbt 1.x and 2.x
./sbt scripted        # run integration tests
./sbt +publishLocal   # install locally for both sbt versions
```

## License

[MIT](https://opensource.org/licenses/MIT)

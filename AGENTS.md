# AGENTS.md: sbt-sass

## Agent tooling

- Follow the `zen-of-projects` Skill (extract with `./sbta extractSkillsJars` into the gitignored `.kiro/skills/`); this file records only project-specific facts and exceptions.
- For Scala 3 and ZIO code (writing or reviewing it), follow `zen-of-scala`.
- For design, domain modeling, effects and testing strategy, follow `zen-of-james`.
- Read the relevant Skills in `.kiro/skills/` before working.
- MCP server `sbta` (this build's `./sbta --mcp`, configured in `.mcp.json` and `.kiro/settings/mcp.json`, approved in `.claude/settings.json`): use it for all sbt work: `compile`, `test` (`full` for all), `run`, `check` after editing Scala 3 sources, `glob-search` / `inspect` / `symbol-location` for symbols, `sbt-task` for any other command. Its tools are deferred in Claude Code: load them with ToolSearch (search `sbta`). If it's unavailable, say so and use `./sbta <command>`. Logs: `project/target/sbta/mcp.log`.
- Maintenance routine: `.factory/MAINTENANCE.md` (weekly), following the `zen-of-projects` Skill.
- Validation: `./sbta "Test / compile; testFull; +scripted"` (sbt plugin; no `stage`). CI runs `+compile; +scripted` too: plain `scripted` runs only the sbt 2 leg, and every fixture pins sbt 1, so it skips them all. Through sbt-mcp run `+scripted` as its own call: the cross-build switch reloads sbt and drops the connection.
- Scala 3.8.4 is deliberately pinned to the `scala3-library_3` version of the pinned sbt (plugin compatibility rule); don't bump it on its own.

## Supported sbt versions

This plugin supports sbt 1 and sbt 2 and keeps both: most builds that use it are still on sbt 1, and an sbt 2-only plugin can't be added to them.

`pluginCrossBuild / sbtVersion` in `build.sbt` sets the sbt each published artifact compiles against, which is the oldest sbt its users can run (zen-of-projects "sbt Plugin Projects"). These are floors, not dependencies: the maintenance routine leaves them alone.

- **sbt 1 (Scala 2.12): `1.9.0`.** The oldest sbt 1 that can use this plugin at all: it depends on `sbt2-compat`, whose sbt 1 build needs 1.9.0 or later (older sbt can't resolve Maven-style plugin artifacts from Maven Central), and sbt2-compat's own setup uses this floor. The scripted test `sbt-floor` (a copy of `simple` with `sbt.version=1.9.0`) runs the plugin on it; keep that fixture's sbt at the floor when the maintenance routine bumps the others.
- **sbt 2 (Scala 3): `2.0.0`**, the first sbt 2 release, so every sbt 2 user can use it. The scripted test `sbt2` (a copy of `simple` with `sbt.version=2.0.0`) runs the plugin on it; keep that fixture's sbt at the floor too.

Raise a floor only when the plugin needs an API the floor lacks: say which in the PR, label it `needs-human` (it drops users on older sbt), and update this section.


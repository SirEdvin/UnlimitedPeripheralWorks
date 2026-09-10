# Unlimited Peripheral Works

## Overview

Unlimited Peripheral Works is a Minecraft 1.20.1 mod that adds peripherals and integrations for CC:Tweaked. It supports both Fabric and Forge from a shared core module.

## Tech Stack

- Runtime: Minecraft 1.20.1 / Java 17
- Languages: Kotlin 2.0, Java, TypeScript, and Lua
- Loaders: Fabric Loader 0.17 and Forge 47
- Computer mod: CC:Tweaked 1.116
- Testing: Testiarium GameTests and TypeScript-to-Lua fixtures
- Build system: Gradle Wrapper

## Key Commands

Log handling is important. Always use an explicit timeout and silently save complete output.

- Build: `mkdir -p build; LOG="build/gradle-$(date +%Y%m%d-%H%M%S).log"; timeout --foreground 10m ./gradlew build --no-daemon >"$LOG" 2>&1`
- GameTests: `mkdir -p build; LOG="build/gametests-$(date +%Y%m%d-%H%M%S).log"; timeout --foreground 20m xvfb-run -a ./gradlew gameTest --no-daemon -PminimalTestEnvironment >"$LOG" 2>&1`
- TypeScript fixtures: `mkdir -p build; LOG="build/typescript-tests-$(date +%Y%m%d-%H%M%S).log"; timeout --foreground 5m ./gradlew :typescript-tests:compileTestLua --no-daemon >"$LOG" 2>&1`

Increase timeouts only when required. Stop development clients and servers after collecting results. Report the command, exit code, duration, log path, and relevant errors; inspect only the relevant failure window.

## Project Structure

```text
projects/
  core/              # Shared implementation, resources, and GameTests
  fabric/            # Fabric integration and test mod
  forge/             # Forge integration and test mod
  typescript-tests/  # TypeScript sources compiled to ComputerCraft Lua fixtures
gradle/               # Version catalog and Gradle configuration
openspec/             # Change specifications and implementation plans
```

Production code lives in `src/main/`. Shared GameTests and fixtures live in `projects/core/src/testMod/`; loader test-mod entry points and metadata live in each loader's `src/testMod/`.

## Conventions

- Keep loader-independent behavior in `projects/core/`.
- Keep loader API usage in the corresponding `fabric` or `forge` module.
- Follow the official Kotlin code style configured in `gradle.properties`.
- Reuse existing project patterns before adding helpers, abstractions, or dependencies.
- Comments explain why, not what.

## OpenSpec Delivery Workflow

- Before applying an OpenSpec change, create a new feature branch from the intended base branch.
- Implement, verify, commit, and push the change only on that feature branch; never push OpenSpec feature work directly to the base or release branch.
- Open a pull request targeting the intended base branch and report its URL for review instead of merging it directly.
- Only bypass this workflow when the user explicitly requests a direct push for that specific OpenSpec change.

## DO NOT MODIFY

- Never edit generated build output; change its source and rerun the relevant Gradle task.
- Never commit `build/`, development run directories, logs, EULA files, or `node_modules/`.
- Do not modify unrelated user changes in a dirty worktree.

## Testing Approach

- Run `:typescript-tests:compileTestLua` after changing TypeScript fixtures.
- Run the root `gameTest` task for both Fabric and Forge GameTests, using `xvfb-run` for graphics-dependent Minecraft startup.
- Run the timed multi-loader build before marking code changes complete.
- Fix failing tests rather than skipping them.

## Code Style

- Prefer the smallest correct change.
- Do not add speculative abstractions or dependencies.
- Keep shared and loader-specific responsibilities separated.
- Preserve validation, error handling, and dedicated-server safety.
- If requirements are unclear, ask instead of assuming.

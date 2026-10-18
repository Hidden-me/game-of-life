# GameOfLife

A finite-grid Game of Life built with [libGDX](https://libgdx.com/) and Java 25.

Version 0.0.1 uses a 127 × 127 grid with finite boundaries. Click a cell to toggle it while editing, or drag the grid to pan. The bottom controls run, pause, step, reset and clear the simulation. Run advances once every 500 ms; the dot indicates editing (cyan) or running (amber). The question mark opens the rules and controls page.

Reset restores the last edited state. Clicking cells, stepping and clearing update this baseline. Starting a run records the current grid; automatic steps and pausing preserve that record.

Use a Java 25 JDK. To launch the desktop application or verify the project:

```powershell
.\gradlew.bat :lwjgl3:run
.\gradlew.bat test build
```

The build produces `lwjgl3/build/libs/GameOfLife-0.0.1.jar`, which can also be launched with `java -jar` on Java 25.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

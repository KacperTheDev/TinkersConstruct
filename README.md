# [Tinkers' Construct (Unofficial 1.21.1 NeoForge Port)](https://www.curseforge.com/minecraft/mc-mods/tinkers-construct-unofficial-port)

Modify all the things, then do it again!    
Melt down any metals you find.   
Power the world with spinning wind!

This is an **unofficial port** of Tinkers' Construct to Minecraft 1.21.1 using the NeoForge ecosystem. 

## Dependencies
* Requires **[Mantle (Unofficial Port)](https://www.curseforge.com/minecraft/mc-mods/mantle)** version `1.11.104` or higher.

## Documentation
For documentation on writing addons or working with Tinkers' Construct datapacks, see the pages on the SlimeKnight's Github.io pages: https://slimeknights.github.io/docs/

## Setting up a Workspace/Compiling from Source

Note: Git MUST be installed and in the system path to use our scripts.
* **Setup:** Import Tinkers' Construct as a NeoForge Gradle project into IntelliJ IDEA. Let the initial Gradle import complete.
* **Run:** Refresh/reload the Gradle project to automatically generate NeoForge run configurations.
* **Build:** Run `gradlew build`.
* If obscure Gradle issues are found, try running `gradlew clean` and `gradlew cleanCache`.

## Issue reporting
Since this is an unofficial port, **do not report bugs to the official SlimeKnights repository**. Please open issues directly on the [KacperTheDev/TinkersConstruct](https://github.com/KacperTheDev/TinkersConstruct/issues) issue tracker.

When reporting issues, please include:
* Tinkers' Construct version (this port build)
* NeoForge version
* Full `latest.log` or crash report from the root folder
* Steps to reproduce the bug

## Licenses & Credits
* Original Code, Textures, and Binaries by SlimeKnights (licensed under the **MIT License**).
* Ported and maintained by **KacperTheDev**.

Any modpack which uses this unofficial build takes **full** responsibility for user support queries. Do not bother the official developers with issues regarding this version.

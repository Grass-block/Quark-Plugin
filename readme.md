
<div align="center" id="readme-top">

<img width="160" style="border-radius: 1rem" src="https://pic2.imgdd.cc/item/68da84af8dc72b176e74b81f.png">

<h2 align="center">Starlight -「星辰」</h2>

An 'Atomic' designed server plugin aimed to cover anything you need.

[Explore docs](https://wiki.atforever.world/quark/) | [Report issue](https://github.com/Grass-block/Quark-Plugin/issues)

![MCVersion](https://img.shields.io/badge/minecraft-1.17.1_--_1.20.4-3366CC?style=for-the-badge&logoColor=blue&labelColor=29355F)
![Java17+](https://img.shields.io/badge/java-17+-009B98?style=for-the-badge&logoColor=blue&labelColor=29355F)
![MohistCompat](https://img.shields.io/badge/Mohist-Compatible-AD3333?style=for-the-badge&logoColor=blue&labelColor=29355F)

</div>

**This project is still under development and inconsistency.**

### Description

- A management plugin...
- A set of toolbox and some randomly features...
- A set of game features add and tweaks...

This is an essential plugin suite for Spigot/Paper/Folia/Mohist servers,
including more than 90 modules that provides countless features for servers of any scale!
This plugin also includes several performance enhancements and fixes.
From management to display,game features to security,
these contents will improve your server from every aspect.

#### Possible Usages

- create a SMP server.
- create a CMP server with WorldEdit and CoreProtect.
- create a Lobby server with Citizens and sign-in plugins.

All the features are independently and composable.
you can customize its function by enable or disable modules,
add or remove extension packs,
or even make your own pack through sdk pack.

#### Core Features

- MultiPlatform support: supports basically every Bukkit-Implemented Server(see below)
- Fast: initialize on less than 600ms, support command-line hot reload.
- Modularized: all features are separately toggleable.

### Compatibility

> We STRONGLY recommend you to use paper based server to enable full features.
> it could work on other platform, but who knows what will happen? (since lazy GrassBlock2022 never test them)

> Versions showed in versions page are just for placeholder. all versions can work in [1.13-1.20]

| Platform | Features | Interactive Text | Support | Description                    |
|----------|----------|------------------|---------|--------------------------------|
| Paper    | Full     | Full             | Full    | Recommended                    |
| Spigot   | Most     | Full             | BugFix  |                                |
| Bukkit   | Few      | No               | Test    |                                |
| Folia    | Most     | Full             | BugFix  | No hot reload                  |
| Mohist   | Few      | No               | Test    | UnexpectedBlockChange not sync |

### FaQ

#### What relationship does it have to that `Starlight` mod?

Basically, nope :D<br/>
Starlight mod has a totally different function from this artifact.It rewrites light engine and simply provide optimizes.

### About Rename

#### Why it is renamed?
This project is originally name `quark-plugin`. To avoid being collided with that mod——which everybody loves it, I decide to change its name.

But the reason is not only for this. This project actully starts from long long long long ago, from a small plugin. The most bad thing is that when I started this project, I actually have **NO**
understandings to java.

So now, when this plugin is getting more appreciation, start off from the blank and refactor the WHOLE archtecture to ensure extandability is important.

#### What will happen?
Unfortunately, to wholly frick the architecture away, I HAVE to sacrifice A lot, especially, compatibility.
from the following version, This project will enter alpha stages.The plugin will use a completely different namespace and ID.

As soon as the refactor done, the auto-migaration code will be added, to allow you to upgrade **Player Data and module data** from old plugin data folder, and the new starlight plugin can work with the old one together.

#### Help us test
Alpha stage goes with inconsistentcy, so it's now for you to offer help. Please submit any bugs or feature requests to github link above, thank you sooooo much.

#### Working with quark-plugin

The new starlight kernal now register things in a different namespace, but feature conflicts still exists. You have to manually disable modules that has duplicated feature with starlight in your older plugin.

#### Upgrading and migration

Currently, to port all modules and functions to new kernal system as fast as possible, data compatibility is not primary tasks. Once beta test stage is completed, data migration command will be added to let you move:
- Stored player data(like some of their personal settongs like view distance)
- Stored global data(waypoints…)
- Some of stored assets(permission data, midi musics…)

Unfortunately, since most of ids and values are changed, it's not possible to move or migrate legacy language or config file, so please re-configure them once it's ready.

<hr/>
<div align="center">

#### Starlight -「星辰」

A open project by GrassBlock2022, owning by @ATCraftMC 2020-2024
</div>

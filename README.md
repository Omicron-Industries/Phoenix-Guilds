<h1 align="center">
  <img src="https://raw.githubusercontent.com/Omicron-Industries/Phoenix-Guilds/main/guilds.png" alt="PhoenixGuilds" width="256" height="256">
</h1>

<p align="center">
  <strong>Teamwork is the backbone of all modern infrastructure.
   However, it's incredibly simple... to some.
</strong>
</p>

<p align="center">
  <a href="https://www.curseforge.com/minecraft/mc-mods/phoenix-guilds">
    <img alt="CurseForge" height="50" src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/available/curseforge_vector.svg"></a>
  <a href="https://discord.gg/4jch9Rs2Cq">
    <img alt="Discord" height="50" src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/social/discord-singular_vector.svg"></a>
  <a href="https://ko-fi.com/phoenixvine">
    <img alt="Ko-fi" height="50" src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/donate/kofi-singular_vector.svg"></a>
</p>



# Phoenix Guilds
**Guilds** is a simple teams mod with simple-to-use *API*, *UI*, a customizable *per team flag*, and integration with **GregTech:Modern**.

It is designed to integrate with other **PhoenixSuite** mods but works quite well with other mods that want teams. Currently, there is only direct support for **GT:M** 
but if other mods need specific compat, that would be *readily considered*. 

Moreover, **Guilds** serves as a foundation to almost everything a *player* does, so it must be stable. 
If you see any issues with the **Guilds** *api* please report it; they help us a lot!

More information can be found below! And if not? We would be happy to continue to improve both the mod and the included information.

## Wiki link
We have a small in progress wiki for all PhoenixSuite mods, if it is missing any important info or you would like to help,
feel free to ping me on discord by the username of Phoenixvine.

[Wiki](https://omicron-industries.github.io/PhoenixSuite/wiki/)

# Why should you use Guilds over other team mods?
While other team mods are quite stable and easy to use; we still have plenty of localized insanities to make this a phoenix mod!
The greatest benefits over other teams mods would be the flags system and the themable UI, so you can spread your pride in whichever way you desire.

Oh, you don't want to show off? Put a candle or something on the flag; we don't care!

# Getting started as a dev and/or packdev.
If you are a packdev you don't have to do anything special to get guilds up and running! Just plop the mod in, create a guild through the UI, and you're off!

However, if you are a dev (or a more involved packdev with a core-mod), you need to:
- Depend on the mod using either cursemaven or the repsy repository.

```
    //Firstly, add the following to your mavens section depending on which vendor you choose.
    maven {
            url "https://cursemaven.com/"
            content {
                includeGroup "curse.maven"
            }
        }
        
    maven {
        name = "Repsy"
        url = uri("https://repo.repsy.io/mvn/user75142941/phoenixsuite")
    }
    
    
    // Now, you can add it as a dependency.
    dependencies {
    modImplementation("net.phoenixvine.wiki:phoenix_wiki:0.2.8")
    }
```


- Check 

# Major feature list.
## Will probably contain some pictures.

# Roadmap

# Explaining how it fits into the rest of the suite.

# Small snippt of the markdown of quests.

# Claiming to be an omind project.

# Credits

# Ai disclosure.

# Discord link.

# Where to go next.
Contributing
See CONTRIBUTING.md.

Architecture Decisions
See Architecture.md.

Frequently Asked Questions
See FAQ.md

Known Issues
See KNOWN-ISSUES.md
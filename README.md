# Klassenserver7bBot
[![CodeFactor](https://www.codefactor.io/repository/github/klassenserver7b/klassenserver7bbot/badge)](https://www.codefactor.io/repository/github/klassenserver7b/klassenserver7bbot)
[![License](https://img.shields.io/github/license/klassenserver7b/Klassenserver7bBot.svg)](https://github.com//klassenserver7b/Klassenserver7bBot/blob/master/LICENSE)
[![Build status](https://ci.appveyor.com/api/projects/status/k70t7pfha0dbcvo2?svg=true)](https://ci.appveyor.com/project/klassenserver7b/Klassenserver7bBot)

## Install

1. Download bot.jar (Packed by eclipse `Export as runnable JAR-File`) and logback.xml and put them in the same `BOT_DIRECTORY`
2. Download [Java 17](https://www.azul.com/downloads/?version=java-17-lts&package=jre)
3. Run the Bot using `java -jar PATH_OF_YOUR_Bot.jar` in the `YOUR_JAVA_17/bin` directory
4. Insert your Tokens in the Autogenerated  `BOT_DIRECTORY/resources/bot.properties`
5. Add the Bot to your Server and have fun

## Self Compile

1. (Download/Use [Java 17](https://www.azul.com/downloads/?version=java-17-lts&package=jdk))
2. Use Maven Compiler and the submitted pom.xml to compile the complete `DIRECTORY/src/de/k7bot` directory.
3. The current "Main"-Class is `Klassenserver7bbot` - In a further release this will change to `Main`

## Support

**The `friendly exceptions` and `Error on Trackloading` are not my fault and are also listed in the Lavaplayer Issuses**

### You can contact me via

- This [GitHub-Repo](https://github.com/klassenserver7b/Klassenserver7bBot/) and my [GitHub Account](https://github.com/klassenserver7b/)
- Discord: "Klassenserver 7b#0380"
- [Discord Server](https://discord.gg/EdKD5FE)
- E-Mail: "klassenserver7bwin10@gmail.com"

### For those who want to develop themselves

**Creating a "normal" Discord Chat-Command:**

1. Create a new command by creating a new class and adding `implements ServerCommand` or `implements HypixelCommand` (whether it is a Music/Tool/Moderation command or depends it on "Hypixel" and their API)
2. Insert new commands in the [CommandManager](https://github.com/klassenserver7b/Klassenserver7bBot/blob/master/src/de/k7bot/manage/CommandManager.java) or the [HypixelCommandManager](https://github.com/klassenserver7b/Klassenserver7bBot/blob/master/src/de/k7bot/hypixel/HypixelCommandManager.java)

**Creating a SlashCommand**

1. Create a new command by creating a new class and adding `implements SlashCommand`
2. Insert new commands in the [SlashCommandManager](https://github.com/klassenserver7b/Klassenserver7bBot/blob/master/src/de/k7bot/manage/SlashCommandManager.java)
3. Add your required options in the [SlashCommandManager](https://github.com/klassenserver7b/Klassenserver7bBot/blob/master/src/de/k7bot/manage/SlashCommandManager.java) and your option-previews in [ChartsAutoComplete](https://github.com/klassenserver7b/Klassenserver7bBot/blob/master/src/de/k7bot/listener/ChartsAutocomplete.java)

### A DiscordBot by @Klassenserver7b

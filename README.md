<table align="center"><tr><td align="center" width="10000">
<img src="pageBin/logo.png" align="center" width="150" alt="logo">

# TuningFork

[![Release](https://jitpack.io/v/Hangman/TuningFork.svg)](https://jitpack.io/#Hangman/TuningFork)
[![Workflow](https://github.com/Hangman/TuningFork/actions/workflows/gradle.yml/badge.svg)](https://github.com/Hangman/TuningFork/actions/workflows/gradle.yml/badge.svg)
[![WorkflowNative](https://github.com/Hangman/TuningFork/actions/workflows/build_natives.yml/badge.svg)](https://github.com/Hangman/TuningFork/actions/workflows/build_natives.yml/badge.svg)
[![Versioning](https://img.shields.io/badge/semver-2.0.0-blue)](https://semver.org/)

</td></tr></table>

## Introduction
TuningFork is a library for [libGDX](https://github.com/libgdx/libgdx) that provides advanced audio features. The goal was to make most of the features of [OpenAL](https://github.com/kcat/openal-soft) accessible and provide a comfortable, easy to use, low overhead and object oriented high-level API. In addition, the library offers some convenience features that are needed for most games, such as the music player.<br>
Note that TuningFork is not an extension to libGDX audio but a replacement.

### Main Features
* Spatial 3D and 2D audio
* Directional audio (3D)
* Real-time effects such as Reverb, Echo, Flanger, Distortion (and many more)
* Filters
* Music player with support for playlists and fading
* Loop-points
* HRTF support (aka binaural)
* Streaming is handled on a background thread
* Load **wav**, **aiff**, **ogg**, **flac**, **mp3** ([see the full list](https://github.com/Hangman/TuningFork/wiki/Supported-audio-formats-and-codecs))
* Supports surround sound formats
* [AssetManager](https://libgdx.com/wiki/managing-your-assets) integration
* Play raw PCM data
* Record audio

### Limitations
* Desktop only

### Why
The standard audio capabilities of libGDX are very limited compared to sound APIs like OpenAL - probably due to its cross-platform nature. Especially if you are developing a 3D application and want to create realistic spatial sound, use features like HRTF etc., you'll face some problems with libGDX.

## Install
TuningFork is available via Jitpack.
First make sure you have Jitpack declared as repository in your root build.gradle file:
```groovy
allprojects {
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

Then add TuningFork as dependency in your core project: 

```groovy
project(":core") {
    dependencies {
    	// ...
        implementation 'com.github.Hangman:TuningFork:4.1.0'
    }
}
```
### Compatibility
Java 8 is required, make sure to set `sourceCompatibility = JavaLanguageVersion.of(8)` (or higher) in your gradle scripts.
| Version of libGDX   | Latest compatible version of TuningFork  |
|      :----:         | :---                                     |
| 1.12.1              | 4.1.0                                    |
| 1.12.0              | 4.1.0                                    |
| 1.9.12 - 1.11.0     | 3.3.0                                    |
| 1.9.11              | 2.0.1                                    |
| < 1.9.11            | not supported, might work though: 2.0.1  |

### Upgrading
This library follows [semantic versioning](https://semver.org/). Breaking changes are indicated by a major version increase.
You can find the release notes [here](https://github.com/Hangman/TuningFork/wiki/Patch-Notes).

## Getting Started
[Go to the wiki](https://github.com/Hangman/TuningFork/wiki)

## Building From Source
To build TuningFork from source, you need a JDK >= 8 installed. Like [Adoptium JDK](https://adoptium.net/) for example.  
TuningFork uses [Gradle](https://gradle.org/) as it's build tool (you don't need to have Gradle installed).
```console
./gradlew build
```
Compiles the library. The resulting jar is located under `core/build/libs/`.
```console
./gradlew publishToMavenLocal
```
Publishes the core artifact to your local Maven repository.

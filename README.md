<table align="center"><tr><td align="center" width="10000">
<img src="pageBin/logo.png" align="center" width="150" alt="logo">

# TuningFork

[![Release](https://jitpack.io/v/Hangman/TuningFork.svg)](https://jitpack.io/#Hangman/TuningFork)
[![Workflow](https://github.com/Hangman/TuningFork/actions/workflows/gradle.yml/badge.svg)](https://github.com/Hangman/TuningFork/actions/workflows/gradle.yml/badge.svg)
[![WorkflowNative](https://github.com/Hangman/TuningFork/actions/workflows/build_natives.yml/badge.svg)](https://github.com/Hangman/TuningFork/actions/workflows/build_natives.yml/badge.svg)
[![Javadoc](https://img.shields.io/badge/JavaDoc-Online-green)](https://Hangman.github.io/TuningFork/javadoc/)
[![Versioning](https://img.shields.io/badge/semver-2.0.0-blue)](https://semver.org/)

</td></tr></table>

## Introduction

TuningFork is a library for [libGDX](https://github.com/libgdx/libgdx) that provides advanced audio features. The goal
was to make most of the features of [OpenAL Soft](https://github.com/kcat/openal-soft) accessible and provide a
comfortable, easy to use, low overhead and object-oriented high-level API. In addition, the library offers some
convenience features that are needed for most games, such as the music player.<br>
Note that TuningFork is not an extension to libGDX audio but a replacement.

### Main Features

* Spatial 3D and 2D audio
* Directional audio (3D)
* Real-time effects such as Reverb, Echo, Flanger, Distortion (and many more)
* Loop-points
* Filters
* HRTF
* Music player with support for playlists and fading
* Streaming is handled on a background thread
* [AssetManager](https://libgdx.com/wiki/managing-your-assets) integration
* Play raw PCM data
* Record audio

### Supported Formats

Check out the [full list](https://github.com/Hangman/TuningFork/wiki/Supported-audio-formats-and-codecs) for precise
information about format and codec support.

* wav
* ogg
* mp3
* flac
* aiff
* [qoa](https://qoaformat.org/)

### Limitations

* Desktop only

I didn't want to make any compromises by finding the lowest common denominator between platforms and instead give access
to the full feature-set of OpenAL Soft + more.

## Install

Add TuningFork as a dependency to your project (I recommend to add it in the core project if you develop for desktop
only):  
[![Release](https://jitpack.io/v/Hangman/TuningFork.svg)](https://jitpack.io/#Hangman/TuningFork)

```groovy
implementation 'com.github.Hangman:TuningFork:4.4.0'
```

### Compatibility

Java 8 is required, make sure to set `sourceCompatibility = JavaLanguageVersion.of(8)` (or higher) in your gradle
scripts.
| Version of libGDX | Latest compatible version of TuningFork |
|      :----:         | :--- |
| 1.12.1 - 1.13.5 | 4.4.0 |
| 1.12.0 | 4.1.0 |
| 1.9.12 - 1.11.0 | 3.3.0 |
| 1.9.11 | 2.0.1 |
| < 1.9.11 | not supported, might work though: 2.0.1 |

### Upgrading

This library follows [semantic versioning](https://semver.org/). Breaking changes are indicated by a major version
increase.  
You can find the release notes [here](https://github.com/Hangman/TuningFork/wiki/Patch-Notes).

## Getting Started

The wiki should provide all the information you need to get started quickly. If something is missing, please open an
issue.  
[Link to the Wiki!](https://github.com/Hangman/TuningFork/wiki)

## Building From Source

To build the library from source, you need a JDK >= 8 installed. You can grab one
here: [Liberica JDK](https://bell-sw.com/pages/downloads/).  
TuningFork uses [Gradle](https://gradle.org/) as it's build tool (you don't need to have Gradle installed).  
<br>
This will compile the library. The resulting jar can be found under `core/build/libs/`.

```console
./gradlew build
```

This will publish the core artifact to your local Maven repository.

```console
./gradlew publishToMavenLocal
```

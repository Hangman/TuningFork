# ![logo](https://github.com/Hangman/TuningFork/blob/master/pageBin/logo.png) TuningFork
[![Release](https://jitpack.io/v/Hangman/TuningFork.svg)](https://jitpack.io/#Hangman/TuningFork)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Versioning](https://img.shields.io/badge/semver-2.0.0-blue)](https://semver.org/)

# Introduction
TuningFork is a library for [libGDX](https://github.com/libgdx/libgdx) that provides advanced audio features. The goal of this library is to make most of the features of [OpenAL](https://github.com/kcat/openal-soft) accessible and provide a comfortable, easy to use, low overhead and object oriented high-level API. In addition, the library offers some convenience features that are needed for almost every game, such as the music player.<br>
Note that TuningFork is not an extension to libGDX audio but a replacement.

### Main Features
* Spatial 3D and 2D audio
* Directional audio (3D)
* Real-time effects such as Reverb, Echo, Flanger, Distortion (and many more)
* Filters
* Music player with support for playlists and fading
* HRTF support (aka binaural)
* Streaming is handled on a background thread
* No more fiddling with sound IDs - proper OOP design
* Load **wav**, **aiff** (SNAPSHOT), **ogg**, **flac**, **mp3** ([see the full list](https://github.com/Hangman/TuningFork/wiki/Supported-audio-formats-and-codecs))
* Load asynchronously via `AssetManager`
* Play raw PCM data
* Supports surround sound formats
* Record audio
* Output to any sound device<br>(you are no longer tied to the default sound device)

### Limitations
* Lwjgl3 only, sorry mobile and web users

### Why
The standard audio capabilities of libGDX are very limited compared to sound APIs like OpenAL - probably due to its cross-platform nature. Especially if you are developing a 3D application and want to create realistic spatial sound, use modern features like HRTF etc., you'll face some problems with libGDX.

# Install
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
        implementation 'com.github.Hangman:TuningFork:3.3.0'
    }
}
```
### Compatibility
Java 8 is required, make sure to set `sourceCompatibility = JavaLanguageVersion.of(8)` (or higher) in your gradle scripts.
| Version of libGDX   | Latest compatible version of TuningFork  |
|      :----:         | :---                                     |
| 1.9.12 - 1.11.0     | 3.3.0                                    |
| 1.9.11              | 2.0.1                                    |
| < 1.9.11            | not supported, might work though: 2.0.1  |

### Upgrading
This library follows [semantic versioning](https://semver.org/). That means upgrading is always "safe" unless it is a major version increase. Accordingly, breaking changes are only made in major versions.
Nevertheless, I think it's a good habit to check out the latest patch notes before depending on a new version: [Patch Notes](https://github.com/Hangman/TuningFork/wiki/Patch-Notes)

# Get Started
[Go to the wiki](https://github.com/Hangman/TuningFork/wiki)

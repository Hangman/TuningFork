# ![logo](https://github.com/Hangman/TuningFork/blob/master/pageBin/logo.png) TuningFork
[![Release](https://jitpack.io/v/Hangman/TuningFork.svg)](https://jitpack.io/#Hangman/TuningFork)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Versioning](https://img.shields.io/badge/semver-2.0.0-blue)](https://semver.org/)

# Introduction
TuningFork is a library for [libGDX](https://github.com/libgdx/libgdx) that provides advanced audio features. The goal of this library is to make most of the features of [OpenAL](https://github.com/kcat/openal-soft) accessible and provide a comfortable, easy to use, low overhead and object oriented high-level API. Note that TuningFork is not an extension to libGDX audio but a replacement.

### Main Features
* Spatial audio (3D)
* Directional audio (3D)
* Normal audio (2D)
* Real-time effects such as Reverb, Echo, Flanger, Distortion (and many more)
* Filters
* HRTF support (aka binaural)
* Load **wav**, **ogg**, **flac** or bring your own pcm data
* Record audio
* Output to any sound device<br>(you are no longer tied to the default sound device)

### Limitations
* Lwjgl3 only, sorry mobile and web users

### Why
The standard audio capabilities of libGDX are kinda limited compared to sound APIs like OpenAL - probably due to its cross-platform nature. Especially if you are developing a 3D application and want to create realistic spatial sound, you'll face some problems with libGDX. Other existing sound libraries that I found always emulated a semi-realistic spatiality in their own code. That works but I didn't really like the approach as it creates unnecessary overhead. OpenAL already handles all that for us if you let it. 

# Install
TuningFork is available via Jitpack.
First make sure you have Jitpack declared as repository in your root build.gradle file:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Then add TuningFork as dependency in your core project: 

```groovy
project(":core") {
    dependencies {
    	...
        implementation 'com.github.Hangman:TuningFork:2.0.0'
    }
}
```
### Compatibility
Java 8 is required, make sure to have `sourceCompatibility = JavaVersion.VERSION_1_8` or any higher version than 8 in your gradle scripts.
| Version of libGDX | Latest compatible version of TuningFork |
|      :----:         | :---                          |
| 1.9.11 - 1.10.0 | 2.0.0 |
| < 1.9.11 | not supported, might work though: 2.0.0 |

### Upgrading
The latest patch notes can be found here: [Patch Notes](https://github.com/Hangman/TuningFork/wiki/Patch-Notes)

# Get Started
[Go to the wiki](https://github.com/Hangman/TuningFork/wiki)

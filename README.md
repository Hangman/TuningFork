# ![logo](https://github.com/Hangman/TuningFork/blob/master/pageBin/logo.png) TuningFork
[![Release](https://jitpack.io/v/Hangman/TuningFork.svg)](https://jitpack.io/#Hangman/TuningFork)

# Introduction
TuningFork is a library for [libGDX](https://github.com/libgdx/libgdx) that provides advanced audio features. The goal of this library is to make most of the features of OpenAL accessible and provide a comfortable, easy to use, low overhead and object oriented high-level API. Note that TuningFork is not an extension to libGDX audio but a replacement.

### Main Features
* Spatial audio (3D)
* Directional audio (3D)
* Normal audio (2D)
* Real-time effects such as Reverb, Echo, Flanger, Distortion (and many more)
* Filters
* HRTF support (aka binaural)
* Load wav, ogg, or bring your own pcm data
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
        implementation 'com.github.Hangman:TuningFork:1.1.0'
    }
}
```
### Compatibility
| Version of libGDX | Latest compatible version of TuningFork |
|      :----:         | :---                          |
| 1.9.11 - 1.10.0 | 1.1.0 |
| < 1.9.11 | not supported, might work though: 1.1.0 |

### Upgrading
The latest patch notes can be found here: [Patch Notes](https://github.com/Hangman/TuningFork/wiki/Patch-Notes)

# Get Started
[Go to the wiki](https://github.com/Hangman/TuningFork/wiki)

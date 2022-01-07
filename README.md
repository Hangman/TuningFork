# ![logo](https://github.com/Hangman/TuningFork/blob/master/pageBin/logo.png) TuningFork
[![Release](https://jitpack.io/v/Hangman/TuningFork.svg)](https://jitpack.io/#Hangman/TuningFork)

# Introduction
TuningFork is a library for libGDX that provides advanced audio features. The goal of this library is to make most of the features of OpenAL accessible and still provide a comfortable and easy to use high-level API.

### Main Features
* Spatial audio (3D)
* Directional audio (3D)
* Normal audio (2D)
* Real-time effects such as Reverb, Echo, Flanger, Distortion (and many more)
* Filters
* HRTF support (aka binaural)
* Easy to use API
* Output to any sound device (you are no longer tied to the default sound device)

### Limitations
* Lwjgl3 only, sorry mobile and web users

### Why
The standard audio capabilities of libGDX are very limited. Especially if you are developing a 3D application and want to create realistic spatial sound, libGDX's on-board features are in no way sufficient.
Except for [Rafa Skoberg](https://github.com/rafaskb)s [Boom](https://github.com/rafaskb/Parrot) and [Parrot](https://github.com/rafaskb/Parrot), there are no alternatives available to my knowledge that blend in well with libGDX.
If you're looking for an alternative to TuningFork, Rafa's libs might be an option for you.

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
        implementation 'com.github.Hangman:TuningFork:1.0.0'
    }
}
```
### Compatibility
| Version Of libGDX | Latest Compatible Version Of TuningFork |
|      :----:         | :---                          |
| 1.9.11 - 1.10.0 | 1.0.0 |
| < 1.9.11 | not supported, might work though: 1.0.0 |

## Upgrading
The latest patch notes can be found here: [Patch Notes](https://github.com/Hangman/TuningFork/wiki/Patch-Notes)

# Get Started
[Go to the wiki](https://github.com/Hangman/TuningFork/wiki)

# ![logo](https://github.com/Hangman/TuningFork/blob/master/logo.png) TuningFork

# Introduction
TuningFork is a library for libGDX that provides advanced audio features. To be upfront about it: This library only supports the desktop Lwjgl3 backend, sorry mobile and web users. The goal of TuningFork is to make as many features of OpenAL accessible as possible and still provide a comfortable and easy to use API.

### Main Features
* Spatial audio (3D)
* Directional audio (3D)
* Normal audio
* Advanced real time effects (Reverb, Echo, Flanger, and many more)
* HRTF support (aka binaural)
* Easy to use API
* Sound streaming
* ogg and wav files supported
* Output to any sound device (you are no longer tied to the default sound device)

### Limitations
* Lwjgl3 only
* No support for filters at the moment (can be imitated by the equalizer effect)

### Why
The standard audio capabilities of libGDX are very limited. Especially if you are developing a 3D application and want to create realistic spatial sound, libGDX's on-board resources are in no way sufficient.
Except for [Rafa Skoberg](https://github.com/rafaskb)s [Boom](https://github.com/rafaskb/Parrot) and [Parrot](https://github.com/rafaskb/Parrot), there are no alternatives available to my knowledge that blend in well with libGDX.
Boom offers the possibility to use effects, but only implements a subset of what's available.
Parrot on the other hand got some useful functions to play sounds, it even includes a music player. Unfortunately, it implements its own attenuation algorhithm, which isn't realistic.
If you're looking for a lighter alternative to TuningFork, Rafa's libs might be an option for you.

## Install
TuningFork is available via Jitpack.
First make sure you have Jitpack declared as repository and your root build.gradle file. Also add the version variable (master-SNAPSHOT for now, a stable release will follow in the future):
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
	ext {
        ...
        tfVersion = 'master-SNAPSHOT'
    }
}
```

Then add TuningFork as dependency in your core project: 

```groovy
project(":core") {
    dependencies {
    	...
        implementation 'com.github.Hangman:TuningFork:$tfVersion'
    }
}
```

## Get Started
[Go to the wiki](https://github.com/Hangman/TuningFork/wiki)

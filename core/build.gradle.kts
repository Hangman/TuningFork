val gdxVersion: String by project
val jmhInstanceVersion: String by project
val jmhPluginVersion: String by project
val junitJupiterVersion: String by project
val junitJupiterPlatformLauncherVersion: String by project
val flacLibraryJavaVersion: String by project

buildscript {
    project.version = "4.3.0"
    project.group = "de.pottgames"
}

plugins {
    id("java-library")
    id("eclipse")
    id("maven-publish")
    id("java")
    id("me.champeau.jmh") version "0.7.2"
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven("https://plugins.gradle.org/m2/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
    gradlePluginPortal()
}

dependencies {
    // compileOnly is kinda hacky but probably (afaik) the best solution we can get
    compileOnly("com.badlogicgames.gdx:gdx-backend-lwjgl3:${gdxVersion}")

    implementation("com.github.Hangman:FLAC-library-Java:${flacLibraryJavaVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter:${junitJupiterVersion}")
    testImplementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${gdxVersion}")
    testImplementation("com.badlogicgames.gdx:gdx-platform:${gdxVersion}:natives-desktop")
    testImplementation("com.badlogicgames.gdx:gdx:${gdxVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${junitJupiterPlatformLauncherVersion}")

    jmh("org.openjdk.jmh:jmh-core:${jmhInstanceVersion}")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:${jmhInstanceVersion}")
    jmh("com.badlogicgames.gdx:gdx-backend-lwjgl3:${gdxVersion}")
    jmh("com.badlogicgames.gdx:gdx-platform:${gdxVersion}:natives-desktop")
    jmh("com.badlogicgames.gdx:gdx:${gdxVersion}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.setIncremental(true)
    options.encoding = "UTF-8"
}

tasks {
    jar {
        manifest {
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Version"] = project.version
        }
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        failFast = true

        exclude("de/pottgames/tuningfork/test/InputAdapter.class")
        exclude("de/pottgames/tuningfork/test/Rng.class")
        exclude("de/pottgames/tuningfork/test/MiniExample.class")
        exclude("de/pottgames/tuningfork/test/DeviceTest.class")
        exclude("de/pottgames/tuningfork/test/HrtfTest.class")
        exclude("de/pottgames/tuningfork/test/AsyncLoadTest.class")
        exclude("de/pottgames/tuningfork/test/EffectTest.class")
        exclude("de/pottgames/tuningfork/test/PlaybackPositionBufferedSourceTest.class")
        exclude("de/pottgames/tuningfork/test/FilterTest.class")
        exclude("de/pottgames/tuningfork/test/PcmSoundSourceTest.class")
        exclude("de/pottgames/tuningfork/test/StreamedSoundSourceTest.class")
        exclude("de/pottgames/tuningfork/test/ProceduralSoundTest.class")
        exclude("de/pottgames/tuningfork/test/Note.class")
        exclude("de/pottgames/tuningfork/test/SongNote.class")
        exclude("de/pottgames/tuningfork/test/SongGenerator.class")
        exclude("de/pottgames/tuningfork/test/CaptureTest.class")
        exclude("de/pottgames/tuningfork/test/EightBitTest.class")
        exclude("de/pottgames/tuningfork/test/FlacTest.class")
        exclude("de/pottgames/tuningfork/test/ResamplerTest.class")
        exclude("de/pottgames/tuningfork/test/PanningTest.class")
        exclude("de/pottgames/tuningfork/test/WavFloat32PcmTest.class")
        exclude("de/pottgames/tuningfork/test/WavFloat64PcmTest.class")
        exclude("de/pottgames/tuningfork/test/WavInt24PcmTest.class")
        exclude("de/pottgames/tuningfork/test/WavInt32PcmTest.class")
        exclude("de/pottgames/tuningfork/test/WavExtensibleFormatTest.class")
        exclude("de/pottgames/tuningfork/test/ImaAdpcmWavMonoTest.class")
        exclude("de/pottgames/tuningfork/test/ImaAdpcmWavStereoTest.class")
        exclude("de/pottgames/tuningfork/test/Mp3LoadDemo.class")
        exclude("de/pottgames/tuningfork/test/Mp3StreamDemo.class")
        exclude("de/pottgames/tuningfork/test/SpeedOnlyChangeTest.class")
        exclude("de/pottgames/tuningfork/test/LoaderInputStreamTest.class")
        exclude("de/pottgames/tuningfork/test/JukeBoxTest.class")
        exclude("de/pottgames/tuningfork/test/AuxSendsTest.class")
        exclude("de/pottgames/tuningfork/test/SmartDeviceRerouterTest.class")
        exclude("de/pottgames/tuningfork/test/WavULawTest.class")
        exclude("de/pottgames/tuningfork/test/WavALawTest.class")
        exclude("de/pottgames/tuningfork/test/AifcULawTest.class")
        exclude("de/pottgames/tuningfork/test/AifcALawTest.class")
        exclude("de/pottgames/tuningfork/test/MsAdpcmWavMonoTest.class")
        exclude("de/pottgames/tuningfork/test/MsAdpcmWavStereoTest.class")
        exclude("de/pottgames/tuningfork/test/StbVorbisTest.class")
        exclude("de/pottgames/tuningfork/test/DirectChannelRemixTest.class")
        exclude("de/pottgames/tuningfork/test/LoopPointSoundBufferTest.class")
        exclude("de/pottgames/tuningfork/test/LoopPointStreamingTest.class")
        exclude("de/pottgames/tuningfork/test/ClockTest.class")
        exclude("de/pottgames/tuningfork/test/PlayReverseExample.class")
        exclude("de/pottgames/tuningfork/test/SoundBufferLoaderParameterTest.class")
        exclude("de/pottgames/tuningfork/test/UpdateSoundEffectTest.class")
        exclude("de/pottgames/tuningfork/test/PlayStartDelayTest.class")
        exclude("de/pottgames/tuningfork/test/ThemePlayListProviderTest.class")
        exclude("de/pottgames/tuningfork/test/QoaTest.class")
        exclude("de/pottgames/tuningfork/test/unit/DurationTest.class")
        exclude("de/pottgames/tuningfork/test/unit/SoundSourceUnitTest.class")
        exclude("de/pottgames/tuningfork/test/unit/SoundLoaderUnitTest.class")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
        }
    }
}

jmh {
    iterations = 3 // Number of measurement iterations to do.
    benchmarkMode.set(listOf("avgt")) // Benchmark mode. Available modes are: [Throughput/thrpt, AverageTime/avgt, SampleTime/sample, SingleShotTime/ss, All/all]
    batchSize = 1 // Batch size: number of benchmark method calls per operation.
    fork = 0 // How many times to forks a single benchmark. Use 0 to disable forking altogether
    failOnError = true // Should JMH fail immediately if any benchmark had experienced the unrecoverable error?
    forceGC = true // Should JMH force GC between iterations?
    humanOutputFile = project.file("build/reports/jmh/human.txt") // human-readable output file
    resultsFile = project.file("build/reports/jmh/results.txt") // results file
    operationsPerInvocation = 10 // Operations per invocation.
    timeOnIteration = "2s" // Time to spend at each measurement iteration.
    resultFormat = "TEXT" // Result format type (one of CSV, JSON, NONE, SCSV, TEXT)
    timeUnit = "ms" // Output time unit. Available time units are: [m, s, ms, us, ns].
    verbosity = "NORMAL" // Verbosity mode. Available modes are: [SILENT, NORMAL, EXTRA]
    warmup = "1s" // Time to spend at each warmup iteration.
    warmupBatchSize = 1 // Warmup batch size: number of benchmark method calls per operation.
    warmupForks = 1 // How many warmup forks to make for a single benchmark. 0 to disable warmup forks.
    warmupIterations = 0 // Number of warmup iterations to do.
    warmupMode = "INDI" // Warmup mode for warming up selected benchmarks. Warmup modes are: [INDI, BULK, BULK_INDI].
    zip64 = false // Use ZIP64 format for bigger archives
    jmhVersion = jmhInstanceVersion // Specifies JMH version
    includeTests = false
}

eclipse.project.name = "TuningFork-core"
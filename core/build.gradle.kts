val gdxVersion: String by project
val lwjglVersion: String by project
val jmhInstanceVersion: String by project
val jmhPluginVersion: String by project
val junitJupiterVersion: String by project
val junitJupiterPlatformLauncherVersion: String by project
val flacLibraryJavaVersion: String by project

val testAppClasses = listOf(
    "de.pottgames.tuningfork.test.InputAdapter",
    "de.pottgames.tuningfork.test.Rng",
    "de.pottgames.tuningfork.test.MiniExample",
    "de.pottgames.tuningfork.test.DeviceTest",
    "de.pottgames.tuningfork.test.HrtfTest",
    "de.pottgames.tuningfork.test.AsyncLoadTest",
    "de.pottgames.tuningfork.test.EffectTest",
    "de.pottgames.tuningfork.test.PlaybackPositionBufferedSourceTest",
    "de.pottgames.tuningfork.test.FilterTest",
    "de.pottgames.tuningfork.test.PcmSoundSourceTest",
    "de.pottgames.tuningfork.test.StreamedSoundSourceTest",
    "de.pottgames.tuningfork.test.ProceduralSoundTest",
    "de.pottgames.tuningfork.test.Note",
    "de.pottgames.tuningfork.test.SongNote",
    "de.pottgames.tuningfork.test.SongGenerator",
    "de.pottgames.tuningfork.test.CaptureTest",
    "de.pottgames.tuningfork.test.EightBitTest",
    "de.pottgames.tuningfork.test.FlacTest",
    "de.pottgames.tuningfork.test.ResamplerTest",
    "de.pottgames.tuningfork.test.PanningTest",
    "de.pottgames.tuningfork.test.WavFloat32PcmTest",
    "de.pottgames.tuningfork.test.WavFloat64PcmTest",
    "de.pottgames.tuningfork.test.WavInt24PcmTest",
    "de.pottgames.tuningfork.test.WavInt32PcmTest",
    "de.pottgames.tuningfork.test.WavExtensibleFormatTest",
    "de.pottgames.tuningfork.test.ImaAdpcmWavMonoTest",
    "de.pottgames.tuningfork.test.ImaAdpcmWavStereoTest",
    "de.pottgames.tuningfork.test.Mp3LoadDemo",
    "de.pottgames.tuningfork.test.Mp3StreamDemo",
    "de.pottgames.tuningfork.test.SpeedOnlyChangeTest",
    "de.pottgames.tuningfork.test.LoaderInputStreamTest",
    "de.pottgames.tuningfork.test.JukeBoxTest",
    "de.pottgames.tuningfork.test.AuxSendsTest",
    "de.pottgames.tuningfork.test.SmartDeviceRerouterTest",
    "de.pottgames.tuningfork.test.WavULawTest",
    "de.pottgames.tuningfork.test.WavALawTest",
    "de.pottgames.tuningfork.test.AifcULawTest",
    "de.pottgames.tuningfork.test.AifcALawTest",
    "de.pottgames.tuningfork.test.MsAdpcmWavMonoTest",
    "de.pottgames.tuningfork.test.MsAdpcmWavStereoTest",
    "de.pottgames.tuningfork.test.StbVorbisTest",
    "de.pottgames.tuningfork.test.DirectChannelRemixTest",
    "de.pottgames.tuningfork.test.LoopPointSoundBufferTest",
    "de.pottgames.tuningfork.test.LoopPointStreamingTest",
    "de.pottgames.tuningfork.test.ClockTest",
    "de.pottgames.tuningfork.test.PlayReverseExample",
    "de.pottgames.tuningfork.test.SoundBufferLoaderParameterTest",
    "de.pottgames.tuningfork.test.UpdateSoundEffectTest",
    "de.pottgames.tuningfork.test.PlayStartDelayTest",
    "de.pottgames.tuningfork.test.ThemePlayListProviderTest",
    "de.pottgames.tuningfork.test.QoaTest",
    "de.pottgames.tuningfork.test.WaveFormTest",
    "de.pottgames.tuningfork.test.SoundLoaderAudioStreamTest",
    "de.pottgames.tuningfork.test.unit.DurationTest",
    "de.pottgames.tuningfork.test.unit.SoundSourceUnitTest",
    "de.pottgames.tuningfork.test.unit.SoundLoaderUnitTest"
)

buildscript {
    project.version = "4.4.4"
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
    // compileOnly is not hacky and afaik the best solution we can get
    compileOnly("com.badlogicgames.gdx:gdx-backend-lwjgl3:${gdxVersion}")

    // Explicit lwjgl version for testing (overrides transitive version from gdx-backend-lwjgl3)
    compileOnly("org.lwjgl:lwjgl:${lwjglVersion}")
    compileOnly("org.lwjgl:lwjgl-openal:${lwjglVersion}")
    compileOnly("org.lwjgl:lwjgl-stb:${lwjglVersion}")

    implementation("com.github.Hangman:FLAC-library-Java:${flacLibraryJavaVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter:${junitJupiterVersion}")
    testImplementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:${gdxVersion}")

    // Explicit lwjgl version for testing (overrides transitive version from gdx-backend-lwjgl3)
    testImplementation("org.lwjgl:lwjgl:${lwjglVersion}")
    testImplementation("org.lwjgl:lwjgl-openal:${lwjglVersion}")
    testImplementation("org.lwjgl:lwjgl-stb:${lwjglVersion}")

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
        languageVersion.set(JavaLanguageVersion.of(25))
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

        testAppClasses.forEach { className ->
            exclude(className.replace(".", "/") + ".class")
        }
    }

    // Create run tasks for all non-unit-tests
    testAppClasses.forEach { className ->
        val taskName = className.substringAfterLast('.')
        register<JavaExec>(taskName) {
            group = "verification"
            mainClass.set(className)
            classpath = sourceSets.test.get().runtimeClasspath
            standardInput = System.`in`
            jvmArgs(
                "--enable-native-access=ALL-UNNAMED",
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.nio=ALL-UNNAMED",
                "--add-opens", "java.base/java.util=ALL-UNNAMED"
            )
        }
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.lwjgl") {
                useVersion(lwjglVersion)
            }
        }
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

val eclipseClasspath by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom(configurations.compileOnly.get())
}

eclipse {
    classpath {
        plusConfigurations.add(eclipseClasspath)
    }
}

eclipse.project.name = "TuningFork-core"

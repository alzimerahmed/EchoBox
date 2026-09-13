@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.gradle.internal.tasks.CompileArtProfileTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

val isFullBuild: Boolean =
    try {
        extra["isFullBuild"] == "true"
    } catch (e: Exception) {
        false
    }

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xmulti-dollar-interpolation")
        freeCompilerArgs.add("-Xwhen-guards")
    }
    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    android {
        namespace = "com.alzimer.echobox.data"
        compileSdk = 37
        minSdk = 26
        withHostTest { }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    sourceSets {
        commonMain {
            dependencies {
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(projects.common)
                implementation(projects.domain)
                implementation(projects.aiService)
                implementation(projects.autoEqService)
                implementation(projects.lyricsService)
                implementation(projects.spotify)
                implementation(projects.kotlinYtmusicScraper)
                implementation(projects.kizzy)
                implementation(projects.listenTogether)

                // Last.fm (gated: real scrobbler for full builds, no-op stub for FOSS builds)
                if (isFullBuild) {
                    implementation(projects.lastfm)
                } else {
                    implementation(projects.lastfmEmpty)
                }

                implementation(libs.kotlin.stdlib)
                // Add KMP dependencies here
                // Kotlinx serialization
                implementation(libs.kotlinx.serialization.json)

                // DataStore
                implementation(libs.datastore.preferences)

                // Room
                implementation(libs.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
                implementation(libs.androidx.room.migration)

                // Koin
                implementation(libs.koin.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val androidHostTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.turbine)
                implementation(libs.robolectric)
                implementation(libs.androidx.junit)
                implementation(libs.junit)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
                implementation(libs.koin.android)
                implementation(projects.media3)
                implementation(libs.room.ktx)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
}

tasks.withType<CompileArtProfileTask> {
    enabled = false
}
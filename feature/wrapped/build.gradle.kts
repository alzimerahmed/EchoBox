plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(21)
    android {
        namespace = "com.alzimer.echobox.feature.wrapped"
        compileSdk = 37
        minSdk = 26
        withHostTest { }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xwhen-guards")
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(projects.common)
                implementation(projects.domain)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                // WrappedFormat formats with the compose locale APIs
                implementation(libs.runtime)
                implementation(libs.compose.ui)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

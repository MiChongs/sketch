plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.kover")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.sketch.http.ktor2")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.sketchCore)
            api(libs.ktor2.client.core)
        }
        wasmJsMain.dependencies {
            api(libs.ktor2.client.coreWasmJs)
        }

        commonTest.dependencies {
            implementation(projects.internal.test)
            implementation(projects.internal.testSingleton)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.ktor2.client.android)
        }
        desktopTest.dependencies {
            implementation(libs.ktor2.client.java)
        }
        iosTest.dependencies {
            implementation(libs.ktor2.client.darwin)
        }
        jsTest.dependencies {
            implementation(libs.ktor2.client.js)
        }
        wasmJsTest.dependencies {
            implementation(libs.ktor2.client.wasmJs)
        }
    }
}
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.sketch.extensions.view")

dependencies {
    api(projects.sketch)
    api(projects.sketchExtensionsViewCore)
    androidTestImplementation(projects.internal.testUtils)
}
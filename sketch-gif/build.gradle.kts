plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = property("COMPILE_SDK").toString().toInt()

    defaultConfig {
        minSdk = property("MIN_SDK").toString().toInt()
        targetSdk = property("TARGET_SDK").toString().toInt()

        consumerProguardFiles("proguard-rules.pro")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "VERSION_NAME", "\"${property("VERSION_NAME")}\"")
        buildConfigField("int", "VERSION_CODE", "${property("VERSION_CODE")}")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    api("androidx.annotation:annotation:${property("ANDROIDX_ANNOTATION")}")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${property("KOTLIN")}")
    api("pl.droidsonroids:relinker:${property("RELINKER")}")
    api(project(":sketch-core"))

    testImplementation("junit:junit:${property("JUNIT")}")
    androidTestImplementation("com.android.support.test:runner:${property("ANDROIDX_TEST_RUNNER")}")
    androidTestImplementation("com.android.support.test:rules:${property("ANDROIDX_TEST_RULES")}")
}

/**
 * publish config
 */
if (hasProperty("signing.keyId")    // configured in the ~/.gradle/gradle.properties file
    && hasProperty("signing.password")    // configured in the ~/.gradle/gradle.properties file
    && hasProperty("signing.secretKeyRingFile")    // configured in the ~/.gradle/gradle.properties file
    && hasProperty("mavenCentralUsername")    // configured in the ~/.gradle/gradle.properties file
    && hasProperty("mavenCentralPassword")    // configured in the ~/.gradle/gradle.properties file
    && hasProperty("GROUP")    // configured in the rootProject/gradle.properties file
    && hasProperty("POM_ARTIFACT_ID")    // configured in the project/gradle.properties file
) {
    apply { plugin("com.github.panpf.maven.publish") }

    configure<com.github.panpf.maven.publish.MavenPublishPluginExtension> {
        sonatypeHost = com.github.panpf.maven.publish.SonatypeHost.S01
        disableAndroidJavaDocsAddReferencesLinks = true
    }
}
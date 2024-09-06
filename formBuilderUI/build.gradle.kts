import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    id("com.vanniktech.maven.publish") version "0.29.0"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        publishLibraryVariants("release","debug")
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "FormBuilderUI"
            isStatic = true
        }
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.lifecycle.viewmodel.compose)

            //date time
            implementation(libs.kotlinx.datetime)

            //kermit
            implementation(libs.kermit)

            //serialization
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

android {
    namespace = "io.github.aniketdhakorkar"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.aniketdhakorkar",
        artifactId = "form-builder-ui",
        version = "1.0.0-beta-03"
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("FormBuilderUILibrary")
        description.set("Library used to open up a web browser on both Android/iOS.")
        inceptionYear.set("2024")
        url.set("https://github.com/aniketdhakorkar/FormBuilderUILibrary")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        // Specify developers information
        developers {
            developer {
                id.set("aniketdhakorkar")
                name.set("Aniket Dhakorkar")
                email.set("aniket4dhakorkar@gmail.com")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/aniketdhakorkar/FormBuilderUILibrary")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}


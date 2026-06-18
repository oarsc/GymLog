plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

version = "1.0.0"
group = "org.oar.gymlog.manager"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

kotlin {
    js {
        browser {
            binaries.executable()
            runTask {
                sourceMaps = false
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation("com.github.oarsc:kotlin-js-blocks:v1.0.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            }
        }
    }
}

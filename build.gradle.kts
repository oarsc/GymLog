plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

version = "1.0.0"
group = "org.oar.gymlog.manager"

repositories {
    mavenCentral()
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
        val coroutinesVersion = "1.7.3"
        val serializationVersion = "1.6.0"
//        val kotlinxHtmlVersion = "0.12.0"
        val doodleVersion = "0.10.4"

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
//                implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinxHtmlVersion")

                implementation("io.nacular.doodle:core:$doodleVersion")
                //implementation("io.nacular.doodle:browser:$doodleVersion")

                // Optional dependencies:
                // implementation("io.nacular.doodle:controls:$doodleVersion")
                // implementation("io.nacular.doodle:animation:$doodleVersion")
                // implementation("io.nacular.doodle:themes:$doodleVersion")
            }
        }
    }
}

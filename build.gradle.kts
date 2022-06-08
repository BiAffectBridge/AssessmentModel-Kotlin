buildscript {
    val compose_version by extra("1.2.0-beta02")
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.6.21")
    }
}

plugins {
    id("org.jetbrains.dokka") version "1.6.21"
    id("maven-publish")
}

tasks.dokkaHtmlMultiModule {
    outputDirectory.set(rootDir.resolve("docs"))
}

allprojects {
    group = "org.sagebionetworks.assessmentmodel"
    version = "0.7.0"

    repositories {
        google()
        mavenCentral()
        jcenter() // still used by org.jetbrains.dokka:javadoc-plugin - liujoshua 05-07-2021
        //TODO: Remove once kotlinx.datatime 0.4.0 release is available -nbrown 05/26/2022
        maven("https://teamcity.jetbrains.com/guestAuth/repository/download/KotlinTools_KotlinxDatetime_Build_All/3858989:id/maven/")
    }
}

subprojects {
    afterEvaluate {
        if (project.plugins.hasPlugin("com.android.library")) {
//            val android = this.extensions.getByName("android") as com.android.build.gradle.LibraryExtension
//            val kotlin =
//                this.extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java)

            tasks.register<Jar>("javadocJar") {
                val dokkaJavadoc = tasks.getByName<org.jetbrains.dokka.gradle.DokkaTask>("dokkaJavadoc")
                dependsOn(dokkaJavadoc)
                classifier = "javadoc"
                from(dokkaJavadoc.outputDirectory)
            }
        }
    }
}

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.0")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.4.0")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
    }
}

allprojects {
    group = "org.sagebionetworks.assessmentmodel"
    version = "0.3.0"

    repositories {
        jcenter()
        google()
    }
}

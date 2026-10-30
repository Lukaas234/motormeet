buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = "https://jitpack.io" }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.0.21")
        classpath("org.jetbrains.kotlin:kotlin-serialization:2.0.21")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.0.21-1.0.25")
        classpath("org.jetbrains.kotlin:compose-multiplatform-kotlin-plugin:1.7.3")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = "https://jitpack.io" }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

plugins {
    java
    kotlin("jvm") version "2.1.0" apply false
}

allprojects {
    group = "com.example.satdsl"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

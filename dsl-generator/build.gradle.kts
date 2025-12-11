plugins {
    java
    id("org.xtext.xtend") version "4.0.0" apply false
}

dependencies {
    implementation(project(":common-api"))
    
    // Xtext 2.41 dependencies (targeting Java 21)
    implementation("org.eclipse.xtext:org.eclipse.xtext:2.36.0")
    implementation("org.eclipse.xtext:org.eclipse.xtext.xbase:2.36.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

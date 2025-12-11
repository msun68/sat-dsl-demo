plugins {
    java
}

dependencies {
    implementation(project(":common-api"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// FFM API is preview in Java 21, finalized in Java 22+
tasks.withType<JavaCompile> {
    options.release.set(21)
    options.compilerArgs.addAll(listOf("--enable-preview"))
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}

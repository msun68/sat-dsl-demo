import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":common-api"))
    implementation(project(":dsl-generator"))
    implementation(project(":cadical-ffm"))
    
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<JavaCompile> {
    options.release.set(21)
    options.compilerArgs.addAll(listOf("--enable-preview"))
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")
}

application {
    mainClass.set("com.example.satdsl.cli.MainKt")
}

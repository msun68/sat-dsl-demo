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
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

kotlin {
    jvmToolchain(25)
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}

application {
    mainClass.set("com.example.satdsl.cli.MainKt")
}

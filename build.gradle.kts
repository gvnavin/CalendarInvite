plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation("javax.mail:mail:1.5.0-b01")
    implementation("jakarta.mail:jakarta.mail-api:2.0.1")
    implementation("software.amazon.awssdk:protocol-core:2.17.143")
    implementation(platform("software.amazon.awssdk:bom:2.17.256"))
    implementation("software.amazon.awssdk:pinpointemail")
    implementation("software.amazon.awssdk:ses")
//    implementation("software.amazon.awssdk:sesv2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("com.example.ses.SendMessage")
}
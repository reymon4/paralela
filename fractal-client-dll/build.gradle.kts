plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
   implementation("net.java.dev.jna:jna:5.16.0")
}

tasks.test {
    useJUnitPlatform()
}
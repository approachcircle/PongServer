plugins {
    id("java")
    id("com.github.johnrengelman.shadow").version("8.1.1")
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.corundumstudio.socketio:netty-socketio:2.0.6")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "net.approachcircle.server.Main"
    }
}

tasks.test {
    useJUnitPlatform()
}
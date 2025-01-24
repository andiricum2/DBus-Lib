plugins {
    id("java")
    id("maven-publish")
}

group = "com.andiri.libs.dbus"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()

    // Configuramos la compatibilidad de bytecode para versiones superiores a 11
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("DBus Library")
                description.set("A simple Java library for DBus integration.")
                url.set("https://github.com/andiricum2/DBus-Lib")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("andiri")
                        name.set("Andoni Iriso")
                        email.set("andiricum2@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/andiricum2/DBus-Lib.git")
                    developerConnection.set("scm:git:ssh://github.com/andiricum2/DBus-Lib.git")
                    url.set("https://github.com/andiricum2/DBus-Lib")
                }
            }
        }
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
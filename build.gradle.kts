plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.7")
    implementation("io.rest-assured:rest-assured:5.5.6")
    implementation(files("libs/oa_at_train_project-1.0-SNAPSHOT.jar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Test>("apiTest") {
    group = "verification"
    useJUnitPlatform {
        includeTags("api")
    }
}
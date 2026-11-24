/*
 * Точка входа Spring Boot и REST-контроллеры (входной адаптер).
 */
plugins {
    id("org.springframework.boot") version "4.2.0"
}

dependencies {
    implementation(project(":application"))
    implementation(project(":infrastructure"))

    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.2.0"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}

tasks.withType<Jar>().configureEach {
    enabled = false
}
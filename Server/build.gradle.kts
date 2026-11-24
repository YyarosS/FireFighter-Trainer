/*
 * Корневой build-скрипт серверной части тренажёра.
 *
 * Модули (слои DDD):
 *   domain         — ядро предметной области, без внешних зависимостей;
 *   application    — сценарии использования, зависит только от domain;
 *   infrastructure — адаптеры (in-memory репозитории, часы, генератор id) и Spring-конфигурация;
 *   web            — REST-контроллеры и точка входа Spring Boot.
 *
 * Правило зависимостей: domain не знает ни о Spring, ни о web, ни о СУБД.
 * Оно обеспечено не соглашением, а сборкой: Spring отсутствует в classpath
 * модулей domain и application, поэтому импорт фреймворка там не скомпилируется.
 */

val springBootVersion = "4.2.0"
val springBootBom = "org.springframework.boot:spring-boot-dependencies:$springBootVersion"

allprojects {
    group = "ru.mirea.firefighter"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    // java-library, а не java: модули публикуют типы домена в своём API
    // (api-зависимости), чтобы соседние слои видели модель, но не тянули
    // фреймворки транзитивно.
    apply(plugin = "java-library")

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(21)
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:all")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "failed", "skipped")
        }
    }

    dependencies {
        // Версии тестовых библиотек задаёт BOM Spring Boot — единственный источник версий.
        "testImplementation"(platform(springBootBom))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testImplementation"("org.assertj:assertj-core")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }
}
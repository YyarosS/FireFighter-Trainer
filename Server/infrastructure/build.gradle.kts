/*
 * Адаптеры к внешнему миру. Здесь и только здесь появляется Spring:
 * @Configuration собирает граф объектов из домена и приложения.
 * На этапе 5 in-memory реализации будут заменены на JPA/PostgreSQL.
 */
dependencies {
    api(project(":application"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.2.0"))
    implementation("org.springframework:spring-context")
}
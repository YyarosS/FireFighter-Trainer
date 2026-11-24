package ru.mirea.firefighter.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа серверной части VR-тренажёра подготовки спасателей.
 *
 * <p>Сканирование начинается с общего пакета {@code ru.mirea.firefighter},
 * чтобы захватить конфигурацию инфраструктуры: сам модуль {@code web}
 * знает о ней только как о зависимости сборки.</p>
 */
@SpringBootApplication(scanBasePackages = "ru.mirea.firefighter")
public class FirefighterServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FirefighterServerApplication.class, args);
    }
}
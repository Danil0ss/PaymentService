package com.example.PaymentService.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class LiquibaseRunner {

    private static final Logger log = LoggerFactory.getLogger(LiquibaseRunner.class);

    @Value("${spring.data.mongodb.uri}")
    private String url;

    @PostConstruct
    public void runLiquibase() {

        String changeLogFile = "db/changelog/db.changelog-master.xml";

        try {
            log.info("Запуск Liquibase миграций для MongoDB...");

            Database database = DatabaseFactory.getInstance().openDatabase(
                    url,
                    null,
                    null,
                    null,
                    new ClassLoaderResourceAccessor()
            );

            Liquibase liquibase = new Liquibase(
                    changeLogFile,
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update("");

            log.info("Liquibase миграции успешно применены!");

        } catch (Exception e) {
            log.error("Ошибка при выполнении Liquibase миграций", e);
            throw new RuntimeException("Не удалось применить Liquibase миграции", e);
        }
    }
}
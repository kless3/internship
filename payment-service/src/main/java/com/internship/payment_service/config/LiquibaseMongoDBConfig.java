package com.internship.payment_service.config;

import com.internship.payment_service.config.property.LiquibaseMongoProperties;
import com.internship.payment_service.config.property.MongoProperties;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionCommandStep;
import liquibase.exception.LiquibaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "spring.liquibase-mongodb",
        name = "enabled",
        havingValue = "true"
)
public class LiquibaseMongoDBConfig {

    private static final String LIQUIBASE_UPDATE_COMMAND = "update";
    private static final String MONGODB_CONNECTION_URL_TEMPLATE = "mongodb://%s:%s/%s";
    private static final String MIGRATIONS_FAIL = "Liquibase migration failed";


    private final LiquibaseMongoProperties liquibaseProperties;
    private final MongoProperties mongoProperties;

    @EventListener(ContextRefreshedEvent.class)
    public void runLiquibaseUpdate() {
        try {
            CommandResults results = new CommandScope(LIQUIBASE_UPDATE_COMMAND)
                    .addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, liquibaseProperties.getChangeLog())
                    .addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, buildConnectionUrl())
                    .execute();
        } catch (LiquibaseException e) {
            throw new RuntimeException(MIGRATIONS_FAIL, e);
        }
    }

    private String buildConnectionUrl() {
        return String.format(MONGODB_CONNECTION_URL_TEMPLATE,
                mongoProperties.getHost(),
                mongoProperties.getPort(),
                mongoProperties.getDatabase());
    }
}
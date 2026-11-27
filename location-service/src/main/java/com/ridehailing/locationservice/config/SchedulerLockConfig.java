package com.ridehailing.locationservice.config;

import com.mongodb.client.MongoClient;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
public class SchedulerLockConfig {

    @Bean
    public LockProvider lockProvider(MongoClient mongoClient,
                                     @Value("${spring.data.mongodb.database:locationdb}") String database) {
        return new MongoLockProvider(mongoClient.getDatabase(database));
    }
}

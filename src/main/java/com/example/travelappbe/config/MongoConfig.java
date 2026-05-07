package com.example.travelappbe.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfig {

    @Primary
    @Bean
    public MongoClient mongoClient(
            @Value("${spring.data.mongodb.uri}") String uri,
            @Value("${spring.data.mongodb.username:}") String username,
            @Value("${spring.data.mongodb.password:}") String password,
            @Value("${spring.data.mongodb.authentication-database:}") String authenticationDatabase) {
        ConnectionString connectionString = new ConnectionString(uri);

        if (!username.isBlank() && !password.isBlank()) {
            String authDb = authenticationDatabase.isBlank() ? connectionString.getDatabase() : authenticationDatabase;
            if (authDb == null || authDb.isBlank()) {
                authDb = "admin";
            }
            MongoCredential credential = MongoCredential.createCredential(username, authDb, password.toCharArray());
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .credential(credential)
                    .build();
            return MongoClients.create(settings);
        }

        return MongoClients.create(connectionString);
    }

    @Primary
    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient,
                                                     @Value("${spring.data.mongodb.database}") String database) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, database);
    }

    @Primary
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}

package app.c2.dao.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DBConfiguration {

    @Autowired
    DataSourceProperties dataSourceProperties;

    @Bean
    public DataSource dataSource() {
        if(dataSourceProperties.getPlatform().equalsIgnoreCase("sqlite")){
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName("org.sqlite.JDBC");
            dataSourceBuilder.url(dataSourceProperties.getUrl());
            return dataSourceBuilder.build();
        }else{
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName(DatabaseDriver
                    .fromJdbcUrl(dataSourceProperties.getUrl()).getDriverClassName());
            dataSourceBuilder.password("P@ssw0rd123");
            dataSourceBuilder.username("c2");
            dataSourceBuilder.url(dataSourceProperties.getUrl());
            return dataSourceBuilder.build();
        }
    }

}

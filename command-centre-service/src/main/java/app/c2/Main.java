package app.c2;

import app.c2.dao.configuration.FileStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({
        FileStorageProperties.class,
        C2PlatformProperties.class
})

@SpringBootApplication(scanBasePackages = {"app.c2"})
public class Main implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Main.class);
        app.run(args);
    }

    @Autowired
    private Cli2 cli;

    @Override
    public void run(String... args) {
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }
}
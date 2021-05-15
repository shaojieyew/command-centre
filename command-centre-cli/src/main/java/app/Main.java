package app;

import app.c2.C2PlatformProperties;
import app.c2.dao.configuration.FileStorageProperties;
import app.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.context.LifecycleAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@EnableConfigurationProperties({
        FileStorageProperties.class,
        C2PlatformProperties.class
})

@Configuration
@SpringBootApplication(scanBasePackages = {"app"})
public class Main implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Main.class);
        app.run(args);
    }

    @Autowired
    private CreateCli createCli;
    @Autowired
    private DeleteCli deleteCli;
    @Autowired
    private RunCli runCli;
    @Autowired
    private StopCli stopCli;
    @Autowired
    private LsCli lsCli;
    @Autowired
    private GetCli getCli;

    @Override
    public void run(String... args) {
        String option = args[0];
        String [] cliArgs = new String[args.length-1];
        System.arraycopy(args,1,cliArgs,0,args.length-1);
        int exitCode = 0;
        String action = option.toLowerCase();
        LOG.info("starting action="+action);
        switch (action){
            case "create":
                exitCode = createCli.execute(cliArgs);
                break;
            case "delete":
            case "remove":
                exitCode = deleteCli.execute(cliArgs);
                break;
            case "get":
                exitCode = getCli.execute(cliArgs);
                break;
            case "ls":
                exitCode = lsCli.execute(cliArgs);
                break;
            case "run":
            case "start":
                exitCode = runCli.execute(cliArgs);
                break;
            case "stop":
                exitCode = stopCli.execute(cliArgs);
                break;
        }
        System.exit(exitCode);
    }
}
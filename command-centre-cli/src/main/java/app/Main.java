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
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@EnableConfigurationProperties({
        FileStorageProperties.class,
        C2PlatformProperties.class
})

@SpringBootApplication(scanBasePackages = {"app"})
public class Main implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println(Main.class.getResource("/spark-2.4.7-bin-hadoop2.7").getPath());
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
        switch (option.toLowerCase()){
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
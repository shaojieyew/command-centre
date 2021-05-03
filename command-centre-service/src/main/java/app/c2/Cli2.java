package app.c2;

import app.c2.model.User;
import app.c2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;


@Command(name = "", mixinStandardHelpOptions = true, version = "1.0",
        description = "")
@Service
public class Cli2 implements Callable<Integer> {

    public int execute(String[] args){
        return new CommandLine(this).execute(args);
    }

    @Autowired
    UserService userService;
    @Override
    public Integer call() throws Exception {
        User user = new User();
        user.setEmail("ABC@GMAIL.com");
        user.setUserId("ABC");
        userService.save(user);
        return 0;
    }
}


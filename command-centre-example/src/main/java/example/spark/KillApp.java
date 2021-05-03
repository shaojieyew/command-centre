package example.spark;

import app.c2.services.yarn.YarnSvc;

import java.io.IOException;

public class KillApp {

    public static void main(String arg[]) throws IOException {
        YarnSvc.builder("http://localhost:8088/")
                .setApplicationId("application_1615085878868_0011")
                .kill();
    }
}

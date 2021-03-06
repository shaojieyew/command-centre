package example.yarn;
import app.c2.service.yarn.YarnSvc;

public class Main {
    public static void main(String arg[]) throws Exception {
        YarnSvc.builder("http://localhost:8088")
                .setApplicationId("application_1616240364888_0003")
                .get()
                .stream().forEach(x->System.out.println(x.getId()+" "+x.getName()+" "+x.getState()));

        YarnSvc.builder("http://localhost:8088")
                .setStates("RUNNING")
                .setQueue("default")
                .get()
                .stream().forEach(x->System.out.println(x.getId()+" "+x.getName()));

        YarnSvc.builder("http://localhost:8088")
                .setApplicationId("application_1610802627554_0004")
                .kill();
    }
}

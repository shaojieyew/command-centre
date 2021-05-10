package app.util;

import app.cli.StopCli;
import org.apache.hadoop.log.LogLevel;
import org.jboss.logging.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ConsoleHelper {
    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(StopCli.class);
    public static ConsoleHelper console = new ConsoleHelper();
    private String lastLine = "";
    private byte anim;
    private final static int PROGRESS_BAR_LENGTH = 30;

    private void print(String line) {
        //clear the last line if longer
        if (lastLine.length() > line.length()) {
            String temp = "";
            for (int i = 0; i < lastLine.length(); i++) {
                temp += " ";
            }
            if (temp.length() > 1)
                System.out.print("\r" + temp);
        }
        System.out.print("\r" + line);
        lastLine = line;
    }

    public void animate(String line, int status) {
        String progress = "";
        int progressCount = anim%PROGRESS_BAR_LENGTH;
        if(status == 1){
            for(int i=0;i<PROGRESS_BAR_LENGTH;i++){
                progress=progress+"#";
            }
            print("["+progress+"] " + line +" done\n");
        }
        if(status == -1){
            for(int i=0;i<PROGRESS_BAR_LENGTH;i++){
                progress=progress+"#";
            }
            print("["+progress+"] " + line +" failed\n");
        }
        if(status == 0){
            for(int i=0;i<=progressCount;i++){
                progress=progress+"#";
            }
            for(int i=progressCount+1;i<PROGRESS_BAR_LENGTH;i++){
                progress=progress+"-";
            }
            print("["+progress+"] " + line +" loading");
        }
        anim++;
    }

    public void display(String message){
        display("\r"+message, Logger.Level.INFO);
    }
    public void display(String message, Logger.Level level){
        System.out.println("\r"+level.toString()+" "+message);
        if(level.equals(Logger.Level.INFO)){
            LOG.info(message);
        }
        if(level.equals(Logger.Level.WARN)){
            LOG.warn(message);
        }
        if(level.equals(Logger.Level.ERROR)){
            LOG.error(message);
        }
        if(level.equals(Logger.Level.FATAL)){
            LOG.error(message);
        }
        if(level.equals(Logger.Level.DEBUG)){
            LOG.debug(message);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            ConsoleHelper.console.animate("asds", 0);
            //simulate a piece of task
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ConsoleHelper.console.display("testing");
    }

    public void display(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        display( e.getMessage(), Logger.Level.ERROR);
        LOG.error(errors.toString());
    }
}
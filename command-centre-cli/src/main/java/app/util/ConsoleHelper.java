package app.util;

import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ConsoleHelper {
    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(ConsoleHelper.class);
    public static ConsoleHelper console = new ConsoleHelper();

    public void display(String message){
        System.out.println(message);
        LOG.info(message);
    }

    public void display(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        if(e.getMessage()!=null){
            System.out.println(e.getMessage());
            LOG.error(errors.toString());
        }else{
            System.out.println("An exception occurred and logged; No error message available");
            LOG.error(errors.toString());
        }
    }

    public void display(String message, LogLevel logLevel){
        System.out.println(message);
        if(logLevel.equals(LogLevel.info)){
            LOG.info(message);
        }
        if(logLevel.equals(LogLevel.warn)){
            LOG.warn(message);
        }
        if(logLevel.equals(LogLevel.error)){
            LOG.error(message);
        }
        if(logLevel.equals(LogLevel.debug)){
            LOG.debug(message);
        }
    }
}
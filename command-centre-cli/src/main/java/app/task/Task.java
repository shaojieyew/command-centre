package app.task;

import app.util.ConsoleHelper;

import java.io.IOException;

public abstract class Task {
    public void startTask() throws Exception {
        preTask();
        ConsoleHelper.console.display(getTaskName()+" ...");
        try {
            task();
            ConsoleHelper.console.display(getTaskName()+" done");
            postTask();
        }catch (Exception e){
            ConsoleHelper.console.display(e);
        }
    }

    protected abstract void preTask() throws Exception;
    protected abstract void postTask() throws IOException, Exception;
    protected abstract String getTaskName();
    protected abstract void task() throws Exception;

}

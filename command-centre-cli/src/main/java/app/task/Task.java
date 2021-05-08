package app.task;

import app.cli.Cli;
import app.util.ConsoleHelper;

public abstract class Task {

    class JobRunnable implements Runnable {
        String jobName;
        boolean running = true;
        public JobRunnable(String jobName){
            this.jobName = jobName;
        }
        ConsoleHelper consoleHelper = new ConsoleHelper();
        public void done(){
            running = false;
            consoleHelper.animate( jobName, 1);
        }
        public void failed(){
            running = false;
            consoleHelper.animate( jobName, -1);
        }
        @Override
        public void run() {
            while(running){
                consoleHelper.animate( jobName, 0);
                //simulate a piece of task
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    Cli cli;
    JobRunnable jobRunnable;
    public void startTask() throws Exception {
        startTask(false);
    }
    public void startTask(Cli cli) throws Exception {
        this.cli = cli;
        startTask(false);
    }
    public void startTask(Cli cli, boolean disableLoading) throws Exception {
        this.cli = cli;
        startTask(disableLoading);
    }
    public void startTask(boolean disableLoading) throws Exception {
        if(disableLoading){
            task();
        }else{
        jobRunnable = new JobRunnable(getTaskName());
        new Thread(jobRunnable).start();
        try{
            task();
            jobRunnable.done();
        }catch (Exception ex){
            jobRunnable.failed();
            ex.printStackTrace();
        }
        }
    }


    protected abstract String getTaskName();
    protected abstract void task() throws Exception;

}

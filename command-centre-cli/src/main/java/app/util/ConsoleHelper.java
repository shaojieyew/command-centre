package app.util;


public class ConsoleHelper {
    private String lastLine = "";

    public void print(String line) {
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

    private byte anim;

    private final static int PROGRESS_BAR_LENGTH = 20;

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


    public static void main(String[] args) throws InterruptedException {
        ConsoleHelper consoleHelper = new ConsoleHelper();
        for (int i = 0; i < 5; i++) {
            consoleHelper.animate("asds", 0);
            //simulate a piece of task
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        consoleHelper.animate("asds", 1);
    }
}
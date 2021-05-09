package example.git;

import app.c2.services.git.GitSvc;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;

public class GitApiExample {

    static String tmpLocalRepository = "tmp";

    public static void main(String arg[]) throws GitAPIException, IOException {
        String remoteUrl = "https://gitlab.com/mrysj/command-center.git";
        GitSvc gitSvc = new GitSvc(remoteUrl,"B8UxzhjZiBDJK51ZVHxY", tmpLocalRepository);
        String configFile ="spark-app/src/main/resources/config.yml";
        String selectedBranch ="master";

        //get all branches
        List<String> branches = gitSvc.getBranches();

        //get all branches by branch name
        branches = gitSvc.getBranches(selectedBranch, GitSvc.BranchType.BOTH);
        String output = "";

        // get file from branch
        for(String b: branches){
            if(b.endsWith("/"+selectedBranch)){
                output = gitSvc.getFileAsString(b, configFile);
                System.out.println("------------"+b+"------------");
                System.out.println(output);
            }
        }

        // update files
        selectedBranch ="master2";
        String newUpdatedBranch ="master2";
        String commitMessage ="updated config";
        gitSvc.updateFile( configFile,  output,  selectedBranch,  newUpdatedBranch,  commitMessage);
    }

}

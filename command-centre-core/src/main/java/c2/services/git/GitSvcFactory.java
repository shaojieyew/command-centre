package c2.services.git;

import c2.properties.C2Properties;
import c2.properties.GitRepoServiceProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.lang.time.DateUtils;
import org.apache.zookeeper.Op;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Creating GitSvc using from C2Properties
 */
public class GitSvcFactory {
    public static List<GitSvc> create(C2Properties prop){
        List<GitSvc> svcs = new ArrayList<>();
        prop.getGitRemoteRepositoryProperties().forEach(gitProp->{
            GitSvc gitSvc = new GitSvc(gitProp.getRemoteUrl(), gitProp.getToken());
            svcs.add(gitSvc);
        });
        return svcs;
    }
    public static GitSvc create(C2Properties prop, String remoteUrl){
        Optional<GitRepoServiceProperties> repoProp =prop.getGitRemoteRepositoryProperties().stream().filter(gitProp->
            gitProp.getRemoteUrl().equalsIgnoreCase(remoteUrl)
        ).findFirst();
        if(repoProp.isPresent()){
            GitSvc gitSvc = new GitSvc(repoProp.get().getRemoteUrl(),
                    repoProp.get().getToken());
            return gitSvc;
        }
        return null;
    }
}

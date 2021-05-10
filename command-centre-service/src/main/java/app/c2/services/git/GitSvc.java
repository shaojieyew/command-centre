package app.c2.services.git;

import app.c2.services.util.FileManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for listing branches and updating files in git repository
 */
public class GitSvc {

    private String tmpLocalRepository;
    private String token = null;
    private String remoteUrl = null;

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }


    public GitSvc(String remoteUrl, String token, String tmpLocalRepository){
        this.remoteUrl= remoteUrl;
        this.token= token;
        this.tmpLocalRepository = tmpLocalRepository;
    }

    public enum BranchType{
        TAG,
        BRANCH,
        BOTH
    }

    /**
     * get all branches from a remoteURL repository
     * @return list of branches eg. /ref/head/master, /ref/tag/1.0.0-release
     * @throws GitAPIException
     * @throws IOException
     */
    public List<String> getBranches() throws GitAPIException, IOException {
        return getBranches(null, BranchType.BOTH);
    }

    /**
     * get all branches from a remoteURL repository filtered by enum BranchType and regex
     * @param patternString
     * @param type
     * @return list of branches eg. /ref/head/master, /ref/tag/1.0.0-release
     * @throws GitAPIException
     * @throws IOException
     */
    public List<String> getBranches(String patternString, BranchType type) throws GitAPIException, IOException {
        List<String> branches = new ArrayList<String>();
        Collection<Ref> refs =  Git.lsRemoteRepository()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token))
                .setRemote(remoteUrl)
                .call();

        for (Ref ref : refs) {
            if( (ref.getName().startsWith("refs/heads/") && type == BranchType.BRANCH)
                    ||  (ref.getName().startsWith("refs/tags/") && type == BranchType.TAG)
                    ||  ((ref.getName().startsWith("refs/heads/") || ref.getName().startsWith("refs/tags/")) && type == BranchType.BOTH)){
                if(patternString==null|| patternString.trim().length()==0){
                    branches.add(ref.getName());
                }else{
                    Pattern pattern = Pattern.compile(patternString.trim());
                    if(pattern.matcher(ref.getName().split("/")[ref.getName().split("/").length-1]).find()){
                        branches.add(ref.getName());
                    }
                }
            }

        }
        branches = branches.stream().filter(b->!(b.equalsIgnoreCase("head") || b.equalsIgnoreCase("merge"))).collect(Collectors.toList());
        Collections.sort(branches);
        return branches;
    }

    /**
     * return list of files paths in a branch that match the pattern
     * @param branch
     * @param patternString for filtering
     * @return list of files paths that match the pattern
     * @throws GitAPIException
     * @throws IOException
     */
    public List<String> getFilesPath(String branch, String patternString) throws GitAPIException, IOException {
        DfsRepositoryDescription repoDesc = new DfsRepositoryDescription();
        InMemoryRepository repo = new InMemoryRepository(repoDesc);
        Git git = new Git(repo);
        git.fetch()
                .setRemote(remoteUrl)
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"), new RefSpec("+refs/tags/*:refs/tags/*"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token))
                .call();
        repo.getObjectDatabase();
        ObjectId lastCommitId = repo.resolve(branch);
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(false);

        List<String> files = new ArrayList<>();

        Pattern pattern = null;
        if(patternString!=null && patternString.trim().length()>0) {
            pattern = Pattern.compile(patternString.trim());
        }

        while (treeWalk.next()) {
            if (treeWalk.isSubtree()) {
                treeWalk.enterSubtree();
            } else {
                if(pattern!=null){
                    if(pattern.matcher(treeWalk.getPathString()).find()){
                        files.add(treeWalk.getPathString());
                    }
                }else{
                    files.add(treeWalk.getPathString());
                }
            }
        }
        git.clean();
        git.close();
        return files;
    }

    /**
     * Add or update file to a branch
     * @param filePath of the file to be added/updated
     * @param content of the file to be added/updated
     * @param branch the base branch used for adding/updating the new file
     * @param newBranch is the target branch where it will perform a force push
     * @param commitMessage
     * @throws IOException
     * @return false when fails, otherwise true
     */
    public boolean updateFile(String filePath, String content, String branch, String newBranch, String commitMessage)  {
        if(newBranch==null || newBranch.length()==0){
            newBranch = branch;
        }
        String tmpDirectory = tmpLocalRepository+"/repo";
        if( new File(tmpDirectory).exists() &&  !new File(tmpDirectory).isFile()){
            // delete folders older than 2hours
            try {
                FileManager.clean(tmpDirectory, 2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new File(tmpDirectory).mkdirs();


        String tmpRepo = tmpDirectory+"/"+System.currentTimeMillis();
        File localRepoDir = new File(tmpRepo);
        Git git = null;
        try{
            Git.cloneRepository()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token))
                    .setDirectory(localRepoDir)
                    .setURI(remoteUrl).call().getRepository();
            git = Git.open(localRepoDir);
            Optional<String> developBranch = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call().stream()
                    .map(r -> r.getName()).findAny();
            git.fetch().setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token)).call();
            git.checkout().setName(developBranch.get()).setStartPoint(developBranch.get()).call();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(localRepoDir.getAbsolutePath()+"/"+filePath), "utf-8"))) {
                writer.write(content);
            }
            git.add().addFilepattern(filePath).call();

            RevCommit revCommit = git.commit().setAll(true).setMessage(commitMessage).call();
            Iterable<RevCommit> commitLog = git.log().call();
            git.branchCreate().setName(newBranch).call();
            Iterable<PushResult> pushCommand = git.push().setRemote("origin").setForce(true)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token))
                    .setRefSpecs(new RefSpec(newBranch+":"+newBranch)).call();

            File[] hiddenFiles = new File(tmpRepo).listFiles();
            for (File hiddenFile : hiddenFiles) {
                hiddenFile.delete();
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }finally{
            if(git!=null){
                git.clean();
                git.close();
            }
        }
        return false;
    }


    /**
     * Get content of a file from remote branch
     * @param branch
     * @param filePath
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public String getFileAsString(String branch, String filePath) throws GitAPIException, IOException {
        filePath = filePath.replace("\\","/");
        DfsRepositoryDescription repoDesc = new DfsRepositoryDescription();
        InMemoryRepository repo = new InMemoryRepository(repoDesc);
        Git git = new Git(repo);
        git.fetch()
                .setRemote(remoteUrl)
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"), new RefSpec("+refs/tags/*:refs/tags/*"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token))
                .call();
        repo.getObjectDatabase();
        ObjectId lastCommitId = repo.resolve(branch);
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filePath));
        if (!treeWalk.next()) {
            return null;
        }

        ObjectId objectId = treeWalk.getObjectId(0);
        ObjectLoader loader = repo.open(objectId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        loader.copyTo(stream);
        String finalString = new String(stream.toByteArray());
        return finalString;
    }

    /**
     * call getFileAsString and write it to a tmp folder and return its path as File
     * @param branch
     * @param filePath
     * @return File
     * @throws GitAPIException
     * @throws IOException
     */
    public File getFile(String branch, String filePath) throws GitAPIException, IOException {

        String tmpDirectory = tmpLocalRepository+"/file";

        if( new File(tmpDirectory).exists() &&  !new File(tmpDirectory).isFile()){
            // delete folders older than 2hours
            try {
                FileManager.clean(tmpDirectory, 2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new File(tmpDirectory).mkdirs();

        String fileName = filePath.split("/")[filePath.split("/").length-1];
        String content = getFileAsString( branch,  filePath);
        byte[] bytes = content.getBytes();
        String md5 = DigestUtils.md5Hex(bytes);
        String dir = tmpDirectory+"/"+md5;
        Path path = Paths.get(dir);
        Files.createDirectories(path);
        String tmpFileLocation = dir+"/"+fileName;
        File f = new File(tmpFileLocation);
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
        writer.write(content);
        writer.close();
        return f;
       }


    private void deleteOldFiles(String directory, int seconds) {
        Date oldestAllowedFileDate = DateUtils.addSeconds(new Date(), -seconds);
        File targetDir = new File(directory);
        Iterator<File> filesToDelete = FileUtils.iterateFiles(targetDir, new AgeFileFilter(oldestAllowedFileDate), null);
        while (filesToDelete.hasNext()) {
            try {
                FileUtils.forceDelete(filesToDelete.next());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package app.c2.services.mvnRegistry;

import app.c2.services.mvnRegistry.downloader.ArtifactDownloader;
import app.c2.services.mvnRegistry.model.Package;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO to be implemented
public class JFrogRegistrySvc extends AbstractRegistrySvc {

    public final static String type = "jfrog";

    public JFrogRegistrySvc(String url, String privateToken, String localRepository) {
        super(url, privateToken, localRepository);
    }

    @Override
    public List<Package> getPackages() {
        return null;
    }

    @Override
    public List<Package> getPackages(String group) {
        return null;
    }

    @Override
    public List<Package> getPackages(String group, String artifact) {
        return null;
    }

    @Override
    public Optional<Package> getPackage(String group, String artifact, String version) {
        return Optional.empty();
    }

}

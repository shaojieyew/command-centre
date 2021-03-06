package app.c2.service.maven;

import app.c2.service.maven.model.Package;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//TODO to be implemented
public class JFrogRegistrySvc extends AbstractRegistrySvc {

    public final static String type = "jfrog";

    public JFrogRegistrySvc(String url, String username, String password, String privateToken, String localRepository) {
        super(url,username, password, privateToken, localRepository);
    }

    @Override
    public List<Package> getPackages() {
        return new ArrayList<>();
    }

    @Override
    public List<Package> getPackages(String group) {
        return new ArrayList<>();
    }

    @Override
    public List<Package> getPackages(String group, String artifact) {
        return new ArrayList<>();
    }

    @Override
    public Optional<Package> getPackage(String group, String artifact, String version) {
        return Optional.empty();
    }

}

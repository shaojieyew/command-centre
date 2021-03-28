package c2.services.mvnRegistry;

import c2.services.mvnRegistry.model.Package;
import org.apache.log4j.Logger;
import util.Util;

import java.io.File;
import java.util.List;
import java.util.Optional;

//TODO to be implemented
public class JFrogRegistrySvc extends AbstractRegistrySvc {
    final static Logger logger = Util.initLogger(JFrogRegistrySvc.class);

    public final static String type = "jfrog";
    private String host;
    public JFrogRegistrySvc(String host) {
        this.host = host;
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

    @Override
    public File download(Package pkg) {
        return null;
    }
}

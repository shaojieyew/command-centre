package app.c2.services.mvnRegistry;

import app.c2.services.mvnRegistry.model.Package;

import java.io.File;
import java.util.List;
import java.util.Optional;

public abstract class AbstractRegistrySvc {
    String localRepository = "tmp/repository";
    public final static String type = "";

    /**
     * get list of packages in registry
     * @return
     */
    abstract public List<Package> getPackages();

    /**
     * get list of packages in registry filtered by group
     * @param group
     * @return
     */
    abstract public List<Package> getPackages(String group);

    /**
     * get list of packages in registry filtered by group and artifact
     * @param group
     * @param artifact
     * @return
     */
    abstract public List<Package> getPackages(String group, String artifact);
    /**
     * get list of packages in registry filtered by group and artifact
     * @param group
     * @param artifact
     * @return
     */
    abstract public Optional<Package> getPackage(String group, String artifact, String version);

    /**
     * download package
     * @param pkg
     * @return returns the downloaded pkg as File
     */
    abstract public File download(Package pkg);
}

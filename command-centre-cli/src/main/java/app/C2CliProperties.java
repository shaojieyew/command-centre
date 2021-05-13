package app;

import app.c2.properties.C2Properties;

public class C2CliProperties extends C2Properties {
    private String projectName;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public String toString() {
        return "C2CliProperties{" +
                "projectName='" + projectName + '\'' +
                "mavenProperties=" + getMavenProperties() +
                ", gitProperties=" + getGitProperties() +
                ", hadoopProperties=" + getHadoopYarnProperties() +
                '}';
    }
}

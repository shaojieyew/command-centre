package app.spec.spark;

import app.spec.Kind;
import app.spec.Spec;
import app.spec.SpecException;
import org.apache.commons.lang.StringUtils;

public class AppDeploymentKind extends Kind<AppDeploymentSpec> {

    public void validate() throws SpecException {
        super.validate();
        for(Spec s : getSpec()){
            AppDeploymentSpec spec = (AppDeploymentSpec)s;
            if(StringUtils.isEmpty(spec.getName())){
                throw new SpecException(getFileOrigin().getAbsolutePath()+ " - Missing spec.name");
            }
            if(StringUtils.isEmpty(spec.getJarGroupId())){
                throw new SpecException(getFileOrigin().getAbsolutePath()+ " - Missing spec.jarGroupId");
            }
            if(StringUtils.isEmpty(spec.getJarArtifactId())){
                throw new SpecException(getFileOrigin().getAbsolutePath()+ " - Missing spec.jarArtifactId");
            }
            if(StringUtils.isEmpty(spec.getJarVersion())){
                throw new SpecException(getFileOrigin().getAbsolutePath()+ " - Missing spec.jarVersion");
            }
            if(StringUtils.isEmpty(spec.getMainClass())){
                throw new SpecException(getFileOrigin().getAbsolutePath()+ " - Missing spec.mainClass");
            }
        }
    }

}

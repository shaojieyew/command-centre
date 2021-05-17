package app.spec.resource;

import app.spec.Kind;
import app.spec.Spec;
import app.spec.SpecException;

public class GroupResourceKind extends Kind<GroupResourceSpec> {
    public void validate() throws SpecException {
        super.validate();
        for(Spec s : getSpec()){
            GroupResourceSpec spec = (GroupResourceSpec)s;
            if(spec.getName()==null){
                throw new SpecException(getFileOrigin().getAbsolutePath()+ " - Missing spec.name");
            }
            if(spec.getResources()==null || spec.getResources().size()==0){
                throw new SpecException(getFileOrigin().getAbsolutePath()+ " - Missing spec.resources");
            }
        }
    }
}

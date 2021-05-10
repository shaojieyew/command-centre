package app.spec.nifi;

import app.spec.Kind;
import app.spec.Spec;
import app.spec.SpecException;
import org.apache.commons.lang.StringUtils;

public class NifiQueryKind extends Kind<NifiQuerySpec> {

    public void validate() throws SpecException {
        super.validate();
        for(Spec s : getSpec()) {
            NifiQuerySpec spec = (NifiQuerySpec) s;
            if (StringUtils.isEmpty(spec.getName())) {
                throw new SpecException(getFileOrigin().getAbsolutePath() + " - Missing spec.name");
            }
            if (StringUtils.isEmpty(spec.getQuery())) {
                throw new SpecException(getFileOrigin().getAbsolutePath() + " - Missing spec.query");
            }
        }
    }
}

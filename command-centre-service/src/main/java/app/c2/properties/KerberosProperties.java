package app.c2.properties;

import java.util.List;

public class KerberosProperties {
   private String keytab;
   private String principle;

    public String getKeytab() {
        return keytab;
    }

    public void setKeytab(String keytab) {
        this.keytab = keytab;
    }

    public String getPrinciple() {
        return principle;
    }

    public void setPrinciple(String principle) {
        this.principle = principle;
    }
}

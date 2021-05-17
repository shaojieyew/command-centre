package app.c2.properties;

import java.util.List;

public class KafkaProperties {
    private List<String> kafkaHosts;
    private KerberosProperties kerberos;

    public KerberosProperties getKerberos() {
        return kerberos;
    }

    public void setKerberos(KerberosProperties kerberos) {
        this.kerberos = kerberos;
    }

    public List<String> getKafkaHosts() {
        return kafkaHosts;
    }

    public void setKafkaHosts(List<String> kafkaHosts) {
        this.kafkaHosts = kafkaHosts;
    }
}
package app.c2.service.util;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class JaasFileManager {

    private Set<JaasConfiguration> jaasConfigurations = new HashSet<>();

    public void addJaas(JaasConfiguration jaas) throws IOException {
        jaasConfigurations.add(jaas);
    }

    public void setJaasConfig(File file) throws IOException {
        System.setProperty("java.security.auth.login.config",file.getAbsolutePath());
    }

    public void setJaasConfig(String tmpDirectory) throws IOException {
         FileManager.clean(tmpDirectory, 1, false);
        String jaas_path = tmpDirectory+"/jaas_"+System.currentTimeMillis()+".conf";
        FileUtils.forceMkdirParent(new File(jaas_path));
        FileWriter myWriter = new FileWriter(jaas_path);
        String jaasStr = getJaasStr();
        myWriter.write(jaasStr);
        myWriter.close();
        System.setProperty("java.security.auth.login.config",new File(jaas_path).getAbsolutePath());
    }


    public String getJaasStr(){
        return String.join("\n", jaasConfigurations.stream().map(s->s.toString()).collect(Collectors.toSet()));
    }

    public  Set<JaasConfiguration> getJaas(){
        return jaasConfigurations;
    }

    public static void main(String arg[]) throws IOException {
        JaasFileManager manager = new JaasFileManager();
        manager.addJaas(new JaasConfiguration("11","asd","sad","d"));
        manager.addJaas(new JaasConfiguration("sad","asd","sad","d"));
        System.out.println(manager.getJaasStr());
    }

    public static class JaasConfiguration  {
        private String entryName;
        private Map<String, String> options = new HashMap();
        public JaasConfiguration(String entryName, String principal, String keytab, String serviceName) {
            this.entryName = entryName;
            options.put("keyTab", keytab);
            options.put("principal", principal);
            options.put("useKeyTab", "true");
            options.put("storeKey", "true");
            options.put("useTicketCache", "false");
            options.put("refreshKrb5Config", "true");
            options.put("debug", "false");
            if(serviceName!=null){
                options.put("serviceName", serviceName);
            }
        }

        private String getKrb5LoginModuleName() {
            String krb5LoginModuleName;
            if (System.getProperty("java.vendor").contains("IBM")) {
                krb5LoginModuleName = "com.ibm.security.auth.module.Krb5LoginModule";
            } else {
                krb5LoginModuleName = "com.sun.security.auth.module.Krb5LoginModule";
            }

            return krb5LoginModuleName;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(entryName+" {\n");
            sb.append(getKrb5LoginModuleName()+" required\n");
            for (Map.Entry<String, String> option : options.entrySet()) {
                if(option.getValue().equalsIgnoreCase("true")||option.getValue().equalsIgnoreCase("false")){
                    sb.append(option.getKey()+"="+option.getValue()+"\n");
                }else{
                    sb.append(option.getKey()+"=\""+option.getValue()+"\"\n");
                }
            }
            sb.append(";\n");
            sb.append("};");
            return sb.toString();
        }
    }

}
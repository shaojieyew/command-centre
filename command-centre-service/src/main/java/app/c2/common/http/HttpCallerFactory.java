package app.c2.common.http;


public class HttpCallerFactory {
    public static HttpCaller create(){
        return new HttpCallerClient();
    }

    public static HttpCaller create(String principle, String keytab){
        if(principle==null || principle.length()==0 || keytab==null || keytab.length()==0){
            return create();
        }else{
            return new KerberosHttpCallerClient(principle,keytab);
        }
    }
}

package util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HttpService {

    public enum HttpMethod{
        GET,
        POST,
        PUT,
        DELETE
    }

    public static String inputStreamToString(InputStream ins) throws IOException {
        StringBuffer response = new StringBuffer();
        InputStreamReader isr = new InputStreamReader(ins);
        BufferedReader br = new BufferedReader(isr);
        String inputLine;
        while ((inputLine = br.readLine()) != null) {
            //Save a line of the response.
            response.append(inputLine + '\n');
        }
        br.close();
        return response.toString();
    }

    public static HttpURLConnection getConnection(HttpMethod method, String url, HashMap<String,String> headers, String data) throws IOException {
        URL getURL = new URL(url);
        HttpURLConnection con = (HttpURLConnection) getURL.openConnection();
        con.setConnectTimeout(60000);
        con.setRequestMethod(method.name());
        if(headers!=null){
            for (String key: headers.keySet()) {
                con.setRequestProperty(key,headers.get(key));
            }
        }
        con.setDoOutput(true);
        if(data!=null && data.length()>0){
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(data.getBytes(StandardCharsets.UTF_8));
            wr.flush();
            wr.close();
        }
        return con;
    }

}

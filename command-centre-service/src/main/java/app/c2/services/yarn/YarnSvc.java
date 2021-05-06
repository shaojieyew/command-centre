package app.c2.services.yarn;

import app.c2.common.http.HttpCaller;
import app.c2.common.http.HttpCallerClient;
import app.c2.common.http.KerberosHttpCallerClient;
import app.c2.services.yarn.model.YarnApp;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import util.HttpService;

import javax.ws.rs.core.MediaType;
import java.net.HttpURLConnection;
import java.util.*;

public class YarnSvc {
    String url;
    String applicationId;
    Map<String, String> args = new HashMap<>();

    public static YarnSvc builder(String host){
        return new YarnSvc(host);
    }
    public YarnSvc(String host){
        url= host+"/ws/v1/cluster/apps";
    }
    public YarnSvc setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }
    public YarnSvc setLimit(int limit){
        args.put("limit", Integer.toString(limit));
        return this;
    }
    public YarnSvc setUser(String user){
        args.put("user", user);
        return this;
    }
    public YarnSvc setStates(String states){
        args.put("states", states);
        return this;
    }
    public YarnSvc setApplicationTypes(String applicationTypes){
        args.put("applicationTypes", applicationTypes);
        return this;
    }
    public YarnSvc setStartedTimeBegin(Long startedTimeBegin){
        args.put("startedTimeBegin", Long.toString(startedTimeBegin));
        return this;
    }
    public YarnSvc setStartedTimeEnd(Long startedTimeEnd){
        args.put("startedTimeEnd", Long.toString(startedTimeEnd));
        return this;
    }
    public YarnSvc setFinishedTimeBegin(Long finishedTimeBegin){
        args.put("finishedTimeBegin", Long.toString(finishedTimeBegin));
        return this;
    }
    public YarnSvc setFinishedTimeEnd(Long finishedTimeEnd){
        args.put("finishedTimeEnd", Long.toString(finishedTimeEnd));
        return this;
    }
    public YarnSvc setQueue(String queue){
        args.put("queue", queue);
        return this;
    }

    /**
     * get list of yarn application
     * @return
     */
    public List<YarnApp> get(){
        String queryUrl = url;
        String parameters = "";
        for(Map.Entry<String, String> entry : args.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            parameters = parameters +key+"="+value+ "&";
        }
        if(applicationId!=null&&applicationId.length()>0){
            queryUrl = queryUrl+"/"+applicationId;
        }
        if(parameters.length()>0){
            queryUrl = queryUrl + "?"+parameters;
        }

        List<YarnApp> list = new ArrayList<>();
        String strResponse = "";
        HashMap<String,String> requestMap = new HashMap<>();
        requestMap.put("content-type","application/json");
        try {
            HttpURLConnection con = HttpService.getConnection( HttpService.HttpMethod.GET,
                    queryUrl,
                    requestMap,
                    null);
            int statusCode = con.getResponseCode();
            strResponse = HttpService.inputStreamToString(con.getInputStream());
            if(statusCode != 200){
                throw new Exception(strResponse);
            }
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(strResponse);
            JSONObject jsonAppsObject = (JSONObject)json.get("apps");
            if(jsonAppsObject==null){
                ObjectMapper mapper = new ObjectMapper(new JsonFactory());
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                if(json.get("app")!=null){
                    YarnApp app = mapper.readValue((json.get("app")).toString(), YarnApp.class);
                    list.add(app);
                }
                return list;
            }

            JSONArray jsonAppObject = (JSONArray)jsonAppsObject.get("app");
            for (int i =0;i<jsonAppObject.size();i++ ){

                ObjectMapper mapper = new ObjectMapper(new JsonFactory());
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                YarnApp app = mapper.readValue((jsonAppObject.get(i)).toString(), YarnApp.class);
                list.add(app);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * kill application by applicationId
     * @param applicationId
     * @return
     */
    public boolean kill(String applicationId){
        this.applicationId = applicationId;
        return kill();
    }

    /**
     * kill yarn application
     * applicationId
     * @return
     */
    public boolean kill(){
        try {
            if(applicationId==null){
                throw new Exception("ApplicationId not set. To kill an application with YarnAppQuery, applicationId needs to be set using setApplicationId");
            }
            String queryUrl = url+"/"+applicationId+"/state";
            HashMap<String,String> requestMap = new HashMap<>();
            requestMap.put("content-type","application/json");

            boolean enabledSpnegoHttpRequest = true;
            String principle = "";
            String keytab = "";
            HttpCaller httpCaller = new HttpCallerClient();
            if(enabledSpnegoHttpRequest){
                httpCaller = new KerberosHttpCallerClient(principle, keytab);
            }
            String requestJson = "{\"state\": \"KILLED\"}";
            StringEntity entity = new StringEntity(requestJson, "application/json");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + "xxxxxxxxxxxx");
            HttpEntity<String> entity = new HttpEntity<String>(input, headers);

            // send request and parse result
            ResponseEntity<String> response = restTemplate
                    .exchange(uri, HttpMethod.POST, entity, String.class);


            request.setEntity(HttpEntity);
            httpCaller.callRestUrl();

            HttpURLConnection con = HttpService.getConnection( HttpService.HttpMethod.PUT, queryUrl, requestMap, "{\"state\": \"KILLED\"}");
            int statusCode = con.getResponseCode();

            String strResponse = HttpService.inputStreamToString(con.getInputStream());
            if(statusCode != 200){
                throw new Exception(strResponse);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

package app.c2.service.yarn;

import app.c2.common.http.*;
import app.c2.service.yarn.model.YarnApp;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.security.auth.login.LoginException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;

public class YarnSvc {
    String url;
    String applicationId;
    Map<String, String> args = new HashMap<>();
    String principle = null;
    String keytab = null;

    public static YarnSvc builder(String host){
        return new YarnSvc(host);
    }
    public YarnSvc(String host){
        url= host+"/ws/v1/cluster/apps";
    }

    public YarnSvc setPrinciple(String principle) {
        this.principle = principle;
        return this;
    }

    public YarnSvc setKeytab(String keytab) {
        this.keytab = keytab;
        return this;
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
    public List<YarnApp> get() throws Exception {
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
        requestMap.put("content-type", MediaType.APPLICATION_JSON);
            HttpGet httpGet = new HttpGet(queryUrl);
            httpGet.setHeader("content-type",MediaType.APPLICATION_JSON);
            HttpCaller httpCaller = HttpCallerFactory.create(principle, keytab);

            HttpResponse response= httpCaller.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            strResponse = HttpUtil.httpEntityToString(response.getEntity());
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
            if (applicationId == null) {
                return false;
            }
            String queryUrl = url + "/" + applicationId + "/state";
            HttpCaller httpCaller = HttpCallerFactory.create(principle, keytab);

            String requestJson = "{\"state\": \"KILLED\"}";
            HttpPut httpPut = new HttpPut(queryUrl);
            StringEntity entity = new StringEntity(requestJson, "UTF-8");
            httpPut.setEntity(entity);
            httpPut.setHeader("Accept", MediaType.APPLICATION_JSON);
            httpPut.setHeader("Content-type", MediaType.APPLICATION_JSON);

            HttpResponse response = httpCaller.execute(httpPut);

            String body = HttpUtil.httpEntityToString(response.getEntity());
            if(body.equalsIgnoreCase(requestJson)){
                return true;
            }else{
                return false;
            }
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

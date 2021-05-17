package app.c2.service.hdfs;

import app.c2.common.http.HttpCaller;
import app.c2.common.http.HttpCallerFactory;
import app.c2.common.http.HttpUtil;
import app.c2.service.hdfs.model.FileStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper around webhdfs operations
 */
public class HdfsSvc {
    String webHdfsUrl;
    String username;
    String principle;
    String keytab;

    public String getUsername() {
        return username;
    }

    public String getPrinciple() {
        return principle;
    }

    public String getKeytab() {
        return keytab;
    }

    public HdfsSvc setUsername(String username) {
        this.username = username;
        return this;
    }

    public HdfsSvc setPrinciple(String principle) {
        this.principle = principle;
        this.username = username;
        return this;
    }

    public HdfsSvc setKeytab(String keytab) {
        this.keytab = keytab;
        return this;
    }

    public String getWebHdfsUrl() {
        return webHdfsUrl;
    }

    public HdfsSvc setWebHdfsUrl(String webHdfsUrl) {
        this.webHdfsUrl = webHdfsUrl;
        return this;
    }

    public List<FileStatus> getFileStatusList(String path) throws Exception {
        List<FileStatus> result = new ArrayList<>();
        if(path==null || path.length()==0){
            path="/";
        }
        String queryUrl = webHdfsUrl+"/webhdfs/v1"+path+"?op=LISTSTATUS";

        if(username !=null && username.length()>0){
            queryUrl = queryUrl + "&user.name="+ username;
        }

        HttpCaller httpCaller = HttpCallerFactory.create(principle, keytab);
        HttpGet httpGet = new HttpGet(queryUrl);
        httpGet.addHeader("content-type", MediaType.APPLICATION_JSON);
        HttpResponse response = httpCaller.execute(httpGet);

        int statusCode = response.getStatusLine().getStatusCode();
        String strResponse = HttpUtil.httpEntityToString(response.getEntity());
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        org.json.simple.parser.JSONParser parser = new JSONParser();
        if(statusCode != 200){
            throw new Exception(strResponse);
        }
        JSONObject json = (JSONObject)parser.parse(strResponse);
        JSONObject jsonFileStatuses = (JSONObject)json.get("FileStatuses");
        JSONArray jsonFileStatusArray = (JSONArray)jsonFileStatuses.get("FileStatus");
        for (int i =0;i<jsonFileStatusArray.size();i++ ){
            FileStatus fs = mapper.readValue((jsonFileStatusArray.get(i)).toString(), FileStatus.class);
            result.add(fs);
        }
        return result;
    }

    public boolean createDirectory(String path) throws Exception {
        if(path==null || path.length()==0){
            return false;
        }
        String queryUrl =webHdfsUrl+"/webhdfs/v1"+path+"?op=MKDIRS";
        if(username !=null && username.length()>0){
            queryUrl = queryUrl + "&user.name="+ username;
        }

        HttpCaller httpCaller = HttpCallerFactory.create(principle,keytab);
        HttpPut httpPut = new HttpPut(queryUrl);
        httpPut.addHeader("content-type",MediaType.APPLICATION_JSON);
        HttpResponse response = httpCaller.execute(httpPut);

        int statusCode = response.getStatusLine().getStatusCode();

        String strResponse = HttpUtil.httpEntityToString(response.getEntity());
        if(statusCode != 200){
            throw new Exception(strResponse);
        }
        org.json.simple.parser.JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject)parser.parse(strResponse);
        return (boolean)json.get("boolean");
    }

    public boolean deleteFile(String path, boolean recursive) throws Exception {
        if(path==null || path.length()==0){
            return false;
        }
        String queryUrl =webHdfsUrl+"/webhdfs/v1"+path+"?op=DELETE&recursive="+recursive;
        if(username !=null && username.length()>0){
            queryUrl = queryUrl + "&user.name="+ username;
        }



        HttpCaller httpCaller = HttpCallerFactory.create(principle,keytab);
        HttpDelete httpDelete = new HttpDelete(queryUrl);
        httpDelete.addHeader("content-type",MediaType.APPLICATION_JSON);
        HttpResponse response = httpCaller.execute(httpDelete);

        int statusCode = response.getStatusLine().getStatusCode();

        String strResponse = HttpUtil.httpEntityToString(response.getEntity());
        if(statusCode != 200){
            throw new Exception(strResponse);
        }
        org.json.simple.parser.JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject)parser.parse(strResponse);
        return (boolean)json.get("boolean");
    }

    public boolean renameFile(String oldPath, String newPath) throws Exception {
        if(oldPath==null || oldPath.length()==0 || newPath==null || newPath.length()==0){
            return false;
        }
        String queryUrl =webHdfsUrl+"/webhdfs/v1"+oldPath+"?op=RENAME&destination="+newPath;
        if(username !=null && username.length()>0){
            queryUrl = queryUrl + "&user.name="+ username;
        }

        HttpCaller httpCaller = HttpCallerFactory.create(principle, keytab);
        HttpPut httpPut = new HttpPut(queryUrl);
        httpPut.addHeader("content-type",MediaType.APPLICATION_JSON);
        HttpResponse response = httpCaller.execute(httpPut);

        int statusCode =response.getStatusLine().getStatusCode();

        String strResponse = HttpUtil.httpEntityToString(response.getEntity());
        if(statusCode != 200){
            throw new Exception(strResponse);
        }
        org.json.simple.parser.JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject)parser.parse(strResponse);
        return (boolean)json.get("boolean");
    }


    public String readFile(String path) throws Exception {
        String queryUrl = webHdfsUrl+"/webhdfs/v1"+path+"?op=OPEN";
        if(username !=null && username.length()>0){
            queryUrl = queryUrl + "&user.name="+ username;
        }

        HttpCaller httpCaller = HttpCallerFactory.create(principle, keytab);
        HttpGet httpGet = new HttpGet(queryUrl);
        httpGet.addHeader("content-type",MediaType.APPLICATION_JSON);
        HttpResponse response = httpCaller.execute(httpGet);
        int statusCode =response.getStatusLine().getStatusCode();
        String strResponse = HttpUtil.httpEntityToString(response.getEntity());

        if(statusCode != 200){
            throw new Exception(strResponse);
        }
        return strResponse;
    }
}

package c2.services.hdfs;

import c2.services.hdfs.model.FileStatus;
import org.apache.hadoop.fs.Path;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.HttpService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A wrapper around hdfs operations; get files, delete/rename/copy files and create directory
 */
public class HdfsSvc {
    String webHdfsUrl = "";
    String coreSiteXmlLocation ="";
    String hdfsSiteXmlLocation = "";
    String username = "";

    public String getUsername() {
        return username;
    }

    public HdfsSvc(String webHdfsUrl, String username) {
        this.webHdfsUrl = webHdfsUrl;
        this.username = username;
    }

    public String getWebHdfsUrl() {
        return webHdfsUrl;
    }

    public HdfsSvc setWebHdfsUrl(String webHdfsUrl) {
        this.webHdfsUrl = webHdfsUrl;
        return this;
    }

    public String getCoreSiteXmlLocation() {
        return coreSiteXmlLocation;
    }

    public HdfsSvc setCoreSiteXmlLocation(String coreSiteXmlLocation) {
        this.coreSiteXmlLocation = coreSiteXmlLocation;
        return this;
    }

    public String getHdfsSiteXmlLocation() {
        return hdfsSiteXmlLocation;
    }

    public HdfsSvc setHdfsSiteXmlLocation(String hdfsSiteXmlLocation) {
        this.hdfsSiteXmlLocation = hdfsSiteXmlLocation;
        return this;
    }


    public List<FileStatus> getFileStatusList(String path) throws Exception {
        List<FileStatus> result = new ArrayList<>();
        try {
            if(path==null || path.length()==0){
                path="/";
            }
            String queryUrl = webHdfsUrl+"/webhdfs/v1"+path+"?op=LISTSTATUS";

            if(username !=null && username.length()>0){
                queryUrl = queryUrl + "&user.name="+ username;
            }

            HashMap<String,String> requestMap = new HashMap<>();
            requestMap.put("content-type","application/json");
            HttpURLConnection con = null;
            con = HttpService.getConnection( HttpService.HttpMethod.GET, queryUrl, requestMap, null);
            int statusCode = con.getResponseCode();

            String strResponse = HttpService.inputStreamToString(con.getInputStream());
            if(statusCode != 200){
                throw new Exception(strResponse);
            }
            ObjectMapper mapper = new ObjectMapper(new JsonFactory());
            org.json.simple.parser.JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(strResponse);
            JSONObject jsonFileStatuses = (JSONObject)json.get("FileStatuses");
            JSONArray jsonFileStatusArray = (JSONArray)jsonFileStatuses.get("FileStatus");
            for (int i =0;i<jsonFileStatusArray.size();i++ ){
                FileStatus fs = mapper.readValue((jsonFileStatusArray.get(i)).toString(), FileStatus.class);
                result.add(fs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean createDirectory(String path)  {
        if(path==null || path.length()==0){
            return false;
        }
        try{

            String queryUrl =webHdfsUrl+"/webhdfs/v1"+path+"?op=MKDIRS";

            if(username !=null && username.length()>0){
                queryUrl = queryUrl + "&user.name="+ username;
            }

            HashMap<String,String> requestMap = new HashMap<>();
            requestMap.put("content-type","application/json");
            HttpURLConnection con = HttpService.getConnection( HttpService.HttpMethod.PUT, queryUrl, requestMap, null);
            int statusCode = con.getResponseCode();

            String strResponse = HttpService.inputStreamToString(con.getInputStream());
            if(statusCode != 200){
                throw new Exception(strResponse);
            }
            org.json.simple.parser.JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(strResponse);
            return (boolean)json.get("boolean");
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteFile(String path, boolean recursive)  {
        if(path==null || path.length()==0){
            return false;
        }
        try{

            String queryUrl =webHdfsUrl+"/webhdfs/v1"+path+"?op=DELETE&recursive="+recursive;
            if(username !=null && username.length()>0){
                queryUrl = queryUrl + "&user.name="+ username;
            }

            HashMap<String,String> requestMap = new HashMap<>();
            requestMap.put("content-type","application/json");
            HttpURLConnection con = HttpService.getConnection( HttpService.HttpMethod.DELETE, queryUrl, requestMap, null);
            int statusCode = con.getResponseCode();

            String strResponse = HttpService.inputStreamToString(con.getInputStream());
            if(statusCode != 200){
                throw new Exception(strResponse);
            }
            org.json.simple.parser.JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(strResponse);
            return (boolean)json.get("boolean");
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean renameFile(String oldPath, String newPath)  {
        if(oldPath==null || oldPath.length()==0 || newPath==null || newPath.length()==0){
            return false;
        }
        try{
            String queryUrl =webHdfsUrl+"/webhdfs/v1"+oldPath+"?op=RENAME&destination="+newPath;
            if(username !=null && username.length()>0){
                queryUrl = queryUrl + "&user.name="+ username;
            }

            HashMap<String,String> requestMap = new HashMap<>();
            requestMap.put("content-type","application/json");
            HttpURLConnection con = HttpService.getConnection( HttpService.HttpMethod.PUT, queryUrl, requestMap, null);
            int statusCode = con.getResponseCode();

            String strResponse = HttpService.inputStreamToString(con.getInputStream());
            if(statusCode != 200){
                throw new Exception(strResponse);
            }
            org.json.simple.parser.JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(strResponse);
            return (boolean)json.get("boolean");
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean copyFile(String oldPath, String newPath)  {
        if(oldPath==null || oldPath.length()==0 || newPath==null || newPath.length()==0) {
            return false;
        }
        if(hdfsSiteXmlLocation==null || hdfsSiteXmlLocation.length()==0 || coreSiteXmlLocation==null || coreSiteXmlLocation.length()==0) {
            return false;
        }
        Configuration conf = new Configuration();
        conf.addResource(hdfsSiteXmlLocation);
        conf.addResource(coreSiteXmlLocation);
        if(username !=null && username.length()>0){
            System.setProperty("HADOOP_USER_NAME", username);
        }

        FileSystem fileSystem = null;
        try {
            fileSystem = FileSystem.get(conf);
            fileSystem.listFiles(new Path("/user"), false);
            FileUtil.copy(
                    fileSystem, new Path(oldPath),
                    fileSystem, new Path(newPath),
                    false,  // move if true
                    conf
            );
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String readFile(String path)  {
     try{
        String queryUrl = webHdfsUrl+"/webhdfs/v1"+path+"?op=OPEN";
         if(username !=null && username.length()>0){
             queryUrl = queryUrl + "&user.name="+ username;
         }

         HashMap<String,String> requestMap = new HashMap<>();
        requestMap.put("content-type","application/json");
        HttpURLConnection con = HttpService.getConnection( HttpService.HttpMethod.PUT, queryUrl, requestMap, null);
        int statusCode = con.getResponseCode();

        String strResponse = HttpService.inputStreamToString(con.getInputStream());
        if(statusCode != 200){
            throw new Exception(strResponse);
        }
        return strResponse;
    } catch (IOException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }
        return null;
    }
}

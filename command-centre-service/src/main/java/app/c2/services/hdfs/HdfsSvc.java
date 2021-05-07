package app.c2.services.hdfs;

import app.c2.common.http.HttpCaller;
import app.c2.common.http.HttpCallerFactory;
import app.c2.common.http.HttpUtil;
import app.c2.services.hdfs.model.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A wrapper around hdfs operations; get files, delete/rename/copy files and create directory
 */
public class HdfsSvc {
    String webHdfsUrl;
    String coreSiteXmlLocation ;
    String hdfsSiteXmlLocation ;
    String username;
    String principle;
    String keytab;

    public String getUsername() {
        return username;
    }

    public HdfsSvc(String webHdfsUrl, String username) {
        this.webHdfsUrl = webHdfsUrl;
        this.username = username;
    }

    public HdfsSvc setUsername(String username) {
        this.username = username;
        return this;
    }

    public HdfsSvc setPrinciple(String principle) {
        this.principle = principle;
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

            HttpCaller httpCaller = HttpCallerFactory.create(principle, keytab);
            HttpGet httpGet = new HttpGet(queryUrl);
            httpGet.addHeader("content-type", MediaType.APPLICATION_JSON);
            HttpResponse response = httpCaller.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            String strResponse = HttpUtil.httpEntityToString(response.getEntity());
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

         HttpCaller httpCaller = HttpCallerFactory.create(principle, keytab);
         HttpPut httpPut = new HttpPut(queryUrl);
         httpPut.addHeader("content-type",MediaType.APPLICATION_JSON);
         HttpResponse response = httpCaller.execute(httpPut);
         int statusCode =response.getStatusLine().getStatusCode();
         String strResponse = HttpUtil.httpEntityToString(response.getEntity());

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

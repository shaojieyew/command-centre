package app.c2.common.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class HttpUtil {

    public static String httpEntityToString(HttpEntity entity) throws IOException {
        InputStream in = entity.getContent();
        String encoding = entity.getContentEncoding().getValue();
        encoding = encoding == null ? "UTF-8" : encoding;
        String body = IOUtils.toString(in, encoding);
        return body;
    }

    public static HttpEntity stringToHttpEntity(String content){
        return new StringEntity(content, "UTF-8");
    }
}

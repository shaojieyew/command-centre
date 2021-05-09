package app.c2.common.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
public abstract class HttpCaller {

    public abstract HttpResponse execute(HttpUriRequest request);

}

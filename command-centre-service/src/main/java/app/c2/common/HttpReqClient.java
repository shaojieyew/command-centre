package app.c2.common;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class HttpReqClient {
        public HttpResponse callRestUrl(final String url, final String userId, HttpUriRequest request) {
                HttpResponse httpResponse = null;
                HttpClient httpClient = HttpClientBuilder.create().build();
                try {
                        httpResponse = httpClient.execute(request);
                        httpResponse = httpResponse;
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return httpResponse;
        }
}

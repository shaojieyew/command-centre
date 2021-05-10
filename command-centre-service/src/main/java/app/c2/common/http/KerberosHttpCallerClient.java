package app.c2.common.http;

import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.Lookup;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class KerberosHttpCallerClient extends HttpCaller {

        private String principal;
        private String keytab;

        public KerberosHttpCallerClient(String principal, String keytab) {
                this.principal = principal;
                this.keytab = keytab;
        }

        public HttpResponse execute(HttpUriRequest request) throws LoginException {
                javax.security.auth.login.Configuration config = new javax.security.auth.login.Configuration() {
                        @SuppressWarnings("serial")
                        @Override
                        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                                AppConfigurationEntry[] appConfigurationEntries = {new AppConfigurationEntry(
                                        "com.sun.security.auth.module.Krb5LoginModule",
                                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                        new HashMap<String, Object>() {
                                                {
                                                        put("useTicketCache", "false");
                                                        put("useKeyTab", "true");
                                                        put("keyTab", keytab);
                                                        put("refreshKrb5Config", "true");
                                                        put("principal", principal);
                                                        put("storeKey", "true");
                                                        put("doNotPrompt", "true");
                                                        put("isInitiator", "true");
                                                        put("debug", "true");
                                                }
                                        })};
                                return appConfigurationEntries;
                        }
                };

                Set<Principal> principals = new HashSet<Principal>(1);
                principals.add(new KerberosPrincipal(principal));
                Subject sub = new Subject(false, principals, new HashSet<Object>(), new HashSet<Object>());
                        // Authentication module: Krb5Login
                        LoginContext loginContext = new LoginContext("Krb5Login", sub, null, config);
                        loginContext.login();
                        Subject serviceSubject = loginContext.getSubject();
                        return Subject.doAs(serviceSubject, new PrivilegedAction<HttpResponse>() {
                                HttpResponse httpResponse = null;

                                @Override
                                public HttpResponse run() {
                                        try {
                                                HttpClient spengoClient = buildSpengoHttpClient();
                                                httpResponse = spengoClient.execute(request);
                                                return httpResponse;
                                        } catch (IOException e) {
                                                e.printStackTrace();
                                        }
                                        return httpResponse;
                                }
                        });
        }

        private static HttpClient buildSpengoHttpClient() {
                HttpClientBuilder builder = HttpClientBuilder.create();
                Lookup<AuthSchemeProvider> authSchemeRegistry
                        = RegistryBuilder.<AuthSchemeProvider>create().register(
                        AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build();
                builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(null, -1, null), new Credentials() {
                        @Override
                        public Principal getUserPrincipal() {
                                return null;
                        }

                        @Override
                        public String getPassword() {
                                return null;
                        }
                });
                builder.setDefaultCredentialsProvider(credentialsProvider);

                // Avoid output WARN: Cookie rejected
                RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                        .build();
                builder.setDefaultRequestConfig(globalConfig);

                CloseableHttpClient httpClient = builder.build();

                return httpClient;
        }

//        public static void main(String arg[]){
//                KerberosHttpRequestClient client = new KerberosHttpRequestClient("asa","ssas");
//                HttpUriRequest request = new HttpPost("https://www.yahoo.com");
//                client.callRestUrl(request);
//        }
}

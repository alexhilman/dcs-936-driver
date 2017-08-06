package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.dcs936.model.DcsFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

/**
 */
@Singleton
public class Dcs936Client {
    private static final Logger LOG = LogManager.getLogger(Dcs936Client.class);

    private final OkHttpClient okHttpClient;

    private final URL baseUrl;
    private final String usernamePasswordAuthorization;

    @Inject
    public Dcs936Client(@Named("dcs.username") final String username,
                        @Named("dcs.password") final String password,
                        @Named("dcs.baseUrl") final URL baseUrl) {
        this.usernamePasswordAuthorization = Base64.getEncoder()
                                                   .encodeToString((username + ":" + password).getBytes());
        this.baseUrl = baseUrl;

        okHttpClient = new OkHttpClient();
        okHttpClient.interceptors()
                    .add(chain -> {
                        final Request request = chain.request();

                        final StringBuilder requestString = new StringBuilder();
                        requestString.append(request.method()).append(" ").append(request.url()).append("\n");
                        request.headers()
                               .names()
                               .forEach(header -> requestString.append("> ")
                                                               .append(header)
                                                               .append(": ")
                                                               .append(request.header(header))
                                                               .append("\n"));
                        LOG.info("Request:\n{}", requestString.toString());

                        return chain.proceed(request);
                    });
    }

    public DcsFile getRootDirectory() {
        final String url = baseUrl.toString() + SearchParams.get().withFilesPerPage(100);
        LOG.info("GET {}", url);
        final Request request = baseRequestBuilder()
                .url(url)
                .get()
                .build();
        final Call call = okHttpClient.newCall(request);
        final Response response;
        try {
            response = call.execute();
        } catch (IOException e) {
            throw new RuntimeException("Could not execute request", e);
        }
        final String responseBody;
        try {
            responseBody = response.body().string();
        } catch (IOException e) {
            throw new RuntimeException("Could not extract response body", e);
        }

        LOG.info("Response: {}", responseBody);
        return null;
    }

    private Request.Builder baseRequestBuilder() {
        return new Request.Builder()
                .header("User-Agent",
                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "en-GB,en-US;q=0.8,en;q=0.6")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Referer", "http://192.168.1.113/eng/admin/adv_sdcard.cgi")
                .header("Cookie", "language=eng; usePath=null")
                .header("Authorization", "Basic " + usernamePasswordAuthorization)
                .header("Upgrade-Insecure-Requests", "1");
    }
}

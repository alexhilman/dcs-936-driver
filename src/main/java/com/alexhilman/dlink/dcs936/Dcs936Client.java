package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.dcs936.model.DcsFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.squareup.okhttp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
@Singleton
public class Dcs936Client {
    private static final Logger LOG = LogManager.getLogger(Dcs936Client.class);

    private static final AtomicLong REQUEEST_ID = new AtomicLong(0);
    private final DcsFileInterpreter dcsFileInterpreter = new DcsFileInterpreter();

    private final OkHttpClient okHttpClient;
    private final URL baseUrl;
    private final String usernamePasswordAuthorization;

    @Inject
    public Dcs936Client(final AccessCredentials accessCredentials) {
        checkNotNull(accessCredentials, "accessCredentials cannot be null");

        this.usernamePasswordAuthorization =
                Base64.getEncoder()
                      .encodeToString((accessCredentials.getUsername() + ":" + accessCredentials.getPassword()).getBytes());

        this.baseUrl = accessCredentials.getEndpoint();

        okHttpClient = new OkHttpClient();
        okHttpClient.interceptors()
                    .add(chain -> {
                        final long id = REQUEEST_ID.incrementAndGet();

                        final Request request = chain.request();

                        final StringBuilder requestString = new StringBuilder();
                        requestString.append("> #")
                                     .append(id)
                                     .append("\n")
                                     .append("> ")
                                     .append(request.method())
                                     .append(" ")
                                     .append(request.url())
                                     .append("\n");
                        request.headers()
                               .names()
                               .forEach(header -> requestString.append("> ")
                                                               .append(header)
                                                               .append(": ")
                                                               .append(request.header(header))
                                                               .append("\n"));

                        LOG.debug("Request:\n{}", requestString.toString());

                        final Response response = chain.proceed(request);

                        final StringBuilder responseString = new StringBuilder();
                        responseString.append("< #")
                                      .append(id)
                                      .append("\n")
                                      .append("< Status=")
                                      .append(response.code())
                                      .append("\n");

                        final byte[] responseBytes = response.body().bytes();
                        final String responseBody = new String(responseBytes);
                        response.headers()
                                .names()
                                .forEach(header -> responseString.append("< ")
                                                                 .append(header)
                                                                 .append(": ")
                                                                 .append(response.header(header))
                                                                 .append("\n"));

                        responseString.append(responseBody);

                        LOG.debug("Response:\n{}", responseString.toString());

                        return response.newBuilder()
                                       .body(ResponseBody.create(response.body().contentType(), responseBytes))
                                .build();
                    });
    }

    public List<DcsFile> list() {
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

        return dcsFileInterpreter.interpret(responseBody);
    }

    private Request.Builder baseRequestBuilder() {
        return new Request.Builder()
                .header("User-Agent",
                        "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0")
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Referer", baseUrl + "/eng/admin/adv_sdcard.cgi")
                .header("Cookie", "language=eng; usePath=null")
                .header("Authorization", "Basic " + usernamePasswordAuthorization)
                .header("Upgrade-Insecure-Requests", "1");
    }
}

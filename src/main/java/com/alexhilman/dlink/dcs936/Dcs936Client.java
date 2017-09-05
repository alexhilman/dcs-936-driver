package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.dcs936.model.DcsFile;
import com.alexhilman.dlink.helper.IOStreams;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.squareup.okhttp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.alexhilman.dlink.helper.IOStreams.copyToByteArrayInputStream;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

/**
 */
@Singleton
public class Dcs936Client {
    private static final Logger LOG = LogManager.getLogger(Dcs936Client.class);
    private static final AtomicLong REQUEEST_ID = new AtomicLong(0);
    private static final String SD_EXPLORE_PATH = "/eng/admin/adv_sdcard.cgi";
    private static final String SD_DOWNLOAD_PATH = "/cgi/admin/getSDFile.cgi";
    private static final DateTimeFormatter FIRST_FOLDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss'D.mp4'");

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

                        final Response preParsedResponse = chain.proceed(request);
                        final Response responseToReturn;

                        final StringBuilder responseString = new StringBuilder();
                        responseString.append("< #")
                                      .append(id)
                                      .append("\n")
                                      .append("< Status=")
                                      .append(preParsedResponse.code())
                                      .append("\n");

                        preParsedResponse.headers()
                                         .names()
                                         .forEach(header -> responseString.append("< ")
                                                                          .append(header)
                                                                          .append(": ")
                                                                          .append(preParsedResponse.header(header))
                                                                          .append("\n"));

                        final ResponseBody body = preParsedResponse.body();
                        if (body != null && body.contentType() != null && body.contentType().type().equals("binary")) {
                            responseToReturn = preParsedResponse;
                        } else {
                            final byte[] responseBytes = preParsedResponse.body().bytes();
                            final String responseBody = new String(responseBytes);

                            responseString.append(responseBody);

                            responseToReturn =
                                    preParsedResponse.newBuilder()
                                                     .body(ResponseBody.create(preParsedResponse.body().contentType(),
                                                                               responseBytes))
                                                     .build();
                        }

                        LOG.debug("Response:\n{}", responseString.toString());

                        return responseToReturn;
                    });
    }

    List<DcsFile> list(final DcsFile file) {
        checkNotNull(file, "file cannot be null");
        checkArgument(file.isDirectory(), "file is not a folder");

        return list(file.getParentPath() + file.getFileName());
    }

    List<DcsFile> list(final String path) {
        checkNotNull(path, "path cannot be null");

        final String url =
                baseUrl.toString() +
                        SD_EXPLORE_PATH +
                        SearchParams.get()
                                    .withFilesPerPage(100)
                                    .withFolderPath(removeLeadingSlash(path));
        LOG.debug("GET {}", url);
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

        if (response.code() >= 400) {
            throw new RuntimeException("Could not list contents (status code = " + response.code() + ") of " + path);
        }

        final String responseBody;
        try {
            responseBody = response.body().string();
            response.body().close();
        } catch (IOException e) {
            throw new RuntimeException("Could not extract response body", e);
        }

        final List<DcsFile> files = dcsFileInterpreter.interpret(responseBody);
        files.forEach(this::requestSize);
        return files;
    }

    public InputStream open(final DcsFile dcsFile) throws IOException {
        checkNotNull(dcsFile, "dcsFile cannot be null");
        checkArgument(dcsFile.isFile(), "dcsFile must be a file");

        final Request request =
                baseRequestBuilder()
                        .url(baseUrl + SD_DOWNLOAD_PATH + "?file=" + dcsFile.getFileName() + "&path=" + dcsFile.getAbsoluteFileName())
                        .get()
                        .build();

        final Call call = okHttpClient.newCall(request);
        final Response response = call.execute();

        if (response.code() >= 400) {
            throw new IOException("Could not open file (status code = " + response.code() + "): " + dcsFile.getAbsoluteFileName());
        }
        try {
            return copyToByteArrayInputStream(response.body().byteStream());
        } finally {
            response.body().close();
        }
    }

    void requestSize(final DcsFile dcsFile) {
        checkNotNull(dcsFile, "dcsFile cannot be null");

        if (dcsFile.isDirectory()) {
            return;
        }

        final Request request =
                baseRequestBuilder()
                        .url(baseUrl + SD_DOWNLOAD_PATH + "?file=" + dcsFile.getFileName() + "&path=" + dcsFile.getAbsoluteFileName())
                        .get()
                        .build();

        final Call call = okHttpClient.newCall(request);
        final Response response;
        try {
            response = call.execute();
        } catch (IOException e) {
            throw new RuntimeException("Could not get the headers for file: " + dcsFile.getAbsoluteFileName(), e);
        }

        if (response.code() >= 400) {
            throw new RuntimeException("Could not get the headers for file: " + dcsFile.getAbsoluteFileName());
        }

        final String header = response.header("Content-Length");
        if (Strings.isNullOrEmpty(header)) {
            throw new RuntimeException("Server did not return the Content-Length for the requested file: " + dcsFile.getAbsoluteFileName());
        }

        dcsFile.setSize(Integer.parseInt(header));

        try {
            response.body().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String removeLeadingSlash(final String path) {
        final String pathWithoutLeadingSlash;
        if (path.startsWith("/")) {
            pathWithoutLeadingSlash = path.substring(1);
        } else {
            pathWithoutLeadingSlash = path;
        }
        return pathWithoutLeadingSlash;
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

    public List<DcsFile> findNewMoviesSince(final Instant earliestInstant) {
        final ZonedDateTime earliestDateTime = earliestInstant.atZone(ZoneId.systemDefault());
        return list("/").stream()
                        .filter(dir -> {
                            final LocalDate dirDate = LocalDate.parse(dir.getFileName(), FIRST_FOLDER_DATE_FORMAT);
                            return earliestDateTime.toLocalDate().isEqual(dirDate) ||
                                    earliestDateTime.toLocalDate().isBefore(dirDate);
                        })
                        .map(dir -> list(dir).stream()
                                             .filter(hourDir -> earliestDateTime.toLocalTime()
                                                                                .getHour() <= Integer.parseInt(hourDir.getFileName()))
                                             .collect(toList()))
                        .flatMap(List::stream)
                        .map(hourDir -> list(hourDir).stream()
                                                     .filter(file -> file.getFileName().endsWith(".mp4"))
                                                     .filter(movieFile -> {
                                                         final LocalDateTime dt =
                                                                 earliestDateTime.toLocalDate()
                                                                                 .atTime(earliestDateTime.toLocalTime());

                                                         final LocalDateTime fileDateTime =
                                                                 LocalDateTime.parse(movieFile.getFileName(),
                                                                                     FILE_DATE_FORMAT);

                                                         return dt.isEqual(fileDateTime) || dt.isBefore(fileDateTime);
                                                     })
                                                     .collect(toList()))
                        .flatMap(List::stream)
                        .collect(toList());
    }
}

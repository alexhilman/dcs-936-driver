package com.alexhilman.dlink.dcs936;

import com.alexhilman.dlink.dcs936.model.DcsFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.squareup.okhttp.*;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.alexhilman.dlink.helper.IOStreams.copyToByteArrayInputStream;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

/**
 */
@Singleton
public class Dcs936Client {
    public static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Logger LOG = LogManager.getLogger(Dcs936Client.class);
    private static final AtomicLong REQUEEST_ID = new AtomicLong(0);
    private static final String SD_EXPLORE_PATH = "/eng/admin/adv_sdcard.cgi";
    private static final String SD_DOWNLOAD_PATH = "/cgi/admin/getSDFile.cgi";
    private static final DateTimeFormatter FIRST_FOLDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
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

                        LOG.trace("Request:\n{}", requestString.toString());

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

                        LOG.trace("Response:\n{}", responseString.toString());

                        return responseToReturn;
                    });
    }

    static CharSequence basename(final String path) {
        final int endIndex;
        if (path.endsWith("/")) {
            endIndex = path.lastIndexOf('/');
        } else {
            endIndex = path.length();
        }

        final int beginIndex;
        if (path.lastIndexOf('/', endIndex - 1) >= 0) {
            beginIndex = path.lastIndexOf('/', endIndex - 1) + 1;
        } else {
            beginIndex = 0;
        }

        return path.substring(beginIndex, endIndex);
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

        return dcsFileInterpreter.interpret(responseBody)
                                 .stream()
                                 .filter(f -> f.isDirectory() || f.getFileName().endsWith(".mp4"))
                                 .map(f -> f.build(this))
                                 .collect(toList());
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

    public Flowable<DcsFile> findNewMoviesSince(final Instant sinceInstant) {
        final ZonedDateTime sinceDateTime = sinceInstant.atZone(ZoneId.systemDefault());
        final LocalDate sinceLocalDateTime = sinceDateTime.toLocalDate();
        return Flowable.fromIterable(list("/"))
                       .parallel()
                       .runOn(Schedulers.io())
                       .filter(dir -> {
                           final LocalDate dirDate = LocalDate.parse(dir.getFileName(), FIRST_FOLDER_DATE_FORMAT);
                           return sinceLocalDateTime.isEqual(dirDate) || sinceLocalDateTime.isBefore(dirDate);
                       })
                       .flatMap(dir -> {
                           final LocalDate dirDate = LocalDate.parse(dir.getFileName(), FIRST_FOLDER_DATE_FORMAT);

                           return Flowable.fromIterable(list(dir))
                                          .parallel()
                                          .runOn(Schedulers.io())
                                          .filter(hourDir -> sinceLocalDateTime.isBefore(dirDate) ||
                                                  sinceDateTime.toLocalTime()
                                                               .getHour() <= Integer.parseInt(hourDir.getFileName()))
                                          .sorted(Comparator.comparing(DcsFile::getAbsoluteFileName));
                       })
                       .flatMap(hourDir -> {
                           return Flowable.fromIterable(list(hourDir))
                                          .parallel()
                                          .runOn(Schedulers.io())
                                          .filter(movieFile -> {
                                              final LocalDateTime dt = sinceLocalDateTime.atTime(sinceDateTime.toLocalTime());

                                              return dt.isBefore(movieFile.getCreatedInstant()
                                                                          .atZone(ZoneOffset.systemDefault())
                                                                          .toLocalDateTime());
                                          })
                                          .map(dcsFile -> {
                                              LOG.info("Matching file: {}", dcsFile.getAbsoluteFileName());
                                              return dcsFile;
                                          })
                                          .sorted(Comparator.comparing(DcsFile::getCreatedInstant));
                       })
                       .sorted(Comparator.comparing(DcsFile::getCreatedInstant));
    }
}

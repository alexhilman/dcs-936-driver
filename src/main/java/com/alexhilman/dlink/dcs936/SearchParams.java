package com.alexhilman.dlink.dcs936;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 */
class SearchParams {
    private final Map<String, Object> params = new HashMap<String, Object>();

    public static SearchParams get() {
        return new SearchParams().withFolderPath("").withCommand("video");
    }

    private SearchParams withCommand(final String command) {
        params.put("command", command);
        return this;
    }

    public SearchParams withFolderPath(final String folderPath) {
        params.put("folderpath", folderPath);
        return this;
    }

    public SearchParams withFilesPerPage(final Integer filesPerPage) {
        params.put("filesperpage", filesPerPage);
        return this;
    }

    @Override
    public String toString() {
        if (params.isEmpty()) {
            return "";
        }

        return "?" + params.entrySet()
                           .stream()
                           .map(entry -> entry.getKey() + "=" + encode(entry))
                           .collect(Collectors.joining("&"));
    }

    private String encode(final Map.Entry<String, Object> entry) {
        try {
            return URLEncoder.encode(String.valueOf(entry.getValue()), "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not URL-encode entry: " + entry.getKey() + "=" + entry.getValue(), e);
        }
    }
}

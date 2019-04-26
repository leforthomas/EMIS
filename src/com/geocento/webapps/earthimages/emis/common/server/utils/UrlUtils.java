package com.geocento.webapps.earthimages.emis.common.server.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/**
 * Created by thomas on 18/09/2017.
 */
public class UrlUtils {

    // download file preserving the file name in the URL or in the content-type, if not use the product id
    public static File downloadProductFromHTTP(String urlString, String parentDirectory, String productId) throws Exception {
        return downloadProductFromHTTP(urlString, parentDirectory, productId, null);
    }

    public static File downloadProductFromHTTP(String urlString, String parentDirectory, String productId, String userName, String password) throws Exception {
        String token = userName + ":" + password;
        return downloadProductFromHTTP(urlString, parentDirectory, productId, "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8)));
    }

    public static File downloadProductFromHTTP(String urlString, String parentDirectory, String productId, String token) throws Exception {
        URL url = new URL(urlString);
        // open the connection
        URLConnection connection = url.openConnection();
        if(token != null) {
            connection.setRequestProperty("Authorization", token);
        }
        String filename = getFileName(connection);
        if (filename == null) {
            filename = productId == null ? (new Date().getTime() + "") : productId;
            String format = getFormat(connection);
            if(format == null || format.contentEquals("octet-stream")) {
                format = "zip";
            }
            filename += "." + format;
        }
        // create file in systems temporary directory
        File download = new File(parentDirectory, filename);

        // open the stream and download
        ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
        FileOutputStream fos = new FileOutputStream(download);
        try {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } finally {
            fos.close();
        }
        return download;
    }

    public static String getFormat(URLConnection connection) {
        String format = connection.getContentType();
        if(format == null) {
            return null;
        }
        return format.substring(format.lastIndexOf("/") + 1);
    }

    public static String getFileName(URLConnection connection) {
        String fieldValue = connection.getHeaderField("Content-Disposition");
        String option = "filename=";
        if (fieldValue == null || ! fieldValue.contains(option)) {
            return null;
        }
        // check if has quote or not
        int index = 0;
        // parse the file name from the header field
        String filename = fieldValue.substring(fieldValue.indexOf(option) + option.length(), fieldValue.length());
        // check for quotes
        if(filename.contains("\"")) {
            filename = filename.replaceAll("\"", "");
        }
        filename = filename.trim();
        return filename;
    }

    public static void downloadFileFromHTTP(String urlString, File file, String token) throws Exception {
        URL url = new URL(urlString);
        // open the connection
        URLConnection connection = url.openConnection();
        if(token != null) {
            connection.setRequestProperty("Authorization", token);
        }
        // open the stream and download
        ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
        FileOutputStream fos = new FileOutputStream(file);
        try {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } finally {
            fos.close();
        }
    }
}

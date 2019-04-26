package com.geocento.webapps.earthimages.emis.common.server.utils;

/**
 * Created by thomas on 24/11/2014.
 */
public class UtilImageAlert {


    public static boolean verifyHash(String imageAlertId, String emailAddress, String hash) {
        return BCrypt.checkpw(imageAlertId + emailAddress, hash);
    }

    private static String generateHash(String imageAlertId, String emailAddress) {
        return BCrypt.hashpw(imageAlertId + emailAddress, BCrypt.gensalt());
    }

    public static String generateUnregisterLink(Long id, String email) {
        return Utils.getSettings().getWebsiteUrl() + "/unregisteralert?imageAlert=" + id.toString() +
                "&emailAddress=" + email +
                "&hash=" + generateHash(id.toString(), email);
    }

}

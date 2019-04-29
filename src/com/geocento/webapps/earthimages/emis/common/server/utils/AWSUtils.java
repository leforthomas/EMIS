package com.geocento.webapps.earthimages.emis.common.server.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.geocento.webapps.earthimages.emis.common.server.ServerUtil;

/**
 * Created by thomas on 20/04/2016.
 */
public class AWSUtils {

    public static AmazonS3 getS3Client() {
        final AmazonS3 s3 = new AmazonS3Client(new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return Utils.getSettings().getAWSKey();
            }

            @Override
            public String getAWSSecretKey() {
                return Utils.getSettings().getAWSSecretKey();
            }
        });
        Region euWest = Region.getRegion(Regions.EU_WEST_1);

        s3.setRegion(euWest);

        return s3;
    }
}

package com.geocento.webapps.earthimages.emis.common.share.entities;

/**
* Created by thomas on 28/07/2017.
*/
public enum STATUS {
    created,
    canceled,
    requested,
    downloading,
    downloadingFailed,
    downloadAvailable,
    downloaded,
    fetching,
    publishing,
    publishingFailed,
    publishingSuccess,
    requestFailed, notPublished, published,
    // temporary, to support planet local ordering
    planetCreated, planetWaiting, planetDownloading
}

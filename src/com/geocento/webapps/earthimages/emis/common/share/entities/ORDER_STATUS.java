package com.geocento.webapps.earthimages.emis.common.share.entities;

public enum ORDER_STATUS {
        // user requested the products in the order
        REQUESTED,
        // supplier rejected the request
        CANCELLED,
        // supplier quoted the request
        QUOTED,
        // user rejected the quote
        QUOTEREJECTED,
        // user accepted and paid the quote
        ACCEPTED,
        // products are being generated
        INPRODUCTION,
        // failed to produce the order
        FAILED,
        // order has been completed and products are available
        COMPLETED,
        // order has been archived and products have been delivered and are not downloadable anymore
        DELIVERED,
        //The order has been closed or archived by the user, not possible to add more products
        ARCHIVED

    };


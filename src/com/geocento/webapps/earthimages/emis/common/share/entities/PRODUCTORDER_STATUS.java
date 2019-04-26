package com.geocento.webapps.earthimages.emis.common.share.entities;

public enum PRODUCTORDER_STATUS {
    // right after creation of product order
    Created,
    // submitted corresponds to after the product order was submitted to EI
    Submitted,
    // EI statuses
    ChangeRequested, Quoted, Accepted, Rejected, Cancelled,
    // documentation is when license needs signing
    Documentation,
    DocumentationProvided,
    // in production is when the product order is being generated
    InProduction, Suspended,
    Delivered, Failed,
    // completed is when the product is available locally
    Completed,
    Downloading, Unknown
};


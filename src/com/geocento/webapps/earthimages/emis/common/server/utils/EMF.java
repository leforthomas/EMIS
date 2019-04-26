package com.geocento.webapps.earthimages.emis.common.server.utils;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class EMF {

    private static final EntityManagerFactory emfInstance =
        Persistence.createEntityManagerFactory("database");

    private EMF() {}

    public static EntityManagerFactory get() {
        return emfInstance;
    }
}

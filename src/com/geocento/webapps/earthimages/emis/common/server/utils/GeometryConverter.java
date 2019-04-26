package com.geocento.webapps.earthimages.emis.common.server.utils;

import org.postgis.PGgeometry;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by thomas on 04/11/2016.
 */
@Converter(autoApply = false)
public class GeometryConverter implements AttributeConverter<String, PGgeometry> {

    @Override
    public PGgeometry convertToDatabaseColumn(String wktString) {
        if(wktString != null && wktString.length() > 0) {
            try {
                return new PGgeometry(wktString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String convertToEntityAttribute(PGgeometry pGgeometry) {
        if(pGgeometry != null) {
            return pGgeometry.getValue();
        }
        return null;
    }
}

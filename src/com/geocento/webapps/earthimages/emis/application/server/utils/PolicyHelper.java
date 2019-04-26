package com.geocento.webapps.earthimages.emis.application.server.utils;

import com.metaaps.webapps.earthimages.extapi.server.domain.policies.EULADocumentDTO;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.LicensingPolicy;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;

/**
 * Created by thomas on 16/06/14.
 */
public class PolicyHelper {

    public static EULADocumentDTO getLicensingEULADocument(LicensingPolicy licensingPolicy, String value) {
        // get options
        // basic checks first
        if(licensingPolicy == null || value == null || licensingPolicy.getParameter().getLicenseOptions() == null) {
            return null;
        }
        // now look for the matching value
        return ListUtil.findValue(licensingPolicy.getParameter().getLicenseOptions(), option -> option.getOption().contentEquals(value)).getEulaDocument();
    }

    public static String getLicensingEULAUrl(EULADocumentDTO eulaDocument) {
        return "./api/license/eula/download/" + eulaDocument.getId();
    }
}

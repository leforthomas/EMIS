package com.geocento.webapps.earthimages.emis.common.server.utils;

import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.common.share.entities.Settings;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;

/**
 * Created by thomas on 9/01/15.
 */
public class Utils {

    private static Long settingsId;

    public static Settings getSettings() {
        EntityManager em = EMF.get().createEntityManager();
        try {
            if(settingsId == null) {
                TypedQuery<Settings> query = em.createQuery("select s from Settings s", Settings.class);
                List<Settings> results = query.getResultList();
                if (results == null || results.size() == 0) {
                    return null;
                }
                Settings settings = results.get(0);
                settingsId = settings.getId();
                return settings;
            } else {
                return em.find(Settings.class, settingsId);
            }
        } finally {
            em.close();
        }
    }

    public static String displayPrice(Price totalPrice) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        currencyFormatter.setCurrency(Currency.getInstance(totalPrice.getCurrency()));
        return currencyFormatter.format(totalPrice.getValue());
    }

    public static String sanitizeEarthImagesUrl(String urlValue) {
        if(StringUtils.isEmpty(urlValue)) {
            return null;
        }
        if (!urlValue.startsWith("http")) {
            try {
                URL url = new URL(new URL("https://earthimages.geocento.com"), urlValue);
                return url.toString();
            } catch (Exception e) {
            }
        }
        return urlValue;
    }

    public static String getWebsiteUrl(String path) throws MalformedURLException {
        return new URL(new URL(getSettings().getWebsiteUrl()), path).toString();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}

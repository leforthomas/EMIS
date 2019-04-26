package com.geocento.webapps.earthimages.emis.application.server.utils;

import com.geocento.webapps.earthimages.emis.common.server.domain.ProductEntity;
import com.geocento.webapps.earthimages.emis.common.server.domain.User;
import com.geocento.webapps.earthimages.emis.common.server.utils.EMF;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.application.server.imageapi.EIAPIUtil;
import com.metaaps.webapps.earthimages.extapi.server.domain.ORBITDIRECTION;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.ProductPolicy;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Created by thomas on 23/09/2015.
 */
public class ProductRequestUtil {

    public static ProductEntity convertToProductEntity(Product product) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setModeId(product.getModeId());
        productEntity.setModeName(product.getModeName());
        productEntity.setInstrumentName(product.getInstrumentName());
        productEntity.setSatelliteName(product.getSatelliteName());
        productEntity.setSensorType(product.getSensorType());
        productEntity.setSensorBand(product.getSensorBand());
        productEntity.setSensorResolution(product.getSensorResolution());
        productEntity.setSensorInformationUrl(product.getSensorInformationUrl());
        productEntity.setStart(product.getStart());
        productEntity.setStop(product.getStop());
        productEntity.setCoordinates(product.getCoordinatesWKT());
        productEntity.setOrbit(product.getOrbit());
        productEntity.setOrbitDirection(product.getOrbitDirection() == null ? null : product.getOrbitDirection().toString());
        productEntity.setAscendingNodeDate(product.getAscendingNodeDate());
        productEntity.setCloudCoveragePercent(product.getCloudCoveragePercent());
        productEntity.setCloudCoverageStatisticsPercent(product.getCloudCoverageStatisticsPercent());
        productEntity.setCompletionTimeFromAscendingNodeDate(product.getCompletionTimeFromAscendingNodeDate());
        productEntity.setOna(product.getOna());
        productEntity.setOza(product.getOza());
        productEntity.setPolarisation(product.getPolarisation());
        productEntity.setRelativeOrbit(product.getRelativeOrbit());
        productEntity.setStartTimeFromAscendingNode(product.getStartTimeFromAscendingNode());
        productEntity.setSza(product.getSza());
        productEntity.setOptions(product.getVendorAttributes());
        return productEntity;
    }

    public static Product convertToProduct(ProductEntity productEntity) {
        Product product = new Product();
        product.setModeName(productEntity.getModeName());
        product.setInstrumentName(productEntity.getInstrumentName());
        product.setSatelliteName(productEntity.getSatelliteName());
        product.setSensorType(productEntity.getSensorType());
        product.setSensorBand(productEntity.getSensorBand());
        product.setSensorResolution(productEntity.getSensorResolution());
        product.setSensorInformationUrl(productEntity.getSensorInformationUrl());
        product.setStart(productEntity.getStart());
        product.setStop(productEntity.getStop());
        product.setCoordinatesWKT(productEntity.getCoordinates());
        product.setOrbit(productEntity.getOrbit());
        product.setOrbitDirection(productEntity.getOrbitDirection() == null ? null : ORBITDIRECTION.valueOf(productEntity.getOrbitDirection()));
        product.setAscendingNodeDate(productEntity.getAscendingNodeDate());
        product.setCloudCoveragePercent(productEntity.getCloudCoveragePercent());
        product.setCloudCoverageStatisticsPercent(productEntity.getCloudCoverageStatisticsPercent());
        product.setCompletionTimeFromAscendingNodeDate(productEntity.getCompletionTimeFromAscendingNodeDate());
        product.setOna(productEntity.getOna());
        product.setOza(productEntity.getOza());
        product.setPolarisation(productEntity.getPolarisation());
        product.setRelativeOrbit(productEntity.getRelativeOrbit());
        product.setStartTimeFromAscendingNode(productEntity.getStartTimeFromAscendingNode());
        product.setSza(productEntity.getSza());
        product.setVendorAttributes(productEntity.getOptions());
        return product;
    }

    public static boolean licenseNeedsSigning(User user, Long policyId) throws EIException {
        EntityManager em = EMF.get().createEntityManager();
        try {
            ProductPolicy productPolicy = EIAPIUtil.getPolicy(policyId);
            // TODO - check if one off license signing
            // if yes check whether the user has signed the license already
            TypedQuery<Long> query = em.createQuery("select count(s) from SignedLicense s where s.owner = :owner and s.policyId = :policyId", Long.class);
            query.setParameter("owner", user);
            query.setParameter("policyId", productPolicy.getLicensingPolicy().getId());
            return query.getResultList().get(0) == 0;
        } catch (Exception e) {
            throw new EIException(e.getMessage());
        } finally {
            em.close();
        }
    }
}

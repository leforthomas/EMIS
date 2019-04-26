package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;

public class EULARequest implements Serializable {

    String eiOrderId;
    Long productPolicyId;
    Long licensingPolicyId;
    String eulaName;
    private Long EULADocumentId;

    public EULARequest() {
    }

    public String getEiOrderId() {
        return eiOrderId;
    }

    public void setEiOrderId(String eiOrderId) {
        this.eiOrderId = eiOrderId;
    }

    public Long getLicensingPolicyId() {
        return licensingPolicyId;
    }

    public void setLicensingPolicyId(Long licensingPolicyId) {
        this.licensingPolicyId = licensingPolicyId;
    }

    public String getEulaName() {
        return eulaName;
    }

    public void setEulaName(String eulaName) {
        this.eulaName = eulaName;
    }

    public Long getProductPolicyId() {
        return productPolicyId;
    }

    public void setProductPolicyId(Long productPolicyId) {
        this.productPolicyId = productPolicyId;
    }

    public void setEULADocumentId(Long EULADocumentId) {
        this.EULADocumentId = EULADocumentId;
    }

    public Long getEULADocumentId() {
        return EULADocumentId;
    }
}

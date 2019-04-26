package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.emis.common.server.utils.GeometryConverter;

import javax.persistence.*;
import java.util.Date;

@Entity
public class ProductEntity {

	@Id
	@GeneratedValue
	Long id;
	
    @Convert(converter = GeometryConverter.class)
    String coordinates;

    Long modeId;
    String satelliteName;
    String instrumentName;
    String modeName;

    String sensorType;
    String sensorBand;
    double sensorResolution;
    String sensorInformationUrl;

    @Temporal(TemporalType.TIMESTAMP)
    Date start;
    @Temporal(TemporalType.TIMESTAMP)
    Date stop;

    Integer orbit;
    Integer relativeOrbit;
    String orbitDirection;
    String ascendingNodeDate;
    String startTimeFromAscendingNode;
    String completionTimeFromAscendingNodeDate;

    // optical parameters
    Double oza;
    Double ona;
    Double cloudCoveragePercent;
    Double cloudCoverageStatisticsPercent;
    Double sza;

    // sar parameters
    String polarisation;

    // json string according to sensor vendorAttributes
    @Column(length = 1000)
    String options;

    public ProductEntity() {
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getSatelliteName() {
        return satelliteName;
    }

    public void setSatelliteName(String satelliteName) {
        this.satelliteName = satelliteName;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getSensorBand() {
        return sensorBand;
    }

    public void setSensorBand(String sensorBand) {
        this.sensorBand = sensorBand;
    }

    public double getSensorResolution() {
        return sensorResolution;
    }

    public void setSensorResolution(double sensorResolution) {
        this.sensorResolution = sensorResolution;
    }

    public String getSensorInformationUrl() {
        return sensorInformationUrl;
    }

    public void setSensorInformationUrl(String sensorInformationUrl) {
        this.sensorInformationUrl = sensorInformationUrl;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getStop() {
        return stop;
    }

    public void setStop(Date stop) {
        this.stop = stop;
    }

    public Integer getOrbit() {
        return orbit;
    }

    public void setOrbit(Integer orbit) {
        this.orbit = orbit;
    }

    public Integer getRelativeOrbit() {
        return relativeOrbit;
    }

    public void setRelativeOrbit(Integer relativeOrbit) {
        this.relativeOrbit = relativeOrbit;
    }

    public String getOrbitDirection() {
        return orbitDirection;
    }

    public void setOrbitDirection(String orbitDirection) {
        this.orbitDirection = orbitDirection;
    }

    public String getAscendingNodeDate() {
        return ascendingNodeDate;
    }

    public void setAscendingNodeDate(String ascendingNodeDate) {
        this.ascendingNodeDate = ascendingNodeDate;
    }

    public String getStartTimeFromAscendingNode() {
        return startTimeFromAscendingNode;
    }

    public void setStartTimeFromAscendingNode(String startTimeFromAscendingNode) {
        this.startTimeFromAscendingNode = startTimeFromAscendingNode;
    }

    public String getCompletionTimeFromAscendingNodeDate() {
        return completionTimeFromAscendingNodeDate;
    }

    public void setCompletionTimeFromAscendingNodeDate(String completionTimeFromAscendingNodeDate) {
        this.completionTimeFromAscendingNodeDate = completionTimeFromAscendingNodeDate;
    }

    public Double getOza() {
        return oza;
    }

    public void setOza(Double oza) {
        this.oza = oza;
    }

    public Double getOna() {
        return ona;
    }

    public void setOna(Double ona) {
        this.ona = ona;
    }

    public Double getCloudCoveragePercent() {
        return cloudCoveragePercent;
    }

    public void setCloudCoveragePercent(Double cloudCoveragePercent) {
        this.cloudCoveragePercent = cloudCoveragePercent;
    }

    public Double getCloudCoverageStatisticsPercent() {
        return cloudCoverageStatisticsPercent;
    }

    public void setCloudCoverageStatisticsPercent(Double cloudCoverageStatisticsPercent) {
        this.cloudCoverageStatisticsPercent = cloudCoverageStatisticsPercent;
    }

    public Double getSza() {
        return sza;
    }

    public void setSza(Double sza) {
        this.sza = sza;
    }

    public String getPolarisation() {
        return polarisation;
    }

    public void setPolarisation(String polarisation) {
        this.polarisation = polarisation;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public Long getModeId() {
        return modeId;
    }

    public void setModeId(Long modeId) {
        this.modeId = modeId;
    }
}

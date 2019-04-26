package com.geocento.webapps.earthimages.emis.common.share.utils;

import com.metaaps.webapps.earthimages.extapi.server.domain.Instrument;
import com.metaaps.webapps.earthimages.extapi.server.domain.RADAR_BANDS;
import com.metaaps.webapps.earthimages.extapi.server.domain.SENSOR_TYPE;
import com.metaaps.webapps.earthimages.extapi.server.domain.SensorFilters;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;

import java.util.List;

/**
 * Created by thomas on 09/09/2015.
 */
public class SearchHelper {

    public static List<Instrument> getSensors(List<Instrument> instruments, final SensorFilters sensorFilters) {
        final Double minRes = sensorFilters.getMinResolution();
        final Double maxRes = sensorFilters.getMaxResolution();
        final SENSOR_TYPE filterType = sensorFilters.getType();
        final List<RADAR_BANDS> filterRadarBands = sensorFilters.getRadarBands();
        return ListUtil.filterValues(instruments, new ListUtil.CheckValue<Instrument>() {
            @Override
            public boolean isValue(Instrument instrument) {
                // look for non matching conditions
                // check resolution first
                if((minRes != null && instrument.getResolution() < minRes) || (maxRes != null && instrument.getResolution() > maxRes)) {
                    return false;
                }
                // check type of sensor
                SENSOR_TYPE type = instrument.getType();
                if(filterType != null) {
                    // filter type is the same, check bands now
                    if (filterType == type) {
                        switch (type) {
                            case Radar: {
                                if(instrument.getRadarBand() == null) {
                                    return false;
                                }
                                RADAR_BANDS instrumentRadarBand = instrument.getRadarBand();
                                if (filterRadarBands != null && !filterRadarBands.contains(instrumentRadarBand)) {
                                    return false;
                                }
                            }
                            break;
                            case Optical: {
                            }
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            }
        });
    }
}

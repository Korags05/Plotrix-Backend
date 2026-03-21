package com.oneneev.plotrix.service;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class H3MapperService {
    private final H3Core h3;
    private static final int RESOLUTION = 8; // city-block level granularity

    public String getCellId(double lat, double lng) {
        return h3.latLngToCellAddress(lat, lng, RESOLUTION);
    }

    // returns the 6 corner coordinates of a hexagon — used to draw it on the map
    public List<LatLng> getCellBoundary(String cellId) {
        return h3.cellToBoundary(cellId);
    }
}

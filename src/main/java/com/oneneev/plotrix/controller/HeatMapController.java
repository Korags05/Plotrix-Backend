package com.oneneev.plotrix.controller;

import com.oneneev.plotrix.model.GridCell;
import com.oneneev.plotrix.repo.GridCellRepository;
import com.oneneev.plotrix.service.H3MapperService;
import com.uber.h3core.util.LatLng;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api")
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class HeatMapController {
    private final GridCellRepository gridCellRepository;
    private final H3MapperService h3MapperService;

    @GetMapping("/heatmap")
    public ResponseEntity<Map<String, Object>> getHeatmap() {
        List<GridCell> cells = gridCellRepository.findByScoreGreaterThan(0.1);

        List<Map<String, Object>> features = new ArrayList<>();

        for (GridCell cell : cells) {
            try {
                List<LatLng> boundary = h3MapperService.getCellBoundary(cell.getCellId());

                // build GeoJSON polygon coordinates
                List<List<Double>> coords = new ArrayList<>();
                for (LatLng point : boundary) {
                    coords.add(List.of(point.lng, point.lat)); // GeoJSON is [lng, lat]
                }
                // close the polygon
                coords.add(List.of(boundary.get(0).lng, boundary.get(0).lat));

                Map<String, Object> geometry = new HashMap<>();
                geometry.put("type", "Polygon");
                geometry.put("coordinates", List.of(coords));

                Map<String, Object> properties = new HashMap<>();
                properties.put("cellId", cell.getCellId());
                properties.put("score", cell.getScore());
                properties.put("signalCount", cell.getSignalCount());

                Map<String, Object> feature = new HashMap<>();
                feature.put("type", "Feature");
                feature.put("geometry", geometry);
                feature.put("properties", properties);

                features.add(feature);

            } catch (Exception e) {
                // skip malformed cells
            }
        }

        Map<String, Object> geojson = new HashMap<>();
        geojson.put("type", "FeatureCollection");
        geojson.put("features", features);
        geojson.put("totalCells", features.size());

        return ResponseEntity.ok(geojson);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Plotrix is running");
    }
}

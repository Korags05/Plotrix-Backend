package com.oneneev.plotrix.controller;

import com.oneneev.plotrix.model.GridCell;
import com.oneneev.plotrix.repo.GridCellRepository;
import com.oneneev.plotrix.service.H3MapperService;
import com.uber.h3core.util.LatLng;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/api/v1")
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class HeatMapController {
    private final GridCellRepository gridCellRepository;
    private final H3MapperService h3MapperService;

    // supported cities with metadata
    private static final Map<String, Map<String, Object>> CITIES = new LinkedHashMap<>() {{
        put("delhi",     Map.of("name", "Delhi",     "lat", 28.6139, "lng", 77.2090, "zoom", 11));
        put("bangalore", Map.of("name", "Bangalore", "lat", 12.9716, "lng", 77.5946, "zoom", 11));
        put("hyderabad", Map.of("name", "Hyderabad", "lat", 17.3850, "lng", 78.4867, "zoom", 11));
        put("pune",      Map.of("name", "Pune",      "lat", 18.5204, "lng", 73.8567, "zoom", 11));
        put("chennai",   Map.of("name", "Chennai",   "lat", 13.0827, "lng", 80.2707, "zoom", 11));
        put("kolkata",   Map.of("name", "Kolkata",   "lat", 22.5726, "lng", 88.3639, "zoom", 11));
        put("ahmedabad", Map.of("name", "Ahmedabad", "lat", 23.0225, "lng", 72.5714, "zoom", 11));
        put("bhubaneswar", Map.of("name", "Bhubaneswar", "lat", 20.2961, "lng", 85.8245, "zoom", 12));
    }};

    @GetMapping("/cities")
    public ResponseEntity<Map<String, Object>> getCities() {
        return ResponseEntity.ok(Map.of("cities", CITIES));
    }

    @GetMapping("/heatmap")
    public ResponseEntity<Map<String, Object>> getHeatmap(@RequestParam String city) {
        String normalizedCity = city.toLowerCase().trim();

        if (!CITIES.containsKey(normalizedCity)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Unsupported city. Use /api/v1/cities to see available cities.")
            );
        }

        List<GridCell> cells = gridCellRepository.findByCityAndScoreGreaterThan(normalizedCity, 0.1);
        List<Map<String, Object>> features = new ArrayList<>();

        for (GridCell cell : cells) {
            try {
                List<LatLng> boundary = h3MapperService.getCellBoundary(cell.getCellId());
                List<List<Double>> coords = new ArrayList<>();
                for (LatLng point : boundary) {
                    coords.add(List.of(point.lng, point.lat));
                }
                coords.add(List.of(boundary.get(0).lng, boundary.get(0).lat));

                Map<String, Object> geometry = new HashMap<>();
                geometry.put("type", "Polygon");
                geometry.put("coordinates", List.of(coords));

                Map<String, Object> properties = new HashMap<>();
                properties.put("cellId", cell.getCellId());
                properties.put("score", cell.getScore());
                properties.put("signalCount", cell.getSignalCount());
                properties.put("city", cell.getCity());

                Map<String, Object> feature = new HashMap<>();
                feature.put("type", "Feature");
                feature.put("geometry", geometry);
                feature.put("properties", properties);

                features.add(feature);
            } catch (Exception e) {
                // skip malformed cells
            }
        }

        Map<String, Object> cityMeta = CITIES.get(normalizedCity);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "FeatureCollection");
        response.put("features", features);
        response.put("totalCells", features.size());
        response.put("city", normalizedCity);
        response.put("cityMeta", cityMeta);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Plotrix is running");
    }
}

# Plotrix-Backend — Property Demand Signal Engine

> The backend engine powering [Plotrix](https://github.com/Korags05/Plotrix) — real-time property demand heatmaps for Indian cities.

**Live API:** `https://plotrix-backend.onrender.com` · **Frontend:** [Plotrix](https://github.com/Korags05/Plotrix)

---

## What does this do?

This is a Spring Boot backend that:

1. Ingests property search signals (lat/lng + city)
2. Maps coordinates to H3 hexagonal grid cells at resolution 8 (city-block level)
3. Atomically upserts demand scores in PostgreSQL — safe under concurrent load
4. Runs an hourly decay job that degrades stale scores using exponential decay
5. Serves a GeoJSON heatmap API consumed by the frontend

No ML. No black box. Just geometry, decay functions, and careful concurrency.

---

## Architecture

```
POST /api/v1/signals
        │
        ▼
 SignalController
        │
        ▼
 SignalService
        │
        ▼
 H3MapperService
 lat/lng → H3 cell ID (resolution 8)
        │
        ▼
 GridCellRepository
 atomic upsert via ON CONFLICT DO UPDATE
        │
        ▼
 PostgreSQL (Supabase)

────────────────────────────────

 DecayScheduler (@Scheduled hourly)
 score = score × e^(−0.05 × hoursElapsed)

────────────────────────────────

GET /api/v1/heatmap?city=delhi
        │
        ▼
 HeatmapController
 GridCell rows → GeoJSON FeatureCollection
        │
        ▼
 Frontend (Leaflet.js)
```

---

## Tech stack

| Layer | Technology |
|---|---|
| Backend framework | Spring Boot 3.2 |
| Language | Java 17 |
| Database | PostgreSQL (Supabase) |
| Geospatial indexing | Uber H3 4.4.0 |
| ORM | Spring Data JPA |
| Deployment | Render |
| Containerisation | Docker |

---

## API reference

### `GET /api/v1/heatmap?city={city}`

Returns a GeoJSON FeatureCollection of H3 hexagonal cells with demand scores.

```json
{
  "type": "FeatureCollection",
  "city": "delhi",
  "totalCells": 177,
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "Polygon",
        "coordinates": [[...]]
      },
      "properties": {
        "cellId": "8865b1a6fffffff",
        "score": 6.4,
        "signalCount": 12,
        "city": "delhi"
      }
    }
  ]
}
```

---

### `POST /api/v1/signals`

Ingests a property search signal. Fire this when a user searches for a property.

**Request body:**
```json
{
  "latitude": 28.6139,
  "longitude": 77.2090,
  "city": "delhi",
  "propertyType": "apartment"
}
```

**Response:** `200 OK — Signal recorded`

---

### `GET /api/v1/cities`

Returns all supported cities with coordinates and zoom levels.

```json
{
  "cities": {
    "delhi":     { "name": "Delhi",     "lat": 28.6139, "lng": 77.2090, "zoom": 11 },
    "bangalore": { "name": "Bangalore", "lat": 12.9716, "lng": 77.5946, "zoom": 11 },
    ...
  }
}
```

---

### `GET /api/v1/health`

```
200 OK — DemandGrid is running
```

---

## Supported cities

Delhi · Bangalore · Hyderabad · Pune · Chennai · Kolkata · Ahmedabad · Bhubaneswar

---

## Key technical decisions

### H3 hexagonal indexing at resolution 8
Each hexagon covers roughly one city block (~460m edge length). H3 is the same indexing system Uber uses for surge pricing. Using hexagons instead of square grids eliminates directional bias and gives uniform neighbour distances.

### Atomic upserts with `ON CONFLICT DO UPDATE`
Multiple users can search the same area simultaneously. Rather than a read-modify-write cycle (which causes race conditions), every signal is handled with a single atomic PostgreSQL upsert:

```sql
INSERT INTO grid_cells (cell_id, city, score, ...)
VALUES (?, ?, 1.0, ...)
ON CONFLICT (cell_id)
DO UPDATE SET
    score = grid_cells.score + 1.0,
    signal_count = grid_cells.signal_count + 1
```

No locks, no race conditions, no lost writes.

### Exponential time decay
Raw signal counts would make the heatmap a historical artifact. The decay scheduler runs hourly and applies:

```
score = score × e^(−λ × hoursElapsed)
```

With `λ = 0.05`, a signal from 24 hours ago contributes ~30% of a fresh signal. The heatmap always reflects current demand.

---

## Local setup

**Prerequisites:** Java 17, Maven, Docker

```bash
# clone the repo
git clone https://github.com/Korags05/Plotrix-Backend.git
cd Plotrix-Backend

# start PostgreSQL via Docker
docker-compose up -d

# set environment variable
export DATABASE_URL=jdbc:postgresql://localhost:5432/demandgrid?user=admin&password=password

# run the application
./mvnw spring-boot:run
```

API available at `http://localhost:8080`

---

## Project structure

```
src/main/java/com/demandgrid/
├── DemandGridApplication.java     # entry point, enables scheduling
├── config/
│   └── CorsConfig.java            # CORS configuration
├── controller/
│   ├── SignalController.java      # POST /api/v1/signals
│   └── HeatmapController.java     # GET /api/v1/heatmap, cities, health
├── service/
│   ├── SignalService.java         # signal processing logic
│   ├── H3MapperService.java       # H3 coordinate → cell ID mapping
│   └── DecayScheduler.java        # hourly exponential decay job
├── repository/
│   └── GridCellRepository.java    # JPA repository with atomic upsert
├── model/
│   └── GridCell.java              # JPA entity
└── dto/
    └── SignalRequest.java         # incoming signal request body
```

---

## Deployment

Deployed on **Render** using Docker. Auto-deploys on push to `main`.

**Environment variables required on Render:**

| Key | Value |
|---|---|
| `DATABASE_URL` | your Supabase JDBC connection string |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | your Supabase DB password |

**Supabase SSL note:** append `&sslmode=require` to your `DATABASE_URL` if connection is refused.

---

## Related

- **Frontend repo:** [Plotrix](https://github.com/Korags05/Plotrix)
- **Live app:** [plotrix.pages.dev](https://plotrix.pages.dev)

---

## Author

**Kunal Saha** — building [OneNeev](https://www.linkedin.com/company/oneneev), OWN REAL ESTATE • TOGETHER

package com.oneneev.plotrix.repo;

import com.oneneev.plotrix.model.GridCell;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GridCellRepository extends JpaRepository<GridCell, Integer> {
    List<GridCell> findByCityAndScoreGreaterThan(String city, Double threshold);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO grid_cells (cell_id, city, score, signal_count, last_signal_at, last_decayed_at)
        VALUES (:cellId, :city, 1.0, 1, NOW(), NOW())
        ON CONFLICT (cell_id)
        DO UPDATE SET
            score = grid_cells.score + 1.0,
            signal_count = grid_cells.signal_count + 1,
            last_signal_at = NOW()
        """, nativeQuery = true)
    void upsertSignal(@Param("cellId") String cellId, @Param("city") String city);
}

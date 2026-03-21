package com.oneneev.plotrix.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grid_cells")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GridCell {

    @Id
    @Column(name = "cell_id", nullable = false)
    private String cellId;

    @Column(nullable = false)
    private Double score = 0.0;

    @Column(name = "signal_count", nullable = false)
    private Integer signalCount = 0;

    @Column(name = "last_signal_at")
    private LocalDateTime lastSignalAt;

    @Column(name = "last_decayed_at")
    private LocalDateTime lastDecayedAt;

}

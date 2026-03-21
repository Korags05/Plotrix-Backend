package com.oneneev.plotrix.scheduler;

import com.oneneev.plotrix.model.GridCell;
import com.oneneev.plotrix.repo.GridCellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DecayScheduler {
    private final GridCellRepository gridCellRepository;

    private static final double LAMBDA = 0.05; //decay rate
    // runs every hour
    @Scheduled(fixedRate = 3_600_000)
    public void applyDecay() {
        List<GridCell> cells = gridCellRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (GridCell cell : cells) {
            if (cell.getLastDecayedAt() == null) continue;

            long hoursElapsed = ChronoUnit.HOURS.between(cell.getLastDecayedAt(), now);
            if (hoursElapsed < 1) continue;

            // score = score × e^(−λ × hours)
            double decayed = cell.getScore() * Math.exp(-LAMBDA * hoursElapsed);
            cell.setScore(Math.max(decayed, 0.0));
            cell.setLastDecayedAt(now);
        }

        gridCellRepository.saveAll(cells);
        System.out.println("Decay applied to " + cells.size() + " cells at " + now);
    }
}

package com.oneneev.plotrix.service;

import com.oneneev.plotrix.dto.SignalRequest;
import com.oneneev.plotrix.repo.GridCellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignalService {
    private final GridCellRepository gridCellRepository;
    private final H3MapperService h3MapperService;

    public void process(SignalRequest request) {
        String cellId = h3MapperService.getCellId(request.getLatitude(), request.getLongitude());
        gridCellRepository.upsertSignal(cellId); // atomic — safe under concurrent load
    }
}

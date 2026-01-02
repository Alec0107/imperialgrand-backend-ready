package com.imperialgrand.backend.reservations.Tables;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TableRepositoryServiceV1 {

    private final TableRepositoryV1 tableRepo;

    public List<Tables> getCandidateTables(int partySize){
       return tableRepo.findCandidateTables(partySize);
    }

    public Optional<Tables> findTableById(int tableId){
        return tableRepo.findById(Long.valueOf(tableId));
    }

}

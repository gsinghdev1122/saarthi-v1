package com.csd.canteen.service;

import com.csd.canteen.dto.response.InventoryItemResponse;
import com.csd.canteen.mapper.CanteenMapper;
import com.csd.canteen.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final CanteenMapper mapper;

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> search(String division, String search, Pageable pageable) {
        String normalizedDivision = (division == null || division.isBlank()) ? null : division;
        //String normalizedSearch = (search == null || search.isBlank()) ? null : search;
        // Prepare search term directly in Java
        String normalizedSearch = null;
        if (search != null && !search.isBlank()) {
            normalizedSearch = "%" + search.trim().toLowerCase() + "%";
        }
        return inventoryItemRepository.search(normalizedDivision, normalizedSearch, pageable)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}

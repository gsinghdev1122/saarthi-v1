package com.csd.canteen.controller;

import com.csd.canteen.dto.response.InventoryItemResponse;
import com.csd.canteen.service.InventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryItemResponse> list(
            @RequestParam(required = false) String division,
            @RequestParam(required = false) String search) {
        return inventoryService.search(division, search, null);
    }
}

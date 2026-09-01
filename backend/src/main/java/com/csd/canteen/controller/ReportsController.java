package com.csd.canteen.controller;

import com.csd.canteen.dto.response.ReportsOverviewResponse;
import com.csd.canteen.service.ReportsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports")
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/overview")
    public ReportsOverviewResponse overview() {
        return reportsService.overview();
    }
}

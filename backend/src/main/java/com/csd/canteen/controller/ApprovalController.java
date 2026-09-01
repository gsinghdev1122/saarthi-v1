package com.csd.canteen.controller;

import com.csd.canteen.dto.response.ApprovalResponse;
import com.csd.canteen.service.ApprovalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Tag(name = "Finance")
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping
    public List<ApprovalResponse> list() {
        return approvalService.listAll();
    }
}

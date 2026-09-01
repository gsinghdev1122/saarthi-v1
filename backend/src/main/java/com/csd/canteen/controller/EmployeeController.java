package com.csd.canteen.controller;

import com.csd.canteen.dto.request.CreateEmployeeRequest;
import com.csd.canteen.dto.response.EmployeeResponse;
import com.csd.canteen.service.EmployeeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Workforce")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeResponse> list() {
        return employeeService.listAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CANTEEN_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody CreateEmployeeRequest request) {
        return employeeService.create(request);
    }
}

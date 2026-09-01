package com.csd.canteen.service;

import com.csd.canteen.dto.request.CreateEmployeeRequest;
import com.csd.canteen.dto.response.EmployeeResponse;
import com.csd.canteen.entity.Employee;
import com.csd.canteen.exception.ConflictException;
import com.csd.canteen.mapper.CanteenMapper;
import com.csd.canteen.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CanteenMapper mapper;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> listAll() {
        return employeeRepository.findAllByOrderByNameAsc().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new ConflictException("An employee with code '" + request.employeeCode() + "' already exists");
        }
        String status = deriveStatus(request.contractEnd());
        Employee employee = Employee.builder()
                .name(request.name())
                .employeeCode(request.employeeCode())
                .category(request.category())
                .designation(request.designation())
                .attendance(BigDecimal.ZERO)
                .contractEnd(request.contractEnd())
                .status(status)
                .build();
        return mapper.toResponse(employeeRepository.save(employee));
    }

    private String deriveStatus(LocalDate contractEnd) {
        if (contractEnd == null) return "active";
        LocalDate today = LocalDate.now();
        if (contractEnd.isBefore(today)) return "inactive";
        long daysUntilEnd = ChronoUnit.DAYS.between(today, contractEnd);
        if (daysUntilEnd <= 60) return "expiring";
        return "active";
    }
}

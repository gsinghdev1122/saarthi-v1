package com.csd.canteen.service;

import com.csd.canteen.dto.request.CreateEmployeeRequest;
import com.csd.canteen.dto.response.EmployeeResponse;
import com.csd.canteen.entity.Employee;
import com.csd.canteen.exception.ConflictException;
import com.csd.canteen.mapper.CanteenMapper;
import com.csd.canteen.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    private final CanteenMapper mapper = new CanteenMapper();
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(employeeRepository, mapper);
    }

    @Test
    void createsEmployeeWithActiveStatusWhenNoContractEnd() {
        CreateEmployeeRequest request = new CreateEmployeeRequest("Test Person", "CSD-999", "Permanent", "Clerk", null);
        when(employeeRepository.existsByEmployeeCode("CSD-999")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        EmployeeResponse response = service.create(request);

        assertThat(response.status()).isEqualTo("active");
        assertThat(response.attendance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void marksEmployeeExpiringWhenContractEndsWithin60Days() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Test Person", "CSD-998", "Contractual", "Helper", LocalDate.now().plusDays(30));
        when(employeeRepository.existsByEmployeeCode("CSD-998")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = service.create(request);

        assertThat(response.status()).isEqualTo("expiring");
    }

    @Test
    void rejectsDuplicateEmployeeCode() {
        CreateEmployeeRequest request = new CreateEmployeeRequest("Dup", "CSD-001", "Permanent", "Clerk", null);
        when(employeeRepository.existsByEmployeeCode("CSD-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CSD-001");

        verify(employeeRepository, never()).save(any());
    }
}

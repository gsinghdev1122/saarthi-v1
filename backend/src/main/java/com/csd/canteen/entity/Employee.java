package com.csd.canteen.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "employee_code", nullable = false, unique = true)
    private String employeeCode;

    @Column(nullable = false)
    private String category; // Permanent | Contractual | Daily Wage

    @Column(nullable = false)
    private String designation;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal attendance = BigDecimal.ZERO;

    @Column(name = "contract_end")
    private LocalDate contractEnd;

    @Column(nullable = false)
    @Builder.Default
    private String status = "active"; // active | expiring | inactive

    @Column(nullable = false)
    @Builder.Default
    private String canteen = "Delhi Cantt";
}

package com.csd.canteen.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "index_no", nullable = false, columnDefinition = "text")
    private String indexNo;

    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;

    @Column(nullable = false)
    private String division; // Grocery | Liquor

    @Column(name = "closing_stock", nullable = false)
    @Builder.Default
    private Integer closingStock = 0;

    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private Integer reorderLevel = 10;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal value = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private String trend = "stable"; // up | down | stable

    @Column(nullable = false)
    @Builder.Default
    private String status = "healthy"; // healthy | low | dead

    @Column(nullable = false)
    @Builder.Default
    private String canteen = "Delhi Cantt";
}

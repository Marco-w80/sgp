package com.sgp.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "maintenance_log")
public class MaintenanceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;              // data da manutenção

    @Column(nullable = false)
    private Integer durationMinutes;     // duração em minutos

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType type;        // enum com tipos de manutenção

    @Column(length = 500)
    private String description;          // descrição do que foi feito

    @Column(nullable = false)
    private String performedBy;          // “Samuel” ou “Marco”

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public MaintenanceType getType() {
        return type;
    }

    public void setType(MaintenanceType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
    

}

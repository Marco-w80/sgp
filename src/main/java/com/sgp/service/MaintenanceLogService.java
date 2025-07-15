package com.sgp.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sgp.model.MaintenanceLog;
import com.sgp.repository.MaintenanceLogRepository;

@Service
public class MaintenanceLogService {
    private final MaintenanceLogRepository repo;

    public MaintenanceLogService(MaintenanceLogRepository repo) {
        this.repo = repo;
    }

    public MaintenanceLog save(MaintenanceLog log) {
        return repo.save(log);
    }

    public List<MaintenanceLog> findAll() {
        return repo.findAllByOrderByDateDesc();
    }

    public int getTotalMinutes() {
        return repo.sumTotalMinutes();
    }

    public int getMonthlyMinutes() {
    YearMonth ym = YearMonth.now();
    LocalDate ini = ym.atDay(1);
    LocalDate fim = ym.atEndOfMonth();
    return repo.sumMinutesBetween(ini, fim);
  }
  
}

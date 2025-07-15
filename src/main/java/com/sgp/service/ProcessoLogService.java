package com.sgp.service;

import com.sgp.model.Processo;
import com.sgp.model.ProcessoLog;
import com.sgp.repository.ProcessoLogRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProcessoLogService {

    private final ProcessoLogRepository logRepository;

    public ProcessoLogService(ProcessoLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void logIfChanged(Processo processo, String campo, Object antigo, Object novo) {
        if (!Objects.equals(antigo, novo)) {
            ProcessoLog log = new ProcessoLog(processo, campo,
                    antigo != null ? antigo.toString() : null,
                    novo != null ? novo.toString() : null);
            logRepository.save(log);
        }
    }
}

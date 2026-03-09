package com.sgp.service;

import com.sgp.model.Processo;
import com.sgp.model.ProcessoLog;
import com.sgp.model.Usuario;
import com.sgp.repository.ProcessoLogRepository;
import com.sgp.repository.ProcessoRepository;
import com.sgp.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class ProcessoLogService {

    private final ProcessoLogRepository logRepository;
    private final ProcessoRepository processoRepository;
    private final UsuarioRepository usuarioRepository;

    public ProcessoLogService(ProcessoLogRepository logRepository,
                              ProcessoRepository processoRepository,
                              UsuarioRepository usuarioRepository) {
        this.logRepository = logRepository;
        this.processoRepository = processoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void logIfChanged(Processo processo, String campo, Object antigo, Object novo) {
        if (!Objects.equals(antigo, novo)) {
            ProcessoLog log = new ProcessoLog(processo, "EDICAO", getUsuarioLogado(), campo,
                    antigo != null ? antigo.toString() : null,
                    novo != null ? novo.toString() : null);
            logRepository.save(log);
        }
    }

    public void registrarAcesso(Processo processo) {
        LocalDateTime agora = LocalDateTime.now();
        String usuario = getUsuarioLogado();

        ProcessoLog log = new ProcessoLog(processo, "ACESSO", usuario, "ACESSO", null, null);
        log.setDataHora(agora);
        logRepository.save(log);

        processo.setUltimoAcessoEm(agora);
        processo.setUltimoAcessoPor(usuario);
        processoRepository.save(processo);
    }

    public void registrarEdicao(Processo processo) {
        LocalDateTime agora = LocalDateTime.now();
        String usuario = getUsuarioLogado();

        ProcessoLog log = new ProcessoLog(processo, "EDICAO", usuario, "EDICAO", null, null);
        log.setDataHora(agora);
        logRepository.save(log);

        processo.setUltimaEdicaoEm(agora);
        processo.setUltimaEdicaoPor(usuario);
        processoRepository.save(processo);
    }

    private String getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return "Sistema";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return obterNomeOuEmail(userDetails.getUsername());
        }
        if (principal instanceof String str && !str.isBlank()) {
            return obterNomeOuEmail(str);
        }
        return "Sistema";
    }

    private String obterNomeOuEmail(String login) {
        return usuarioRepository.findByEmail(login)
                .map(Usuario::getNome)
                .filter(nome -> nome != null && !nome.isBlank())
                .orElse(login);
    }
}

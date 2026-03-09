package com.sgp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.sgp.dto.ProcessoPendenteDTO;
import com.sgp.dto.ProcessoAcompanhamentoDTO;
import com.sgp.model.Processo;
import com.sgp.model.ProcessoExcluido;
import com.sgp.model.StatusProcesso;
import com.sgp.repository.ProcessoExcluidoRepository;
import com.sgp.repository.ProcessoProdutoRepository;
import com.sgp.repository.ProcessoRepository;

@Service
public class ProcessoService {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private ProcessoProdutoRepository processoProdutoRepository;

    @Autowired
    private ProcessoExcluidoRepository processoExcluidoRepository;

    public List<ProcessoPendenteDTO> listarProcessosComDocumentacaoPendenteComMinDias(int diasMinimos) {
        return listarProcessosComDocumentacaoPendenteComMinDias(diasMinimos, null, null);
    }

    public List<ProcessoPendenteDTO> listarProcessosComDocumentacaoPendenteComMinDias(int diasMinimos,
                                                                                        LocalDate de,
                                                                                        LocalDate ate) {
        LocalDate hoje = LocalDate.now();

        return processoRepository.findAll().stream()
                .filter(this::temPendenciaDocumento)
                .filter(p -> de == null || !p.getDataInicio().isBefore(de))
                .filter(p -> ate == null || !p.getDataInicio().isAfter(ate))
                .map(p -> {
                    long dias = ChronoUnit.DAYS.between(p.getDataInicio(), hoje);
                    return new AbstractMap.SimpleEntry<>(p, dias);
                })
                .filter(e -> e.getValue() >= diasMinimos)
                .map(e -> new ProcessoPendenteDTO(
                        e.getKey(),
                        documentosFaltando(e.getKey()),
                        e.getValue()))
                .toList();
    }

    private boolean temPendenciaDocumento(Processo p) {
        return !p.isCpfAnexado()
                || !p.isCompResidenciaAnexado()
                || !p.isCompRendaAnexado()
                || !p.isProcuracaoAnexado()
                || !p.isDeclaracaoInsuficienciaAnexado();
    }

    private List<String> documentosFaltando(Processo p) {
        List<String> docs = new ArrayList<>();
        if (!p.isCpfAnexado())
            docs.add("CPF");
        if (!p.isCompResidenciaAnexado())
            docs.add("Comprovante de Residência");
        if (!p.isCompRendaAnexado())
            docs.add("Comprovante de Renda");
        if (!p.isProcuracaoAnexado())
            docs.add("Procuração");
        if (!p.isDeclaracaoInsuficienciaAnexado())
            docs.add("Declaração de Insuficiência");
        return docs;
    }

    public Map<StatusProcesso, Long> contarPorStatus() {
        Map<StatusProcesso, Long> mapa = new EnumMap<>(StatusProcesso.class);
        for (StatusProcesso st : StatusProcesso.values()) {
            mapa.put(st, processoRepository.countByStatus(st));
        }
        return mapa;
    }

    public List<ProcessoAcompanhamentoDTO> listarAcompanhamento(Integer diasSemAcesso, Integer diasSemEdicao) {
        return processoRepository.buscarAcompanhamento(diasSemAcesso, diasSemEdicao)
                .stream()
                .map(row -> new ProcessoAcompanhamentoDTO(
                        row[0] != null ? ((Number) row[0]).longValue() : null,
                        (String) row[1],
                        row[2] != null ? ((Timestamp) row[2]).toLocalDateTime() : null,
                        (String) row[3],
                        row[4] != null ? ((Timestamp) row[4]).toLocalDateTime() : null,
                        (String) row[5],
                        row[6] != null ? ((Number) row[6]).longValue() : null,
                        row[7] != null ? ((Number) row[7]).longValue() : null
                ))
                .toList();
    }

    /**
     * Exclui um processo e todos os seus produtos associados de forma transacional.
     * Primeiro remove os registros filhos (processo_produto) e depois o pai
     * (processos).
     *
     * @param id O ID do processo a ser excluído.
     */
    @Transactional
    public void excluir(Long id) {
        // 1. Busca o processo que será excluído da tabela original
        Processo processoOriginal = processoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado com o ID: " + id));

        // 2. Converte (mapeia) os dados para a entidade de histórico
        ProcessoExcluido processoArquivado = mapearParaProcessoExcluido(processoOriginal);

        // 3. SALVA a "fotografia" do processo na tabela de excluídos
        processoExcluidoRepository.save(processoArquivado);

        // 4. Exclui os registros da tabela de junção 'processo_produto' (como antes)
        processoProdutoRepository.deleteByProcessoId(id);

        // 5. Exclui o processo da tabela original 'processos' (como antes)
        processoRepository.deleteById(id);
    }

    /**
     * Método privado que converte um objeto Processo em um ProcessoExcluido,
     * "achatando" os dados e adicionando informações de auditoria.
     */
    private ProcessoExcluido mapearParaProcessoExcluido(Processo original) {
        ProcessoExcluido excluido = new ProcessoExcluido();

        // Mapeia os dados diretos
        excluido.setId(original.getId());
        excluido.setNumeroInterno(original.getNumeroInterno());
        excluido.setNumeroProcesso(original.getNumeroProcesso());
        excluido.setDataInicio(original.getDataInicio());
        excluido.setStatus(original.getStatus());
        excluido.setTipoHospital(original.getTipoHospital());
        excluido.setObs(original.getObs());

        // Mapeia os booleanos de documentos
        excluido.setCpfAnexado(original.isCpfAnexado());
        excluido.setCompResidenciaAnexado(original.isCompResidenciaAnexado());
        excluido.setCompRendaAnexado(original.isCompRendaAnexado());
        excluido.setProcuracaoAnexado(original.isProcuracaoAnexado());
        excluido.setDeclaracaoInsuficienciaAnexado(original.isDeclaracaoInsuficienciaAnexado());

        // Mapeia os dados "achatados" com verificação de nulo
        if (original.getPaciente() != null) {
            excluido.setPacienteNome(original.getPaciente().getNome());
            excluido.setPacienteCpf(original.getPaciente().getCpf());
        }
        if (original.getAdvogado() != null) {
            excluido.setAdvogadoNome(original.getAdvogado().getNome());
            excluido.setAdvogadoOab(original.getAdvogado().getOab());
        }
        if (original.getMedico() != null) {
            excluido.setMedicoNome(original.getMedico().getNome());
            excluido.setMedicoCrm(original.getMedico().getCrm());
        }
        if (original.getHospital() != null) {
            excluido.setHospitalNome(original.getHospital().getNome());
        }
        if (original.getDoenca() != null) {
            excluido.setDoencaNome(original.getDoenca().getNome());
        }
        if (original.getLocal() != null) {
            excluido.setLocalDescricao(original.getLocal().getComarca() + " – " + original.getLocal().getLocalizacao());
        }

        // Preenche os dados de auditoria
        excluido.setDataExclusao(LocalDateTime.now());
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            excluido.setUsuarioExclusao(((UserDetails) principal).getUsername());
        } else {
            excluido.setUsuarioExclusao("Sistema"); // Fallback
        }

        return excluido;
    }

}

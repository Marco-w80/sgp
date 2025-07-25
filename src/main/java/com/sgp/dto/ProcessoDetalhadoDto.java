package com.sgp.dto;

import com.sgp.model.Processo;
import com.sgp.model.ProcessoLog;
import com.sgp.model.ProcessoProduto;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessoDetalhadoDto {

    public Long id;
    public String numeroInterno;
    public String numeroProcesso;
    public String dataInicio;
    public String status;
    public String obs;

    public PessoaDto paciente;
    public PessoaDto advogado;
    public PessoaDto medico;

    public LocalDto local;
    public List<ItemDto> itens;
    public List<LogDto> logs;

    public ProcessoDetalhadoDto(Processo proc, List<ProcessoLog> logs) {
        this.id = proc.getId();
        this.numeroInterno = proc.getNumeroInterno();
        this.numeroProcesso = proc.getNumeroProcesso();
        this.dataInicio = proc.getDataInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        this.status = proc.getStatus().toString();
        this.obs = proc.getObs();

        this.paciente = new PessoaDto(proc.getPaciente());
        this.advogado = proc.getAdvogado() != null ? new PessoaDto(proc.getAdvogado()) : null;
        this.medico = proc.getMedico()   != null ? new PessoaDto(proc.getMedico())   : null;

        // Protege local nulo
        if (proc.getLocal() != null) {
            this.local = new LocalDto(proc.getLocal());
        } else {
            this.local = null;
        }

        this.itens = proc.getItens()
                         .stream()
                         .map(ItemDto::new)
                         .collect(Collectors.toList());
        this.logs  = logs.stream().map(LogDto::new).collect(Collectors.toList());
    }

    public static class PessoaDto {
        public String nome;
        public String cpf;
        public String crm;
        public String oab;

        public PessoaDto(Object pessoa) {
            if (pessoa instanceof com.sgp.model.Paciente p) {
                nome = p.getNome();
                cpf = p.getCpf();
            } else if (pessoa instanceof com.sgp.model.Medico m) {
                nome = m.getNome();
                crm = m.getCrm();
            } else if (pessoa instanceof com.sgp.model.Advogado a) {
                nome = a.getNome();
                oab = a.getOab();
            }
        }
    }

    public static class LocalDto {
        public String comarca;
        public String localizacao;

        public LocalDto(com.sgp.model.Local local) {
            this.comarca      = local.getComarca();
            this.localizacao = local.getLocalizacao();
        }
    }

    public static class ItemDto {
        public String produto;
        public Integer quantidade;
        public String dataEnvio;

        public ItemDto(ProcessoProduto item) {
            this.produto   = item.getProduto()   != null
                            ? item.getProduto().getNomeItem()
                            : "Produto não encontrado";
            this.quantidade = item.getQuantidade();
            this.dataEnvio  = item.getDataEnvio()
                               .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    public static class LogDto {
        public String campo;
        public String valorAntigo;
        public String valorNovo;
        public String data;

        public LogDto(ProcessoLog log) {
            this.campo       = log.getCampo();
            this.valorAntigo = log.getValorAntigo();
            this.valorNovo   = log.getValorNovo();
            this.data        = log.getDataAlteracao()
                                   .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
    }
}

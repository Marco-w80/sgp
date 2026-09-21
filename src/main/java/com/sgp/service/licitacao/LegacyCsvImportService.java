package com.sgp.service.licitacao;

import com.sgp.model.Usuario;
import com.sgp.model.licitacao.CotacaoLicitacao;
import com.sgp.model.licitacao.DecisaoItem;
import com.sgp.model.licitacao.EtapaLicitacao;
import com.sgp.model.licitacao.FornecedorItem;
import com.sgp.model.licitacao.FornecedorLicitacao;
import com.sgp.model.licitacao.Licitacao;
import com.sgp.model.licitacao.LicitacaoHistorico;
import com.sgp.model.licitacao.LicitacaoItem;
import com.sgp.model.licitacao.ResultadoItem;
import com.sgp.repository.UsuarioRepository;
import com.sgp.repository.licitacao.CotacaoLicitacaoRepository;
import com.sgp.repository.licitacao.FornecedorItemRepository;
import com.sgp.repository.licitacao.FornecedorLicitacaoRepository;
import com.sgp.repository.licitacao.LicitacaoHistoricoRepository;
import com.sgp.repository.licitacao.LicitacaoItemRepository;
import com.sgp.repository.licitacao.LicitacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LegacyCsvImportService {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA_BANCO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public record Arquivos(byte[] licitacoes, byte[] itens, byte[] cotacoes, byte[] historicos,
                           byte[] fornecedores, byte[] fornecedorItens) {
        public static Arquivos doDiretorio(Path diretorio) throws IOException {
            Path raiz = diretorio == null ? null : diretorio.toAbsolutePath().normalize();
            if (raiz == null || !Files.isDirectory(raiz)) {
                throw new IllegalArgumentException("Diretório dos CSVs legados não encontrado: " + diretorio);
            }
            return new Arquivos(
                    ler(raiz, "licitacoes.csv"),
                    ler(raiz, "itens.csv"),
                    ler(raiz, "item_cotacoes.csv"),
                    ler(raiz, "licitacao_stage_history.csv"),
                    ler(raiz, "fornecedores.csv"),
                    ler(raiz, "fornecedor_itens.csv"));
        }

        private static byte[] ler(Path diretorio, String nome) throws IOException {
            Path arquivo = diretorio.resolve(nome).normalize();
            if (!arquivo.getParent().equals(diretorio) || !Files.isRegularFile(arquivo)) {
                throw new IllegalArgumentException("Arquivo legado não encontrado: " + nome);
            }
            return Files.readAllBytes(arquivo);
        }
    }

    public record Contagem(int importados, int jaImportados, int reconciliados) {
        public int totalProcessado() { return importados + jaImportados + reconciliados; }
    }

    public record Relatorio(Contagem licitacoes, Contagem itens, Contagem cotacoes, Contagem historicos,
                            Contagem fornecedores, Contagem fornecedorItens, List<String> avisos) {
        public int totalImportado() {
            return licitacoes.importados + itens.importados + cotacoes.importados + historicos.importados
                    + fornecedores.importados + fornecedorItens.importados;
        }
        public int totalReconciliado() {
            return licitacoes.reconciliados + itens.reconciliados + cotacoes.reconciliados
                    + historicos.reconciliados + fornecedores.reconciliados + fornecedorItens.reconciliados;
        }
    }

    private final LicitacaoRepository licitacoes;
    private final LicitacaoItemRepository itens;
    private final CotacaoLicitacaoRepository cotacoes;
    private final LicitacaoHistoricoRepository historicos;
    private final FornecedorLicitacaoRepository fornecedores;
    private final FornecedorItemRepository fornecedorItens;
    private final UsuarioRepository usuarios;

    public LegacyCsvImportService(LicitacaoRepository licitacoes,
                                  LicitacaoItemRepository itens,
                                  CotacaoLicitacaoRepository cotacoes,
                                  LicitacaoHistoricoRepository historicos,
                                  FornecedorLicitacaoRepository fornecedores,
                                  FornecedorItemRepository fornecedorItens,
                                  UsuarioRepository usuarios) {
        this.licitacoes = licitacoes;
        this.itens = itens;
        this.cotacoes = cotacoes;
        this.historicos = historicos;
        this.fornecedores = fornecedores;
        this.fornecedorItens = fornecedorItens;
        this.usuarios = usuarios;
    }

    @Transactional
    public Relatorio importar(Arquivos arquivos) {
        Dados dados = lerEValidar(arquivos);
        List<String> avisos = new ArrayList<>();
        Map<Long, Optional<Usuario>> cacheUsuarios = new HashMap<>();
        Set<Long> usuariosAusentes = new LinkedHashSet<>();

        Acumulador contFornecedores = new Acumulador();
        Acumulador contFornecedorItens = new Acumulador();
        Acumulador contLicitacoes = new Acumulador();
        Acumulador contItens = new Acumulador();
        Acumulador contCotacoes = new Acumulador();
        Acumulador contHistoricos = new Acumulador();

        List<FornecedorLicitacao> fornecedoresPreexistentes = fornecedores.findAll();
        Set<Long> fornecedoresReconciliados = new HashSet<>();
        Map<Long, FornecedorLicitacao> fornecedorPorLegado = new LinkedHashMap<>();
        for (Linha linha : dados.fornecedores.linhas) {
            long legadoId = linha.id();
            Optional<FornecedorLicitacao> jaImportado = fornecedores.findByLegadoId(legadoId);
            if (jaImportado.isPresent()) {
                fornecedorPorLegado.put(legadoId, jaImportado.get());
                contFornecedores.jaImportados++;
                continue;
            }
            List<FornecedorLicitacao> equivalentes = fornecedoresPreexistentes.stream()
                    .filter(f -> normalizar(f.getNome()).equals(normalizar(linha.obrigatorio("nome"))))
                    .filter(f -> !fornecedoresReconciliados.contains(f.getId())).toList();
            if (equivalentes.size() == 1) {
                FornecedorLicitacao existente = equivalentes.get(0);
                existente.setLegadoId(legadoId);
                fornecedorPorLegado.put(legadoId, fornecedores.save(existente));
                fornecedoresReconciliados.add(existente.getId());
                contFornecedores.reconciliados++;
                continue;
            }
            FornecedorLicitacao fornecedor = new FornecedorLicitacao();
            fornecedor.setLegadoId(legadoId);
            fornecedor.setNome(linha.obrigatorio("nome").trim());
            fornecedor.setContato(linha.opcional("contato"));
            fornecedor.setObservacoes(linha.opcional("observacoes"));
            fornecedor.setResumo(linha.opcional("resumo"));
            fornecedor.setCriadoPor(usuario(linha.opcional("created_by"), cacheUsuarios, usuariosAusentes));
            fornecedor.setCriadoEm(dataHora(linha.obrigatorio("created_at"), linha.contexto()));
            fornecedor.setAtualizadoEm(dataHoraOu(linha.opcional("updated_at"), fornecedor.getCriadoEm(), linha.contexto()));
            fornecedorPorLegado.put(legadoId, fornecedores.save(fornecedor));
            contFornecedores.importados++;
        }

        Map<Long, List<FornecedorItem>> fornecedorItensPreexistentes = new HashMap<>();
        fornecedorPorLegado.values().stream().map(FornecedorLicitacao::getId).distinct()
                .forEach(id -> fornecedorItensPreexistentes.put(id, fornecedorItens.findByFornecedorId(id)));
        Set<Long> fornecedorItensReconciliados = new HashSet<>();
        for (Linha linha : dados.fornecedorItens.linhas) {
            long legadoId = linha.id();
            Optional<FornecedorItem> jaImportado = fornecedorItens.findByLegadoId(legadoId);
            if (jaImportado.isPresent()) {
                contFornecedorItens.jaImportados++;
                continue;
            }
            FornecedorLicitacao fornecedor = exigir(fornecedorPorLegado, linha.longObrigatorio("fornecedor_id"), linha, "fornecedor");
            String chave = chaveFornecedorItem(linha.obrigatorio("nome"), linha.opcional("marca"));
            List<FornecedorItem> equivalentes = fornecedorItensPreexistentes.getOrDefault(fornecedor.getId(), List.of()).stream()
                    .filter(i -> chaveFornecedorItem(i.getNome(), i.getMarca()).equals(chave))
                    .filter(i -> !fornecedorItensReconciliados.contains(i.getId())).toList();
            if (equivalentes.size() == 1) {
                FornecedorItem existente = equivalentes.get(0);
                existente.setLegadoId(legadoId);
                fornecedorItens.save(existente);
                fornecedorItensReconciliados.add(existente.getId());
                contFornecedorItens.reconciliados++;
                continue;
            }
            FornecedorItem item = new FornecedorItem();
            item.setLegadoId(legadoId);
            item.setFornecedor(fornecedor);
            item.setNome(linha.obrigatorio("nome").trim());
            item.setMarca(linha.opcional("marca"));
            item.setPreco(decimal(linha.opcional("preco"), linha.contexto()));
            item.setCriadoEm(dataHora(linha.obrigatorio("created_at"), linha.contexto()));
            item.setAtualizadoEm(dataHoraOu(linha.opcional("updated_at"), item.getCriadoEm(), linha.contexto()));
            fornecedorItens.save(item);
            contFornecedorItens.importados++;
        }

        List<Licitacao> licitacoesPreexistentes = licitacoes.findAll();
        Set<Long> licitacoesReconciliadas = new HashSet<>();
        Map<Long, Licitacao> licitacaoPorLegado = new LinkedHashMap<>();
        for (Linha linha : dados.licitacoes.linhas) {
            long legadoId = linha.id();
            Optional<Licitacao> jaImportada = licitacoes.findByLegadoId(legadoId);
            if (jaImportada.isPresent()) {
                licitacaoPorLegado.put(legadoId, jaImportada.get());
                contLicitacoes.jaImportados++;
                continue;
            }
            String chave = chaveLicitacao(linha);
            List<Licitacao> equivalentes = licitacoesPreexistentes.stream()
                    .filter(l -> chaveLicitacao(l).equals(chave))
                    .filter(l -> !licitacoesReconciliadas.contains(l.getId())).toList();
            if (equivalentes.size() == 1) {
                Licitacao existente = equivalentes.get(0);
                existente.setLegadoId(legadoId);
                licitacaoPorLegado.put(legadoId, licitacoes.save(existente));
                licitacoesReconciliadas.add(existente.getId());
                contLicitacoes.reconciliados++;
                continue;
            }
            Licitacao licitacao = new Licitacao();
            licitacao.setLegadoId(legadoId);
            licitacao.setOrgao(linha.obrigatorio("orgao").trim());
            licitacao.setNumeroEdital(linha.obrigatorio("numero_edital").trim());
            licitacao.setUasg(linha.opcional("uasg"));
            licitacao.setModalidade(linha.opcional("modalidade"));
            licitacao.setObjeto(linha.obrigatorio("objeto").trim());
            licitacao.setDataDisputa(dataHora(linha.obrigatorio("data_disputa"), linha.contexto()));
            licitacao.setPrazoEntrega(linha.opcional("prazo_entrega"));
            licitacao.setValorEstimadoTotal(decimal(linha.opcional("valor_estimado_total"), linha.contexto()));
            licitacao.setPrecisaAmostra(booleano(linha.opcional("precisa_amostra")));
            licitacao.setLinkEdital(linha.opcional("link_edital"));
            licitacao.setObservacoes(linha.opcional("observacoes"));
            licitacao.setEtapaAtual(etapa(linha.obrigatorio("current_stage"), linha.contexto()));
            licitacao.setArquivada(booleano(linha.opcional("arquivada")));
            licitacao.setCriadoPor(usuario(linha.opcional("created_by"), cacheUsuarios, usuariosAusentes));
            licitacao.setCriadoEm(dataHora(linha.obrigatorio("created_at"), linha.contexto()));
            licitacao.setAtualizadoEm(dataHoraOu(linha.opcional("updated_at"), licitacao.getCriadoEm(), linha.contexto()));
            licitacaoPorLegado.put(legadoId, licitacoes.save(licitacao));
            contLicitacoes.importados++;
        }

        Map<Long, List<LicitacaoItem>> itensPreexistentes = new HashMap<>();
        licitacaoPorLegado.values().stream().map(Licitacao::getId).distinct()
                .forEach(id -> itensPreexistentes.put(id, itens.findByLicitacaoIdOrderByNumeroItem(id)));
        Set<Long> itensReconciliados = new HashSet<>();
        Map<Long, LicitacaoItem> itemPorLegado = new LinkedHashMap<>();
        for (Linha linha : dados.itens.linhas) {
            long legadoId = linha.id();
            Optional<LicitacaoItem> jaImportado = itens.findByLegadoId(legadoId);
            if (jaImportado.isPresent()) {
                itemPorLegado.put(legadoId, jaImportado.get());
                contItens.jaImportados++;
                continue;
            }
            Licitacao licitacao = exigir(licitacaoPorLegado, linha.longObrigatorio("licitacao_id"), linha, "licitação");
            int numero = linha.intObrigatorio("numero_item");
            List<LicitacaoItem> equivalentes = itensPreexistentes.getOrDefault(licitacao.getId(), List.of()).stream()
                    .filter(i -> Objects.equals(i.getNumeroItem(), numero))
                    .filter(i -> !itensReconciliados.contains(i.getId())).toList();
            if (equivalentes.size() == 1) {
                LicitacaoItem existente = equivalentes.get(0);
                existente.setLegadoId(legadoId);
                itemPorLegado.put(legadoId, itens.save(existente));
                itensReconciliados.add(existente.getId());
                contItens.reconciliados++;
                continue;
            }
            LicitacaoItem item = new LicitacaoItem();
            item.setLegadoId(legadoId);
            item.setLicitacao(licitacao);
            item.setNumeroItem(numero);
            item.setDescricao(linha.obrigatorio("descricao").trim());
            item.setQuantidade(decimalObrigatorio(linha.obrigatorio("quantidade"), linha.contexto()));
            item.setUnidade(Optional.ofNullable(linha.opcional("unidade")).filter(s -> !s.isBlank()).orElse("UN"));
            item.setValorReferencia(decimal(linha.opcional("valor_referencia_edital"), linha.contexto()));
            item.setSelecionadoCotacao(true);
            item.setDecisao(decisao(linha.opcional("aprovado")));
            item.setPrecoMaximo(decimal(linha.opcional("preco_maximo"), linha.contexto()));
            item.setPercentualDesconto(decimal(linha.opcional("percentual_desconto"), linha.contexto()));
            item.setEstrategiaLance(linha.opcional("estrategia_lance"));
            item.setResultado(resultado(linha.opcional("resultado"), linha.contexto()));
            item.setPrecoFinal(decimal(linha.opcional("preco_final"), linha.contexto()));
            item.setMotivoPerda(linha.opcional("motivo_perda"));
            item.setCriadoEm(dataHora(linha.obrigatorio("created_at"), linha.contexto()));
            item.setAtualizadoEm(dataHoraOu(linha.opcional("updated_at"), item.getCriadoEm(), linha.contexto()));
            itemPorLegado.put(legadoId, itens.save(item));
            contItens.importados++;
        }

        Map<String, List<FornecedorLicitacao>> fornecedoresPorNome = fornecedores.findAll().stream()
                .collect(Collectors.groupingBy(f -> normalizar(f.getNome())));
        Map<Long, List<CotacaoLicitacao>> cotacoesPreexistentes = new HashMap<>();
        itemPorLegado.values().stream().map(LicitacaoItem::getId).distinct()
                .forEach(id -> cotacoesPreexistentes.put(id, cotacoes.findByItemId(id)));
        Set<Long> cotacoesReconciliadas = new HashSet<>();
        int datasCotacaoRecuperadas = 0;
        Set<String> fornecedoresCotacaoNaoVinculados = new LinkedHashSet<>();
        for (Linha linha : dados.cotacoes.linhas) {
            long legadoId = linha.id();
            if (cotacoes.findByLegadoId(legadoId).isPresent()) {
                contCotacoes.jaImportados++;
                continue;
            }
            LicitacaoItem item = exigir(itemPorLegado, linha.longObrigatorio("item_id"), linha, "item");
            LocalDateTime criadoEm = dataHora(linha.obrigatorio("created_at"), linha.contexto());
            LocalDate dataCotacao = data(linha.opcional("data_cotacao"), linha.contexto());
            if (dataCotacao == null) {
                dataCotacao = criadoEm.toLocalDate();
                datasCotacaoRecuperadas++;
            }
            String nomeFornecedor = linha.obrigatorio("fornecedor").trim();
            BigDecimal valor = decimalObrigatorio(linha.obrigatorio("valor_unitario"), linha.contexto());
            LocalDate dataFinal = dataCotacao;
            List<CotacaoLicitacao> equivalentes = cotacoesPreexistentes.getOrDefault(item.getId(), List.of()).stream()
                    .filter(c -> normalizar(c.getFornecedorNome()).equals(normalizar(nomeFornecedor)))
                    .filter(c -> c.getValorUnitario().compareTo(valor) == 0)
                    .filter(c -> Objects.equals(c.getDataCotacao(), dataFinal))
                    .filter(c -> !cotacoesReconciliadas.contains(c.getId())).toList();
            if (equivalentes.size() == 1) {
                CotacaoLicitacao existente = equivalentes.get(0);
                existente.setLegadoId(legadoId);
                cotacoes.save(existente);
                cotacoesReconciliadas.add(existente.getId());
                contCotacoes.reconciliados++;
                continue;
            }
            CotacaoLicitacao cotacao = new CotacaoLicitacao();
            cotacao.setLegadoId(legadoId);
            cotacao.setItem(item);
            cotacao.setFornecedorNome(nomeFornecedor);
            List<FornecedorLicitacao> candidatos = fornecedoresPorNome.getOrDefault(normalizar(nomeFornecedor), List.of());
            if (candidatos.size() == 1) cotacao.setFornecedor(candidatos.get(0));
            else fornecedoresCotacaoNaoVinculados.add(nomeFornecedor);
            cotacao.setValorUnitario(valor);
            cotacao.setDataCotacao(dataCotacao);
            cotacao.setObservacoes(linha.opcional("observacoes"));
            cotacao.setRegistradoPor(usuario(linha.opcional("created_by"), cacheUsuarios, usuariosAusentes));
            cotacao.setCriadoEm(criadoEm);
            cotacoes.save(cotacao);
            contCotacoes.importados++;
        }

        Map<Long, List<LicitacaoHistorico>> historicosPreexistentes = new HashMap<>();
        licitacaoPorLegado.values().stream().map(Licitacao::getId).distinct()
                .forEach(id -> historicosPreexistentes.put(id, historicos.findByLicitacaoId(id)));
        Set<Long> historicosReconciliados = new HashSet<>();
        for (Linha linha : dados.historicos.linhas) {
            long legadoId = linha.id();
            if (historicos.findByLegadoId(legadoId).isPresent()) {
                contHistoricos.jaImportados++;
                continue;
            }
            Licitacao licitacao = exigir(licitacaoPorLegado, linha.longObrigatorio("licitacao_id"), linha, "licitação");
            EtapaLicitacao origem = etapaOpcional(linha.opcional("from_stage"), linha.contexto());
            EtapaLicitacao destino = etapa(linha.obrigatorio("to_stage"), linha.contexto());
            LocalDateTime dataHora = dataHora(linha.obrigatorio("changed_at"), linha.contexto());
            List<LicitacaoHistorico> equivalentes = historicosPreexistentes.getOrDefault(licitacao.getId(), List.of()).stream()
                    .filter(h -> h.getEtapaOrigem() == origem && h.getEtapaDestino() == destino)
                    .filter(h -> Objects.equals(h.getDataHora(), dataHora))
                    .filter(h -> !historicosReconciliados.contains(h.getId())).toList();
            if (equivalentes.size() == 1) {
                LicitacaoHistorico existente = equivalentes.get(0);
                existente.setLegadoId(legadoId);
                historicos.save(existente);
                historicosReconciliados.add(existente.getId());
                contHistoricos.reconciliados++;
                continue;
            }
            LicitacaoHistorico historico = new LicitacaoHistorico();
            historico.setLegadoId(legadoId);
            historico.setLicitacao(licitacao);
            historico.setEtapaOrigem(origem);
            historico.setEtapaDestino(destino);
            historico.setUsuario(usuario(linha.opcional("changed_by"), cacheUsuarios, usuariosAusentes));
            historico.setDataHora(dataHora);
            historico.setObservacoes(linha.opcional("observacoes"));
            historico.setSnapshot(linha.opcional("snapshot_json"));
            historicos.save(historico);
            contHistoricos.importados++;
        }

        if (datasCotacaoRecuperadas > 0) {
            avisos.add(datasCotacaoRecuperadas + " cotações sem data usaram a data de criação do registro legado.");
        }
        if (!fornecedoresCotacaoNaoVinculados.isEmpty()) {
            avisos.add("Cotações mantidas somente com o nome do fornecedor (sem vínculo ao portfólio): "
                    + String.join(", ", fornecedoresCotacaoNaoVinculados) + ".");
        }
        if (!usuariosAusentes.isEmpty()) {
            avisos.add("Usuários antigos não encontrados no banco atual; a autoria foi mantida como Sistema: "
                    + usuariosAusentes.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ".");
        }
        long chavesDuplicadas = dados.licitacoes.linhas.stream().collect(Collectors.groupingBy(this::chaveLicitacao, Collectors.counting()))
                .values().stream().filter(total -> total > 1).count();
        if (chavesDuplicadas > 0) {
            avisos.add(chavesDuplicadas + " licitação duplicada no arquivo de origem foi preservada separadamente.");
        }

        return new Relatorio(contLicitacoes.resultado(), contItens.resultado(), contCotacoes.resultado(),
                contHistoricos.resultado(), contFornecedores.resultado(), contFornecedorItens.resultado(),
                List.copyOf(avisos));
    }

    private Dados lerEValidar(Arquivos arquivos) {
        if (arquivos == null) throw new IllegalArgumentException("Informe os seis arquivos CSV do legado.");
        TabelaCsv licitacoes = TabelaCsv.ler("licitacoes.csv", arquivos.licitacoes,
                "id", "orgao", "numero_edital", "uasg", "modalidade", "objeto", "data_disputa",
                "prazo_entrega", "valor_estimado_total", "precisa_amostra", "link_edital", "observacoes",
                "current_stage", "arquivada", "created_by", "created_at", "updated_at");
        TabelaCsv itens = TabelaCsv.ler("itens.csv", arquivos.itens,
                "id", "licitacao_id", "numero_item", "descricao", "quantidade", "unidade",
                "valor_referencia_edital", "preco_maximo", "percentual_desconto", "estrategia_lance",
                "resultado", "preco_final", "motivo_perda", "created_at", "updated_at", "aprovado");
        TabelaCsv cotacoes = TabelaCsv.ler("item_cotacoes.csv", arquivos.cotacoes,
                "id", "item_id", "fornecedor", "valor_unitario", "data_cotacao", "observacoes", "created_by", "created_at");
        TabelaCsv historicos = TabelaCsv.ler("licitacao_stage_history.csv", arquivos.historicos,
                "id", "licitacao_id", "from_stage", "to_stage", "changed_by", "changed_at", "observacoes", "snapshot_json");
        TabelaCsv fornecedores = TabelaCsv.ler("fornecedores.csv", arquivos.fornecedores,
                "id", "nome", "contato", "observacoes", "created_by", "created_at", "updated_at", "resumo");
        TabelaCsv fornecedorItens = TabelaCsv.ler("fornecedor_itens.csv", arquivos.fornecedorItens,
                "id", "fornecedor_id", "nome", "marca", "preco", "created_at", "updated_at");

        validarIdsUnicos(licitacoes);
        validarIdsUnicos(itens);
        validarIdsUnicos(cotacoes);
        validarIdsUnicos(historicos);
        validarIdsUnicos(fornecedores);
        validarIdsUnicos(fornecedorItens);
        validarReferencias(itens, "licitacao_id", licitacoes, "licitação");
        validarReferencias(cotacoes, "item_id", itens, "item");
        validarReferencias(historicos, "licitacao_id", licitacoes, "licitação");
        validarReferencias(fornecedorItens, "fornecedor_id", fornecedores, "fornecedor");
        return new Dados(licitacoes, itens, cotacoes, historicos, fornecedores, fornecedorItens);
    }

    private void validarIdsUnicos(TabelaCsv tabela) {
        Set<Long> vistos = new HashSet<>();
        for (Linha linha : tabela.linhas) {
            long id = linha.id();
            if (!vistos.add(id)) throw new IllegalArgumentException(linha.contexto() + ": id duplicado " + id + ".");
        }
    }

    private void validarReferencias(TabelaCsv origem, String coluna, TabelaCsv destino, String descricao) {
        Set<Long> ids = destino.linhas.stream().map(Linha::id).collect(Collectors.toSet());
        for (Linha linha : origem.linhas) {
            long referencia = linha.longObrigatorio(coluna);
            if (!ids.contains(referencia)) {
                throw new IllegalArgumentException(linha.contexto() + ": " + descricao + " legado " + referencia + " não encontrado.");
            }
        }
    }

    private Usuario usuario(String valor, Map<Long, Optional<Usuario>> cache, Set<Long> ausentes) {
        if (valor == null || valor.isBlank()) return null;
        long id;
        try { id = Long.parseLong(valor.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Identificador de usuário inválido: " + valor); }
        Optional<Usuario> usuario = cache.computeIfAbsent(id, usuarios::findById);
        if (usuario.isEmpty()) ausentes.add(id);
        return usuario.orElse(null);
    }

    private String chaveLicitacao(Linha linha) {
        String link = linha.opcional("link_edital");
        String base = normalizar(linha.obrigatorio("orgao")) + "|" + normalizar(linha.obrigatorio("numero_edital"))
                + "|" + linha.obrigatorio("data_disputa").trim();
        return link == null || link.isBlank() ? base : normalizar(link) + "|" + base;
    }

    private String chaveLicitacao(Licitacao licitacao) {
        String base = normalizar(licitacao.getOrgao()) + "|" + normalizar(licitacao.getNumeroEdital())
                + "|" + licitacao.getDataDisputa();
        return licitacao.getLinkEdital() == null || licitacao.getLinkEdital().isBlank()
                ? base : normalizar(licitacao.getLinkEdital()) + "|" + base;
    }

    private String chaveFornecedorItem(String nome, String marca) {
        return normalizar(nome) + "|" + normalizar(marca);
    }

    private String normalizar(String valor) {
        if (valor == null) return "";
        String semAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return semAcentos.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private EtapaLicitacao etapa(String valor, String contexto) {
        EtapaLicitacao etapa = etapaOpcional(valor, contexto);
        if (etapa == null) throw new IllegalArgumentException(contexto + ": etapa obrigatória não informada.");
        return etapa;
    }

    private EtapaLicitacao etapaOpcional(String valor, String contexto) {
        if (valor == null || valor.isBlank()) return null;
        return switch (normalizar(valor)) {
            case "CADASTRO", "CAPTACAO" -> EtapaLicitacao.CADASTRO;
            case "COTACAO", "ANALISE_COTACAO" -> EtapaLicitacao.COTACAO;
            case "APROVACAO", "PRECO_MAXIMO" -> EtapaLicitacao.APROVACAO;
            case "DEFINICAO" -> EtapaLicitacao.DEFINICAO;
            case "PARTICIPACAO" -> EtapaLicitacao.PARTICIPACAO;
            default -> throw new IllegalArgumentException(contexto + ": etapa legada desconhecida: " + valor);
        };
    }

    private DecisaoItem decisao(String valor) {
        if (valor == null || valor.isBlank()) return DecisaoItem.PENDENTE;
        return booleano(valor) ? DecisaoItem.APROVADO : DecisaoItem.REPROVADO;
    }

    private ResultadoItem resultado(String valor, String contexto) {
        if (valor == null || valor.isBlank()) return null;
        return switch (normalizar(valor)) {
            case "GANHO", "GANHOU", "VENCEDOR" -> ResultadoItem.GANHO;
            case "PERDIDO", "PERDEU" -> ResultadoItem.PERDIDO;
            case "CANCELADO", "CANCELADA" -> ResultadoItem.CANCELADO;
            default -> throw new IllegalArgumentException(contexto + ": resultado legado desconhecido: " + valor);
        };
    }

    private boolean booleano(String valor) {
        if (valor == null || valor.isBlank()) return false;
        return Set.of("1", "TRUE", "SIM", "S", "YES").contains(normalizar(valor));
    }

    private BigDecimal decimal(String valor, String contexto) {
        if (valor == null || valor.isBlank()) return null;
        try { return new BigDecimal(valor.trim().replace(',', '.')); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(contexto + ": número inválido: " + valor); }
    }

    private BigDecimal decimalObrigatorio(String valor, String contexto) {
        BigDecimal numero = decimal(valor, contexto);
        if (numero == null) throw new IllegalArgumentException(contexto + ": número obrigatório não informado.");
        return numero;
    }

    private LocalDate data(String valor, String contexto) {
        if (valor == null || valor.isBlank()) return null;
        try { return LocalDate.parse(valor.trim()); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException(contexto + ": data inválida: " + valor); }
    }

    private LocalDateTime dataHoraOu(String valor, LocalDateTime padrao, String contexto) {
        return valor == null || valor.isBlank() ? padrao : dataHora(valor, contexto);
    }

    private LocalDateTime dataHora(String valor, String contexto) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(contexto + ": data e hora obrigatórias não informadas.");
        String texto = valor.trim();
        try { return OffsetDateTime.parse(texto).atZoneSameInstant(ZONA).toLocalDateTime(); }
        catch (DateTimeParseException ignored) { }
        try { return Instant.parse(texto).atZone(ZONA).toLocalDateTime(); }
        catch (DateTimeParseException ignored) { }
        try { return LocalDateTime.parse(texto); }
        catch (DateTimeParseException ignored) { }
        try { return LocalDateTime.parse(texto, DATA_HORA_BANCO); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException(contexto + ": data e hora inválidas: " + valor); }
    }

    private <T> T exigir(Map<Long, T> mapa, long legadoId, Linha linha, String descricao) {
        T valor = mapa.get(legadoId);
        if (valor == null) throw new IllegalArgumentException(linha.contexto() + ": " + descricao + " legado " + legadoId + " não foi importado.");
        return valor;
    }

    private record Dados(TabelaCsv licitacoes, TabelaCsv itens, TabelaCsv cotacoes, TabelaCsv historicos,
                         TabelaCsv fornecedores, TabelaCsv fornecedorItens) { }

    private static final class Acumulador {
        int importados;
        int jaImportados;
        int reconciliados;
        Contagem resultado() { return new Contagem(importados, jaImportados, reconciliados); }
    }

    private record Linha(String arquivo, int numero, Map<String, String> valores) {
        long id() { return longObrigatorio("id"); }
        String contexto() { return arquivo + ", registro " + numero; }
        String opcional(String coluna) {
            String valor = valores.get(coluna);
            return valor == null || valor.isBlank() ? null : valor;
        }
        String obrigatorio(String coluna) {
            String valor = opcional(coluna);
            if (valor == null) throw new IllegalArgumentException(contexto() + ": coluna obrigatória vazia: " + coluna + ".");
            return valor;
        }
        long longObrigatorio(String coluna) {
            String valor = obrigatorio(coluna);
            try { return Long.parseLong(valor.trim()); }
            catch (NumberFormatException e) { throw new IllegalArgumentException(contexto() + ": inteiro inválido em " + coluna + ": " + valor); }
        }
        int intObrigatorio(String coluna) {
            String valor = obrigatorio(coluna);
            try { return new BigDecimal(valor.trim()).intValueExact(); }
            catch (ArithmeticException | NumberFormatException e) { throw new IllegalArgumentException(contexto() + ": inteiro inválido em " + coluna + ": " + valor); }
        }
    }

    private record TabelaCsv(String nome, List<Linha> linhas) {
        static TabelaCsv ler(String nome, byte[] bytes, String... colunasObrigatorias) {
            if (bytes == null || bytes.length == 0) throw new IllegalArgumentException("Arquivo vazio: " + nome + ".");
            String texto = new String(bytes, StandardCharsets.UTF_8);
            if (!texto.isEmpty() && texto.charAt(0) == '\uFEFF') texto = texto.substring(1);
            List<List<String>> registros = separar(texto, nome);
            if (registros.isEmpty()) throw new IllegalArgumentException("Arquivo sem cabeçalho: " + nome + ".");
            List<String> cabecalho = registros.get(0).stream().map(String::trim).toList();
            Set<String> repetidas = cabecalho.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
            if (!repetidas.isEmpty()) throw new IllegalArgumentException(nome + ": colunas repetidas: " + repetidas + ".");
            for (String coluna : colunasObrigatorias) {
                if (!cabecalho.contains(coluna)) throw new IllegalArgumentException(nome + ": coluna ausente: " + coluna + ".");
            }
            List<Linha> linhas = new ArrayList<>();
            for (int i = 1; i < registros.size(); i++) {
                List<String> registro = registros.get(i);
                if (registro.stream().allMatch(String::isBlank)) continue;
                if (registro.size() != cabecalho.size()) {
                    throw new IllegalArgumentException(nome + ", registro " + (i + 1) + ": esperado "
                            + cabecalho.size() + " colunas, encontrado " + registro.size() + ".");
                }
                Map<String, String> valores = new LinkedHashMap<>();
                for (int coluna = 0; coluna < cabecalho.size(); coluna++) valores.put(cabecalho.get(coluna), registro.get(coluna));
                linhas.add(new Linha(nome, i + 1, valores));
            }
            return new TabelaCsv(nome, List.copyOf(linhas));
        }

        private static List<List<String>> separar(String texto, String nome) {
            List<List<String>> registros = new ArrayList<>();
            List<String> registro = new ArrayList<>();
            StringBuilder campo = new StringBuilder();
            boolean entreAspas = false;
            for (int i = 0; i < texto.length(); i++) {
                char caractere = texto.charAt(i);
                if (caractere == '"') {
                    if (entreAspas && i + 1 < texto.length() && texto.charAt(i + 1) == '"') {
                        campo.append('"');
                        i++;
                    } else {
                        entreAspas = !entreAspas;
                    }
                } else if (caractere == ';' && !entreAspas) {
                    registro.add(campo.toString());
                    campo.setLength(0);
                } else if ((caractere == '\n' || caractere == '\r') && !entreAspas) {
                    if (caractere == '\r' && i + 1 < texto.length() && texto.charAt(i + 1) == '\n') i++;
                    registro.add(campo.toString());
                    campo.setLength(0);
                    registros.add(registro);
                    registro = new ArrayList<>();
                } else {
                    campo.append(caractere);
                }
            }
            if (entreAspas) throw new IllegalArgumentException(nome + ": campo entre aspas não foi encerrado.");
            if (campo.length() > 0 || !registro.isEmpty()) {
                registro.add(campo.toString());
                registros.add(registro);
            }
            return registros;
        }
    }
}

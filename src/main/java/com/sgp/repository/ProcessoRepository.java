package com.sgp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sgp.dto.MonthCountDto;
import com.sgp.dto.StatusCountDto;
import com.sgp.model.Processo;
import com.sgp.model.StatusProcesso;
import com.sgp.projections.Projections;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, Long> {

      boolean existsByNumeroInterno(String numeroInterno);

    @Query(value = """
            select p.id,
                   pac.nome as nome_pessoa,
                   p.ultimo_acesso_em,
                   p.ultimo_acesso_por,
                   p.ultima_edicao_em,
                   p.ultima_edicao_por,
                   timestampdiff(day, coalesce(p.ultimo_acesso_em, cast(p.data_inicio as datetime)), now()) as dias_sem_acesso,
                   timestampdiff(day, coalesce(p.ultima_edicao_em, cast(p.data_inicio as datetime)), now()) as dias_sem_edicao
            from processos p
            join pessoas pac on pac.id = p.paciente_id
            where (:diasSemAcesso is null
                   or timestampdiff(day, coalesce(p.ultimo_acesso_em, cast(p.data_inicio as datetime)), now()) >= :diasSemAcesso)
              and (:diasSemEdicao is null
                   or timestampdiff(day, coalesce(p.ultima_edicao_em, cast(p.data_inicio as datetime)), now()) >= :diasSemEdicao)
            order by p.id desc
            """, nativeQuery = true)
    List<Object[]> buscarAcompanhamento(@Param("diasSemAcesso") Integer diasSemAcesso,
                                        @Param("diasSemEdicao") Integer diasSemEdicao);



    @Query("SELECT new com.sgp.dto.StatusCountDto(p.status, COUNT(p)) "
         + "FROM Processo p GROUP BY p.status")
    List<StatusCountDto> countByStatus();

    
    @Query("SELECT new com.sgp.dto.MonthCountDto(" +
            "YEAR(p.dataInicio), MONTH(p.dataInicio), COUNT(p)) " +
            "FROM Processo p " +
            "GROUP BY YEAR(p.dataInicio), MONTH(p.dataInicio) " +
            "ORDER BY YEAR(p.dataInicio), MONTH(p.dataInicio)")
        List<MonthCountDto> countByMonth();


         @Query("""
      SELECT p FROM Processo p
      WHERE (:de IS NULL OR p.dataInicio >= :de)
        AND (:ate IS NULL OR p.dataInicio <= :ate)
        AND (:status IS NULL OR p.status = :status)
        AND (:localId IS NULL OR p.local.id = :localId)
        AND (:cpfAnexado IS NULL OR p.cpfAnexado = :cpfAnexado)
        AND (:compResidenciaAnexado IS NULL OR p.compResidenciaAnexado = :compResidenciaAnexado)
        AND (:compRendaAnexado IS NULL OR p.compRendaAnexado = :compRendaAnexado)
        AND (:procuracaoAnexado IS NULL OR p.procuracaoAnexado = :procuracaoAnexado)
        AND (:declaracaoInsuficienciaAnexado IS NULL OR p.declaracaoInsuficienciaAnexado = :declaracaoInsuficienciaAnexado)
        AND (:paciente IS NULL OR LOWER(p.paciente.nome) LIKE LOWER(CONCAT('%', :paciente, '%')) OR p.paciente.cpf LIKE CONCAT('%', :paciente, '%'))
      """)
    List<Processo> findByFiltros(
            @Param("de") LocalDate de,
            @Param("ate") LocalDate ate,
            @Param("status") StatusProcesso status,
            @Param("paciente") String paciente,
            @Param("localId") Long localId,
            @Param("cpfAnexado") Boolean cpfAnexado,
            @Param("compResidenciaAnexado") Boolean compResidenciaAnexado,
            @Param("compRendaAnexado") Boolean compRendaAnexado,
            @Param("procuracaoAnexado") Boolean procuracaoAnexado,
            @Param("declaracaoInsuficienciaAnexado") Boolean declaracaoInsuficienciaAnexado
    );

        long countByStatus(StatusProcesso status);


    // Contagem por Status
    @Query("select p.status as status, count(p) as total from Processo p group by p.status")
    List<Projections.StatusCountProjection> contarPorStatus();

    @Query("""
           select p.status as status, count(p) as total
           from Processo p
           where (:de is null or p.dataInicio >= :de)
             and (:ate is null or p.dataInicio <= :ate)
           group by p.status
           """)
    List<Projections.StatusCountProjection> contarPorStatusPeriodo(@Param("de") LocalDate de,
                                                                   @Param("ate") LocalDate ate);

    // Novos processos por mês (YYYY-MM)
    @Query("""
           select function('date_format', p.dataInicio, '%Y-%m') as anoMes,
                  count(p) as valor
           from Processo p
           group by function('date_format', p.dataInicio, '%Y-%m')
           order by function('date_format', p.dataInicio, '%Y-%m')
           """)
    List<Projections.SerieMensalProjection> novosPorMes();

    @Query("""
           select function('date_format', p.dataInicio, '%Y-%m') as anoMes,
                  count(p) as valor
           from Processo p
           where (:de is null or p.dataInicio >= :de)
             and (:ate is null or p.dataInicio <= :ate)
           group by function('date_format', p.dataInicio, '%Y-%m')
           order by function('date_format', p.dataInicio, '%Y-%m')
           """)
    List<Projections.SerieMensalProjection> novosPorMesPeriodo(@Param("de") LocalDate de,
                                                                @Param("ate") LocalDate ate);

    // Distribuição por TipoHospital
    @Query("select cast(p.tipoHospital as string) as nome, count(p) as total from Processo p group by p.tipoHospital")
    List<Projections.ChaveValorLongProjection> distribuicaoPorTipoHospital();

    @Query("""
           select cast(p.tipoHospital as string) as nome, count(p) as total
           from Processo p
           where (:de is null or p.dataInicio >= :de)
             and (:ate is null or p.dataInicio <= :ate)
           group by p.tipoHospital
           """)
    List<Projections.ChaveValorLongProjection> distribuicaoPorTipoHospitalPeriodo(@Param("de") LocalDate de,
                                                                                   @Param("ate") LocalDate ate);

    // Top doenças por volume (sem LIMIT aqui; limite na Service)
    @Query("""
           select d.nome as nome, count(p) as total
           from Processo p
           join p.doenca d
           group by d.nome
           order by count(p) desc
           """)
    List<Projections.ChaveValorLongProjection> topDoencas();

    @Query("""
           select d.nome as nome, count(p) as total
           from Processo p
           join p.doenca d
           where (:de is null or p.dataInicio >= :de)
             and (:ate is null or p.dataInicio <= :ate)
           group by d.nome
           order by count(p) desc
           """)
    List<Projections.ChaveValorLongProjection> topDoencasPeriodo(@Param("de") LocalDate de,
                                                                 @Param("ate") LocalDate ate);

    // Lead time por processo (dias até o 1º envio)
@Query("""
       select p.id as processoId,
              timestampdiff(day, p.dataInicio,
                 (select min(pp.dataEnvio) from ProcessoProduto pp where pp.processo = p)
              ) as leadTimeDias
       from Processo p
       where exists (select 1 from ProcessoProduto ppX where ppX.processo = p)
       """)
List<Projections.LeadTimeProjection> leadTimePorProcesso();

@Query("""
       select p.id as processoId,
              timestampdiff(day, p.dataInicio,
                 (select min(pp.dataEnvio) from ProcessoProduto pp where pp.processo = p)
              ) as leadTimeDias
       from Processo p
       where exists (select 1 from ProcessoProduto ppX where ppX.processo = p)
         and (:de is null or p.dataInicio >= :de)
         and (:ate is null or p.dataInicio <= :ate)
       """)
List<Projections.LeadTimeProjection> leadTimePorProcessoPeriodo(@Param("de") LocalDate de,
                                                                @Param("ate") LocalDate ate);

// Lead time médio por doença
@Query("""
       select d.nome as nome,
              avg(
                 timestampdiff(day, p.dataInicio,
                   (select min(pp.dataEnvio) from ProcessoProduto pp where pp.processo = p)
                 )
              ) as mediaDias,
              count(p) as qtde
       from Processo p
       join p.doenca d
       where exists (select 1 from ProcessoProduto ppX where ppX.processo = p)
       group by d.nome
       order by mediaDias
       """)
List<Projections.LeadTimeMedioProjection> leadTimeMedioPorDoenca();

@Query("""
       select d.nome as nome,
              avg(
                 timestampdiff(day, p.dataInicio,
                   (select min(pp.dataEnvio) from ProcessoProduto pp where pp.processo = p)
                 )
              ) as mediaDias,
              count(p) as qtde
       from Processo p
       join p.doenca d
       where exists (select 1 from ProcessoProduto ppX where ppX.processo = p)
         and (:de is null or p.dataInicio >= :de)
         and (:ate is null or p.dataInicio <= :ate)
       group by d.nome
       order by mediaDias
       """)
List<Projections.LeadTimeMedioProjection> leadTimeMedioPorDoencaPeriodo(@Param("de") LocalDate de,
                                                                        @Param("ate") LocalDate ate);

// Produtividade por advogado (volume & lead time)
@Query("""
       select coalesce(a.nome, '(sem advogado)') as nome,
              avg(
                 timestampdiff(day, p.dataInicio,
                   (select min(pp.dataEnvio) from ProcessoProduto pp where pp.processo = p)
                 )
              ) as mediaDias,
              count(p) as qtde
       from Processo p
       left join p.advogado a
       where exists (select 1 from ProcessoProduto ppX where ppX.processo = p)
       group by a.nome
       order by qtde desc
       """)
List<Projections.LeadTimeMedioProjection> produtividadePorAdvogado();

@Query("""
       select coalesce(a.nome, '(sem advogado)') as nome,
              avg(
                 timestampdiff(day, p.dataInicio,
                   (select min(pp.dataEnvio) from ProcessoProduto pp where pp.processo = p)
                 )
              ) as mediaDias,
              count(p) as qtde
       from Processo p
       left join p.advogado a
       where exists (select 1 from ProcessoProduto ppX where ppX.processo = p)
         and (:de is null or p.dataInicio >= :de)
         and (:ate is null or p.dataInicio <= :ate)
       group by a.nome
       order by qtde desc
       """)
List<Projections.LeadTimeMedioProjection> produtividadePorAdvogadoPeriodo(@Param("de") LocalDate de,
                                                                          @Param("ate") LocalDate ate);


    // Pendências de documentos há N dias+ (nativa OK)
    @Query(value = """
        select p.id,
               p.numero_interno,
               pac.nome as paciente,
               datediff(curdate(), p.data_inicio) as diasDesdeInicio,
               p.cpf_anexado,
               p.comp_residencia_anexado,
               p.comp_renda_anexado,
               p.procuracao_anexado,
               p.declaracao_insuficiencia_anexado
        from processos p
        join paciente pac on pac.id = p.paciente_id
        where not (p.cpf_anexado
               and p.comp_residencia_anexado
               and p.comp_renda_anexado
               and p.procuracao_anexado
               and p.declaracao_insuficiencia_anexado)
          and (:de is null or p.data_inicio >= :de)
          and (:ate is null or p.data_inicio <= :ate)
          and datediff(curdate(), p.data_inicio) >= :dias
        order by diasDesdeInicio desc
        """, nativeQuery = true)
    List<Object[]> pendenciasDocumento(@Param("dias") int dias,
                                       @Param("de") LocalDate de,
                                       @Param("ate") LocalDate ate);

    // Totais para % documentação completa
    @Query("select count(p) from Processo p")
    long totalProcessos();

    @Query("""
           select count(p) from Processo p
           where (:de is null or p.dataInicio >= :de)
             and (:ate is null or p.dataInicio <= :ate)
           """)
    long totalProcessosPeriodo(@Param("de") LocalDate de,
                               @Param("ate") LocalDate ate);

    @Query("""
           select count(p) from Processo p
           where p.cpfAnexado = true
             and p.compResidenciaAnexado = true
             and p.compRendaAnexado = true
             and p.procuracaoAnexado = true
             and p.declaracaoInsuficienciaAnexado = true
           """)
    long totalProcessosCompletos();

    @Query("""
           select count(p) from Processo p
           where p.cpfAnexado = true
             and p.compResidenciaAnexado = true
             and p.compRendaAnexado = true
             and p.procuracaoAnexado = true
             and p.declaracaoInsuficienciaAnexado = true
             and (:de is null or p.dataInicio >= :de)
             and (:ate is null or p.dataInicio <= :ate)
           """)
    long totalProcessosCompletosPeriodo(@Param("de") LocalDate de,
                                        @Param("ate") LocalDate ate);

    // Distribuição por sexo do paciente
// Distribuição por sexo do(a) paciente (JPQL + enum)
@Query("""
       select
         case
           when pac.sexo is null then 'DESCONHECIDO'
           when pac.sexo = com.sgp.model.Sexo.MASCULINO then 'Masculino'
           when pac.sexo = com.sgp.model.Sexo.FEMININO  then 'Feminino'
           else cast(pac.sexo as string)
         end as nome,
         count(proc) as total
       from Processo proc
       join proc.paciente pac
       group by
         case
           when pac.sexo is null then 'DESCONHECIDO'
           when pac.sexo = com.sgp.model.Sexo.MASCULINO then 'Masculino'
           when pac.sexo = com.sgp.model.Sexo.FEMININO  then 'Feminino'
           else cast(pac.sexo as string)
         end
       """)
List<Projections.ChaveValorLongProjection> distribuicaoPorSexoPaciente();

@Query("""
       select
         case
           when pac.sexo is null then 'DESCONHECIDO'
           when pac.sexo = com.sgp.model.Sexo.MASCULINO then 'Masculino'
           when pac.sexo = com.sgp.model.Sexo.FEMININO  then 'Feminino'
           else cast(pac.sexo as string)
         end as nome,
         count(proc) as total
       from Processo proc
       join proc.paciente pac
       where (:de is null or proc.dataInicio >= :de)
         and (:ate is null or proc.dataInicio <= :ate)
       group by
         case
           when pac.sexo is null then 'DESCONHECIDO'
           when pac.sexo = com.sgp.model.Sexo.MASCULINO then 'Masculino'
           when pac.sexo = com.sgp.model.Sexo.FEMININO  then 'Feminino'
           else cast(pac.sexo as string)
         end
       """)
List<Projections.ChaveValorLongProjection> distribuicaoPorSexoPacientePeriodo(@Param("de") LocalDate de,
                                                                               @Param("ate") LocalDate ate);




@Query("""
       select cast(coalesce(pac.sexo, 'NAO_INFORMADO') as string) as sexo,
              avg( timestampdiff(year, pac.dataNascimento, current date) ) as media,
              count(pac.id) as qtde
       from Processo pr
       join pr.paciente pac
       where pac.dataNascimento is not null
       group by pac.sexo
       """)
List<Projections.SexoMediaIdadeProjection> mediaIdadePorSexo();

@Query("""
       select cast(coalesce(pac.sexo, 'NAO_INFORMADO') as string) as sexo,
              avg( timestampdiff(year, pac.dataNascimento, current date) ) as media,
              count(pac.id) as qtde
       from Processo pr
       join pr.paciente pac
       where pac.dataNascimento is not null
         and (:de is null or pr.dataInicio >= :de)
         and (:ate is null or pr.dataInicio <= :ate)
       group by pac.sexo
       """)
List<Projections.SexoMediaIdadeProjection> mediaIdadePorSexoPeriodo(@Param("de") LocalDate de,
                                                                    @Param("ate") LocalDate ate);

    @Query("""
           select distinct year(p.dataInicio)
           from Processo p
           where p.dataInicio is not null
           order by year(p.dataInicio) desc
           """)
    List<Integer> anosComProcessos();




}
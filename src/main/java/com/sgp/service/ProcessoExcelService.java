package com.sgp.service;

import com.sgp.model.Deferimento;
import com.sgp.model.Processo;
import com.sgp.model.ProcessoProduto;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessoExcelService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] gerarRelatorioCompleto(List<Processo> processos, String titulo) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Processos");
            String[] headers = {
                    "ID", "N. Interno", "N. Processo", "Data Inicio", "Status",
                    "Paciente", "CPF Paciente", "Advogado", "Medico", "Doenca",
                    "Local", "Tipo Hospital", "Hospital", "CPF Anexado", "Comp. Residencia",
                    "Comp. Renda", "Procuracao", "Declaracao Insuficiencia", "Produtos",
                    "Deferimentos", "Ultimo Acesso", "Ultimo Acesso Por", "Ultima Edicao",
                    "Ultima Edicao Por", "Observacoes"
            };

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle bodyStyle = createBodyStyle(workbook);

            int rowIndex = 0;
            Row titleRow = sheet.createRow(rowIndex++);
            titleRow.setHeightInPoints(26);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titulo);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));

            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (Processo proc : processos) {
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(18);
                int col = 0;
                createCell(row, col++, asText(proc.getId()), bodyStyle);
                createCell(row, col++, asText(proc.getNumeroInterno()), bodyStyle);
                createCell(row, col++, asText(proc.getNumeroProcesso()), bodyStyle);
                createCell(row, col++, proc.getDataInicio() != null ? proc.getDataInicio().format(DATE_FMT) : "-", bodyStyle);
                createCell(row, col++, proc.getStatus() != null ? proc.getStatus().name() : "-", bodyStyle);
                createCell(row, col++, proc.getPaciente() != null ? asText(proc.getPaciente().getNome()) : "-", bodyStyle);
                createCell(row, col++, proc.getPaciente() != null ? asText(proc.getPaciente().getCpf()) : "-", bodyStyle);
                createCell(row, col++, proc.getAdvogado() != null ? asText(proc.getAdvogado().getNome()) : "-", bodyStyle);
                createCell(row, col++, proc.getMedico() != null ? asText(proc.getMedico().getNome()) : "-", bodyStyle);
                createCell(row, col++, proc.getDoenca() != null ? asText(proc.getDoenca().getNome()) : "-", bodyStyle);
                createCell(row, col++, proc.getLocal() != null ? asText(proc.getLocal().getComarca()) : "-", bodyStyle);
                createCell(row, col++, proc.getTipoHospital() != null ? proc.getTipoHospital().name() : "-", bodyStyle);
                createCell(row, col++, proc.getHospital() != null ? asText(proc.getHospital().getNome()) : "-", bodyStyle);
                createCell(row, col++, boolLabel(proc.isCpfAnexado()), bodyStyle);
                createCell(row, col++, boolLabel(proc.isCompResidenciaAnexado()), bodyStyle);
                createCell(row, col++, boolLabel(proc.isCompRendaAnexado()), bodyStyle);
                createCell(row, col++, boolLabel(proc.isProcuracaoAnexado()), bodyStyle);
                createCell(row, col++, boolLabel(proc.isDeclaracaoInsuficienciaAnexado()), bodyStyle);
                createCell(row, col++, formatProdutos(proc.getItens()), bodyStyle);
                createCell(row, col++, formatDeferimentos(proc.getDeferimentos()), bodyStyle);
                createCell(row, col++, proc.getUltimoAcessoEm() != null ? proc.getUltimoAcessoEm().format(DATETIME_FMT) : "-", bodyStyle);
                createCell(row, col++, asText(proc.getUltimoAcessoPor()), bodyStyle);
                createCell(row, col++, proc.getUltimaEdicaoEm() != null ? proc.getUltimaEdicaoEm().format(DATETIME_FMT) : "-", bodyStyle);
                createCell(row, col++, asText(proc.getUltimaEdicaoPor()), bodyStyle);
                createCell(row, col, asText(proc.getObs()), bodyStyle);
            }

            sheet.setAutoFilter(new CellRangeAddress(1, 1, 0, headers.length - 1));
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(width + 700, 22000));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao gerar relatorio Excel de processos", e);
        }
    }

    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        return style;
    }

    private static CellStyle createBodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static String asText(Object value) {
        if (value == null) {
            return "-";
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "-" : text;
    }

    private static String boolLabel(boolean value) {
        return value ? "Sim" : "Nao";
    }

    private static String formatProdutos(List<ProcessoProduto> itens) {
        if (itens == null || itens.isEmpty()) {
            return "-";
        }
        return itens.stream()
                .map(i -> {
                    String produto = i.getProduto() != null ? asText(i.getProduto().getNomeItem()) : "-";
                    String qtd = i.getQuantidade() != null ? String.valueOf(i.getQuantidade()) : "-";
                    String envio = i.getDataEnvio() != null ? i.getDataEnvio().format(DATE_FMT) : "-";
                    return produto + " | Qtd: " + qtd + " | Env: " + envio;
                })
                .collect(Collectors.joining(" ; "));
    }

    private static String formatDeferimentos(List<Deferimento> deferimentos) {
        if (deferimentos == null || deferimentos.isEmpty()) {
            return "-";
        }
        return deferimentos.stream()
                .map(d -> {
                    String numero = d.getNumeroDeferimento() != null ? "#" + d.getNumeroDeferimento() : "#-";
                    String tipo = d.getTipo() != null ? d.getTipo().name() : "-";
                    String data = d.getDataDeferimento() != null ? d.getDataDeferimento().format(DATE_FMT) : "-";
                    String msg = asText(d.getMensagem());
                    return numero + " " + tipo + " (" + data + ") " + msg;
                })
                .collect(Collectors.joining(" ; "));
    }
}

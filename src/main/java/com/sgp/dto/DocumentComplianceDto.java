package com.sgp.dto;


public class DocumentComplianceDto {
    private String documento;
    private double percentual;

    public DocumentComplianceDto(String documento, double percentual) {
        this.documento = documento;
        this.percentual = percentual;
    }

    public String getDocumento() { return documento; }
    public double getPercentual() { return percentual; }
}

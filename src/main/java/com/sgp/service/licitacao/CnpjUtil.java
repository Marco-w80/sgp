package com.sgp.service.licitacao;

import java.util.Locale;

public final class CnpjUtil {
    private static final int[] PESOS_PRIMEIRO = {5,4,3,2,9,8,7,6,5,4,3,2};
    private static final int[] PESOS_SEGUNDO = {6,5,4,3,2,9,8,7,6,5,4,3,2};

    private CnpjUtil() {}

    public static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    public static String validarENormalizar(String valor) {
        String cnpj = normalizar(valor);
        if (cnpj == null) return null;
        if (!cnpj.matches("[A-Z0-9]{12}[0-9]{2}") || !digitosVerificadoresValidos(cnpj)) {
            throw new IllegalArgumentException("Informe um CNPJ válido.");
        }
        return cnpj;
    }

    public static String formatar(String valor) {
        String cnpj = normalizar(valor);
        if (cnpj == null || cnpj.length() != 14) return valor;
        return cnpj.substring(0,2)+"."+cnpj.substring(2,5)+"."+cnpj.substring(5,8)+"/"+
                cnpj.substring(8,12)+"-"+cnpj.substring(12);
    }

    private static boolean digitosVerificadoresValidos(String cnpj) {
        String base = cnpj.substring(0, 12);
        int primeiro = calcular(base, PESOS_PRIMEIRO);
        int segundo = calcular(base + primeiro, PESOS_SEGUNDO);
        return cnpj.endsWith("" + primeiro + segundo);
    }

    private static int calcular(String valor, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < valor.length(); i++) soma += (valor.charAt(i) - '0') * pesos[i];
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}

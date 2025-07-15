package com.sgp.dto;

public class ProdutoDto {
    private Long id;
    private String nomeItem;

    public ProdutoDto(Long id, String nomeItem) {
        this.id = id;
        this.nomeItem = nomeItem;
    }

    public Long getId() {
        return id;
    }

    public String getNomeItem() {
        return nomeItem;
    }
}

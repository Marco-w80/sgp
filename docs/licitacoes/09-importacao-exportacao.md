# Importação e exportação

## Itens de licitação

A interface aceita texto, delimitador Automático/TAB/vírgula/ponto e vírgula/múltiplos espaços e indicador de cabeçalho. A prévia mostra até seis linhas e mapeia número, descrição, quantidade, unidade e valor. Índice `-1` ignora coluna. Descrição é obrigatória; número ausente recebe sequência após o maior número já existente ou explicitamente importado; quantidade/unidade assumem `1`/`UN`.

## Números brasileiros

`ImportacaoTabularService.decimal` converte `45,00`, `1.250,50` e `6.600` respectivamente em `45.00`, `1250.50` e `6600`. CSV com vírgulas aceita campos entre aspas, inclusive valores como `"45,00"`. Erros indicam o valor inválido.

## Lotes

Itens de licitação e de fornecedor usam `JdbcTemplate.batchUpdate` em transação, evitando uma chamada de insert por registro. A restrição única `(licitacao_id, numero_item)` impede duplicação de números dentro da mesma licitação.

## Exportação

`ExportacaoCotacaoService` gera CSV com Nº Item, Descrição, Quantidade, Unidade, Valor Referência, Fornecedor, Valor Unitário, Data e Observações. O arquivo usa UTF-8 com BOM, `;` e CRLF, compatível com Excel em português.

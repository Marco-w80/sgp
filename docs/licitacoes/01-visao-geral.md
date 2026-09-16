# Visão geral

O módulo gerencia o ciclo completo de uma licitação: cadastro dos dados e itens, coleta de cotações, decisão item a item, definição da participação e registro do resultado. Também oferece histórico imutável de transições, anexos, estoque do portfólio e cadastro de fornecedores com itens.

O ponto de entrada é `/licitacoes`. O Dashboard usa Kanban com Cadastro, Cotação, Aprovação, Definição, Participação e a coluna visual Em andamento. A busca alcança dados da licitação e dos itens. Licitações arquivadas ficam fora da visão ativa, mas podem ser consultadas.

Fluxo oficial: `CADASTRO → COTACAO → APROVACAO → DEFINICAO → PARTICIPACAO`. A tela inicial dos detalhes abre na etapa atual, mas permite consultar qualquer aba sem alterar o estado.

O projeto de referência `_referencia/SISTEMA` e o arquivo `server/data/sistema.db` não estavam presentes no workspace em 15/09/2026. A implementação foi baseada no briefing funcional; a validação visual e o mapeamento definitivo do SQLite permanecem condicionados ao fornecimento desses artefatos.

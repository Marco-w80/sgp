# Anexos

`LicitacaoAnexoService` aceita PDF, DOC, DOCX, XLS, XLSX, JPG/JPEG e PNG, até 20 MB. Cada arquivo pode apontar para item e etapa ou ser geral.

A raiz vem de `app.licitacoes.upload-dir`, por padrão `${LICITACOES_UPLOAD_DIR:./data/licitacoes}`. Em produção, `LICITACOES_UPLOAD_DIR` deve apontar para volume persistente, gravável e incluído em backup. O repositório ignora `/data/`.

O nome físico é UUID mais extensão; o nome original fica só nos metadados. Caminhos são resolvidos e normalizados sob a raiz para evitar traversal. Em erro de persistência, o arquivo recém-copiado é removido. Exclusões de anexo, item e licitação tratam o arquivo físico.

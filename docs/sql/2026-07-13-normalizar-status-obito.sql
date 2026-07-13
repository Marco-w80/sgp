-- Permite persistir o identificador oficial OBITO antes de normalizar o legado.
ALTER TABLE processos
    MODIFY COLUMN status ENUM('ABERTO', 'EM_ANDAMENTO', 'OBITO', 'CONCLUIDO', 'SUSPENSO') NOT NULL;

ALTER TABLE processos_excluidos
    MODIFY COLUMN status ENUM('ABERTO', 'EM_ANDAMENTO', 'OBITO', 'CONCLUIDO', 'SUSPENSO') NULL;

-- CONCLUIDO era apresentado como Óbito pela aplicação. A normalização faz o
-- valor físico acompanhar a representação interna e visual já adotada.
UPDATE processos
SET status = 'OBITO'
WHERE status = 'CONCLUIDO';

UPDATE processos_excluidos
SET status = 'OBITO'
WHERE status = 'CONCLUIDO';

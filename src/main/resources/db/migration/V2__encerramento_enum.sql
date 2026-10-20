ALTER TABLE encerramentos ALTER COLUMN situacao_final_anomalia TYPE VARCHAR(20);
ALTER TABLE encerramentos ADD CONSTRAINT chk_encerramentos_situacao_final
    CHECK (situacao_final_anomalia IN ('DETECTADA','EM_ANALISE','CONFIRMADA','EM_CORRECAO','ESTABILIZADA','IRREVERSIVEL'));

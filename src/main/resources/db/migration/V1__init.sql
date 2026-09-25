CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE agentes (
    id UUID PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    codinome VARCHAR(100) NOT NULL,
    especialidade VARCHAR(100) NOT NULL,
    situacao VARCHAR(20) NOT NULL CHECK (situacao IN ('DISPONIVEL','EM_MISSAO','SUSPENSO','INATIVO')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_agentes_codinome UNIQUE (codinome)
);

CREATE TABLE eventos (
    id UUID PRIMARY KEY,
    titulo VARCHAR(300) NOT NULL,
    descricao TEXT,
    data_evento DATE NOT NULL,
    localizacao VARCHAR(300),
    importancia VARCHAR(20) NOT NULL CHECK (importancia IN ('BAIXA','MODERADA','ALTA','CRITICA')),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE anomalias (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL REFERENCES eventos(id),
    divergencia TEXT NOT NULL,
    detectada_em TIMESTAMPTZ NOT NULL,
    risco VARCHAR(20) NOT NULL CHECK (risco IN ('BAIXO','MODERADO','ALTO','CRITICO')),
    situacao VARCHAR(20) NOT NULL CHECK (situacao IN ('DETECTADA','EM_ANALISE','CONFIRMADA','EM_CORRECAO','ESTABILIZADA','IRREVERSIVEL')),
    origem VARCHAR(300),
    observacoes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_anomalias_evento ON anomalias(evento_id);
CREATE INDEX idx_anomalias_situacao ON anomalias(situacao);
CREATE INDEX idx_anomalias_risco ON anomalias(risco);

CREATE TABLE missoes (
    id UUID PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL,
    objetivo TEXT NOT NULL,
    anomalia_id UUID NOT NULL REFERENCES anomalias(id),
    destino_temporal VARCHAR(300),
    prioridade VARCHAR(20) NOT NULL CHECK (prioridade IN ('BAIXA','NORMAL','ALTA','EMERGENCIA')),
    situacao VARCHAR(20) NOT NULL CHECK (situacao IN ('PLANEJADA','AUTORIZADA','EM_EXECUCAO','CONCLUIDA','CANCELADA','FALHOU')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_missoes_codigo UNIQUE (codigo)
);
CREATE INDEX idx_missoes_anomalia ON missoes(anomalia_id);
CREATE INDEX idx_missoes_situacao ON missoes(situacao);

CREATE TABLE missao_agentes (
    missao_id UUID NOT NULL REFERENCES missoes(id) ON DELETE CASCADE,
    agente_id UUID NOT NULL REFERENCES agentes(id),
    PRIMARY KEY (missao_id, agente_id)
);

CREATE TABLE autorizacoes (
    id UUID PRIMARY KEY,
    missao_id UUID NOT NULL REFERENCES missoes(id) ON DELETE CASCADE,
    responsavel VARCHAR(200) NOT NULL,
    instante TIMESTAMPTZ NOT NULL,
    observacao TEXT
);
CREATE INDEX idx_autorizacoes_missao ON autorizacoes(missao_id);

CREATE TABLE intervencoes (
    id UUID PRIMARY KEY,
    missao_id UUID NOT NULL REFERENCES missoes(id) ON DELETE CASCADE,
    agente_id UUID NOT NULL REFERENCES agentes(id),
    instante TIMESTAMPTZ NOT NULL,
    acao TEXT NOT NULL,
    justificativa TEXT,
    impacto VARCHAR(20) NOT NULL CHECK (impacto IN ('MINIMO','CONTROLADO','SIGNIFICATIVO','SEVERO')),
    resultado TEXT
);
CREATE INDEX idx_intervencoes_missao ON intervencoes(missao_id);

CREATE TABLE encerramentos (
    id UUID PRIMARY KEY,
    missao_id UUID NOT NULL UNIQUE REFERENCES missoes(id) ON DELETE CASCADE,
    resultado TEXT NOT NULL,
    resumo TEXT,
    impacto_observado TEXT,
    situacao_final_anomalia VARCHAR(30),
    observacoes TEXT,
    encerrada_em TIMESTAMPTZ NOT NULL
);

CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL CHECK (perfil IN ('OPERADOR','AGENTE','SUPERVISOR','ADMINISTRADOR')),
    agente_id UUID REFERENCES agentes(id),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_usuarios_username UNIQUE (username)
);

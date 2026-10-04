-- Garantir criação das tabelas caso o Flyway execute antes do Hibernate em base limpa
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    google_subject_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    nome VARCHAR(255),
    foto_perfil VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS contas (
    id BIGSERIAL PRIMARY KEY,
    usuario_id VARCHAR(255) NOT NULL,
    descricao VARCHAR(20) NOT NULL,
    observacoes VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    usuario_id VARCHAR(255) NOT NULL,
    descricao VARCHAR(20) NOT NULL,
    observacoes VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS despesas_fixas (
    id BIGSERIAL PRIMARY KEY,
    usuario_id VARCHAR(255) NOT NULL,
    descricao VARCHAR(100) NOT NULL,
    valor NUMERIC(9, 2) NOT NULL,
    conta_id BIGINT NOT NULL REFERENCES contas(id),
    categoria_id BIGINT NOT NULL REFERENCES categorias(id),
    observacoes VARCHAR(300),
    data_inicio DATE
);

CREATE TABLE IF NOT EXISTS receitas_fixas (
    id BIGSERIAL PRIMARY KEY,
    usuario_id VARCHAR(255) NOT NULL,
    descricao VARCHAR(100) NOT NULL,
    valor NUMERIC(9, 2) NOT NULL,
    conta_id BIGINT NOT NULL REFERENCES contas(id),
    categoria_id BIGINT NOT NULL REFERENCES categorias(id),
    observacoes VARCHAR(300),
    data_inicio DATE
);

CREATE TABLE IF NOT EXISTS despesas_variaveis (
    id BIGSERIAL PRIMARY KEY,
    usuario_id VARCHAR(255) NOT NULL,
    descricao VARCHAR(100) NOT NULL,
    local_compra VARCHAR(100),
    data_compra DATE,
    valor_parcela NUMERIC(9, 2) NOT NULL,
    quantidade_parcelas INTEGER NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    conta_id BIGINT NOT NULL REFERENCES contas(id),
    categoria_id BIGINT NOT NULL REFERENCES categorias(id),
    observacoes VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS receitas_variaveis (
    id BIGSERIAL PRIMARY KEY,
    usuario_id VARCHAR(255) NOT NULL,
    descricao VARCHAR(100) NOT NULL,
    valor_parcela NUMERIC(9, 2) NOT NULL,
    quantidade_parcelas INTEGER NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    conta_id BIGINT NOT NULL REFERENCES contas(id),
    categoria_id BIGINT NOT NULL REFERENCES categorias(id),
    observacoes VARCHAR(300)
);

-- Índices de performance simples e compostos com tenant (usuario_id)
CREATE INDEX IF NOT EXISTS idx_usuarios_google_subject_id ON usuarios (google_subject_id);
CREATE INDEX IF NOT EXISTS idx_despesas_fixas_data_inicio ON despesas_fixas (data_inicio);
CREATE INDEX IF NOT EXISTS idx_despesas_fixas_usuario_data_inicio ON despesas_fixas (usuario_id, data_inicio);
CREATE INDEX IF NOT EXISTS idx_receitas_fixas_data_inicio ON receitas_fixas (data_inicio);
CREATE INDEX IF NOT EXISTS idx_receitas_fixas_usuario_data_inicio ON receitas_fixas (usuario_id, data_inicio);
CREATE INDEX IF NOT EXISTS idx_despesas_variaveis_datas ON despesas_variaveis (data_inicio, data_fim);
CREATE INDEX IF NOT EXISTS idx_despesas_variaveis_usuario_datas ON despesas_variaveis (usuario_id, data_inicio, data_fim);
CREATE INDEX IF NOT EXISTS idx_receitas_variaveis_datas ON receitas_variaveis (data_inicio, data_fim);
CREATE INDEX IF NOT EXISTS idx_receitas_variaveis_usuario_datas ON receitas_variaveis (usuario_id, data_inicio, data_fim);

-- Seed de Dados - Projeto Aureus
-- Pode ser executado via psql ou pgAdmin/DBeaver

DO $$ 
DECLARE
    v_usuario_id VARCHAR := '1'; -- ID interno 1 do usuário
    v_conta_corrente_id BIGINT;
    v_conta_nubank_id BIGINT;
    v_conta_bb_id BIGINT;
    v_cat_essencial_id BIGINT;
    v_cat_nao_essencial_id BIGINT;
BEGIN
    -- Garante que o usuário com id = 1 existe
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE id = 1) THEN
        -- O ID 1 será inserido caso a tabela permita inserção explícita no campo ID (OVERRIDING SYSTEM VALUE se aplicável)
        -- Ou simplesmente insere um registro genérico para não travar
        INSERT INTO usuarios (id, google_subject_id, email, nome) 
        VALUES (1, 'mock-subject-1', 'teste@aureus.com', 'Usuário 1');
    END IF;

    -- ==========================================
    -- 1. CRIAR CONTAS
    -- ==========================================
    INSERT INTO contas (descricao, usuario_id) VALUES ('Conta Corrente', v_usuario_id) RETURNING id INTO v_conta_corrente_id;
    INSERT INTO contas (descricao, usuario_id) VALUES ('Cartão Nubank', v_usuario_id) RETURNING id INTO v_conta_nubank_id;
    INSERT INTO contas (descricao, usuario_id) VALUES ('Cartão BB', v_usuario_id) RETURNING id INTO v_conta_bb_id;

    -- ==========================================
    -- 2. CRIAR CATEGORIAS
    -- ==========================================
    INSERT INTO categorias (descricao, usuario_id) VALUES ('Essencial', v_usuario_id) RETURNING id INTO v_cat_essencial_id;
    INSERT INTO categorias (descricao, usuario_id) VALUES ('Não Essencial', v_usuario_id) RETURNING id INTO v_cat_nao_essencial_id;

    -- ==========================================
    -- 3. POPULAR DESPESAS FIXAS
    -- ==========================================
    INSERT INTO despesas_fixas (descricao, valor, conta_id, categoria_id, usuario_id) 
    VALUES ('Aluguel', 1500.00, v_conta_corrente_id, v_cat_essencial_id, v_usuario_id);
    
    INSERT INTO despesas_fixas (descricao, valor, conta_id, categoria_id, usuario_id) 
    VALUES ('Internet', 100.00, v_conta_nubank_id, v_cat_essencial_id, v_usuario_id);

    -- ==========================================
    -- 4. POPULAR DESPESAS VARIÁVEIS
    -- ==========================================
    INSERT INTO despesas_variaveis (descricao, local_compra, data_compra, valor_parcela, quantidade_parcelas, data_inicio, data_fim, categoria_id, conta_id, usuario_id)
    VALUES ('Supermercado Mês', 'Supermercado Extra', CURRENT_DATE, 600.00, 1, CURRENT_DATE, CURRENT_DATE, v_cat_essencial_id, v_conta_bb_id, v_usuario_id);

    INSERT INTO despesas_variaveis (descricao, local_compra, data_compra, valor_parcela, quantidade_parcelas, data_inicio, data_fim, categoria_id, conta_id, usuario_id)
    VALUES ('Smartphone Novo', 'Loja de Eletrônicos', CURRENT_DATE, 250.00, 12, CURRENT_DATE, CURRENT_DATE + INTERVAL '11 months', v_cat_nao_essencial_id, v_conta_nubank_id, v_usuario_id);

    -- ==========================================
    -- 5. POPULAR RECEITAS FIXAS
    -- ==========================================
    INSERT INTO receitas_fixas (descricao, valor, conta_id, categoria_id, usuario_id) 
    VALUES ('Salário', 5000.00, v_conta_corrente_id, v_cat_essencial_id, v_usuario_id);

    -- ==========================================
    -- 6. POPULAR RECEITAS VARIÁVEIS
    -- ==========================================
    INSERT INTO receitas_variaveis (descricao, valor_parcela, quantidade_parcelas, data_inicio, data_fim, categoria_id, conta_id, usuario_id)
    VALUES ('Freelance Design', 1200.00, 1, CURRENT_DATE, CURRENT_DATE, v_cat_nao_essencial_id, v_conta_corrente_id, v_usuario_id);

    INSERT INTO receitas_variaveis (descricao, valor_parcela, quantidade_parcelas, data_inicio, data_fim, categoria_id, conta_id, usuario_id)
    VALUES ('Venda de Bicicleta', 300.00, 3, CURRENT_DATE, CURRENT_DATE + INTERVAL '2 months', v_cat_nao_essencial_id, v_conta_corrente_id, v_usuario_id);

END $$;

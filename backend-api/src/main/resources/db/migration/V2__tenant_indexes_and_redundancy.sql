-- Os compostos existentes já cobrem usuario_id nas movimentações.
CREATE INDEX IF NOT EXISTS idx_contas_usuario_id ON contas (usuario_id);
CREATE INDEX IF NOT EXISTS idx_categorias_usuario_id ON categorias (usuario_id);

-- IF NOT EXISTS não comprova definição/validade de um índice preexistente.
DO $$
DECLARE
    tabela TEXT;
BEGIN
    FOREACH tabela IN ARRAY ARRAY['contas', 'categorias'] LOOP
        IF NOT EXISTS (
            SELECT 1
            FROM pg_index i
            JOIN pg_class c ON c.oid = i.indexrelid
            JOIN pg_am am ON am.oid = c.relam
            JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = i.indkey[0]
            WHERE i.indrelid = to_regclass(format('%I.%I', current_schema(), tabela))
              AND c.relnamespace = current_schema()::regnamespace
              AND c.relname = 'idx_' || tabela || '_usuario_id'
              AND am.amname = 'btree' AND a.attname = 'usuario_id'
              AND i.indcollation[0] = a.attcollation
              AND i.indisvalid AND i.indisready AND i.indislive
              AND i.indnkeyatts = 1 AND i.indnatts = 1
              AND i.indpred IS NULL AND i.indexprs IS NULL
        ) THEN
            RAISE EXCEPTION 'Índice obrigatório de tenant inválido ou diferente: idx_%_usuario_id', tabela;
        END IF;
    END LOOP;

    -- Não recriar nem substituir compostos: uma base divergente exige inspeção.
    FOREACH tabela IN ARRAY ARRAY[
        'despesas_fixas', 'receitas_fixas', 'despesas_variaveis', 'receitas_variaveis'
    ] LOOP
        IF NOT EXISTS (
            SELECT 1
            FROM pg_index i
            JOIN pg_class c ON c.oid = i.indexrelid
            JOIN pg_am am ON am.oid = c.relam
            JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = i.indkey[0]
            JOIN pg_attribute inicio ON inicio.attrelid = i.indrelid
                AND inicio.attname = 'data_inicio' AND inicio.attnum = i.indkey[1]
            WHERE i.indrelid = to_regclass(format('%I.%I', current_schema(), tabela))
              AND c.relnamespace = current_schema()::regnamespace
              AND c.relname = 'idx_' || tabela || '_usuario_' ||
                  CASE WHEN tabela LIKE '%variaveis' THEN 'datas' ELSE 'data_inicio' END
              AND am.amname = 'btree' AND a.attname = 'usuario_id'
              AND i.indcollation[0] = a.attcollation
              AND i.indisvalid AND i.indisready AND i.indislive
              AND i.indnkeyatts = CASE WHEN tabela LIKE '%variaveis' THEN 3 ELSE 2 END
              AND i.indnatts = i.indnkeyatts
              AND i.indpred IS NULL AND i.indexprs IS NULL
              AND (tabela NOT LIKE '%variaveis' OR i.indkey[2] = (
                  SELECT fim.attnum FROM pg_attribute fim
                  WHERE fim.attrelid = i.indrelid AND fim.attname = 'data_fim'
              ))
        ) THEN
            RAISE EXCEPTION 'Cobertura de tenant inutilizável no índice composto existente de %', tabela;
        END IF;
    END LOOP;
END $$;

-- A existência de UNIQUE no DDL original não substitui a inspeção deste banco alvo.
-- O bloqueio mantém estáveis tabela/constraints durante a inspeção e a remoção.
LOCK TABLE usuarios IN SHARE ROW EXCLUSIVE MODE;
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_index secundario
        JOIN pg_class indice ON indice.oid = secundario.indexrelid
        JOIN pg_am am ON am.oid = indice.relam
        JOIN pg_attribute coluna ON coluna.attrelid = secundario.indrelid
            AND coluna.attname = 'google_subject_id' AND NOT coluna.attisdropped
        JOIN pg_constraint restricao ON restricao.conrelid = secundario.indrelid
            AND restricao.contype = 'u' AND restricao.convalidated
            AND restricao.conkey = ARRAY[coluna.attnum]
        JOIN pg_index unico ON unico.indexrelid = restricao.conindid
        JOIN pg_class indice_unico ON indice_unico.oid = unico.indexrelid
        WHERE secundario.indrelid = to_regclass(format('%I.usuarios', current_schema()))
          AND indice.relnamespace = current_schema()::regnamespace
          AND indice.relname = 'idx_usuarios_google_subject_id'
          AND am.amname = 'btree' AND indice_unico.relam = indice.relam
          AND secundario.indisvalid AND secundario.indisready AND secundario.indislive
          AND NOT secundario.indisunique AND NOT secundario.indisprimary
          AND NOT secundario.indisexclusion AND NOT secundario.indisclustered
          AND NOT secundario.indisreplident
          AND secundario.indnkeyatts = 1 AND secundario.indnatts = 1
          AND secundario.indkey[0] = coluna.attnum
          AND secundario.indpred IS NULL AND secundario.indexprs IS NULL
          AND unico.indrelid = secundario.indrelid
          AND unico.indisvalid AND unico.indisready AND unico.indislive
          AND unico.indisunique AND unico.indimmediate
          AND unico.indnkeyatts = 1 AND unico.indnatts = 1
          AND unico.indpred IS NULL AND unico.indexprs IS NULL
          AND unico.indkey = secundario.indkey
          AND unico.indclass = secundario.indclass
          AND unico.indcollation = secundario.indcollation
          AND unico.indoption = secundario.indoption
          AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conindid = indice.oid)
          AND NOT EXISTS (
              SELECT 1 FROM pg_depend d
              WHERE d.refclassid = 'pg_class'::regclass AND d.refobjid = indice.oid
          )
          AND NOT EXISTS (
              SELECT 1 FROM pg_depend d
              WHERE d.classid = 'pg_class'::regclass AND d.objid = indice.oid
                AND NOT (d.refclassid = 'pg_class'::regclass
                    AND d.refobjid = secundario.indrelid
                    AND d.refobjsubid = coluna.attnum AND d.deptype = 'a')
          )
    ) THEN
        EXECUTE format('DROP INDEX %I.idx_usuarios_google_subject_id', current_schema());
        RAISE NOTICE 'Secundário equivalente e independente removido; UNIQUE preservada';
    ELSE
        RAISE NOTICE 'Secundário ausente ou pré-condições não comprovadas; catálogo preservado. Conferir pendência no banco alvo';
    END IF;
END $$;

-- As configurações dos índices de FKs e temporais permanecem preservadas.

"""Compara índices apenas em um PostgreSQL Docker novo e descartável.

Uso na raiz: python backend-api/scripts/evaluate-indexes.py
Não recebe URL/credenciais de banco externo. Requer somente Python e Docker.
"""

import argparse
import hashlib
import json
import pathlib
import subprocess
import tempfile
import time
import uuid


ROOT = pathlib.Path(__file__).resolve().parents[2]
TABLES = ["despesas_fixas", "receitas_fixas", "despesas_variaveis", "receitas_variaveis"]
PROTOCOL = {
    "linhas_por_movimentacao": 40000,
    "tenants": 100,
    "distribuicao": "tenant-000: 16000 (40%); demais 99: 242/243; 4 vínculos por tenant/tabela",
    "datas": "2016-01-01 + (g*17 mod 3650) dias; fixas: 5% nulas; variáveis: fim=início+330 dias",
    "seletividade": "tenant-042: ~0,606%; tenant-000: 40%; FK presente: ~0,152%; FK ausente: 0%",
    "repeticoes": 5,
    "aquecimentos": 1,
    "estado_fisico": "VACUUM FULL e ANALYZE antes de cada estado; escritas por último e sempre com rollback",
    "update": "1000 linhas (IDs 20001–21000), rotacionando os pais entre quatro vínculos do tenant de cada linha",
    "criterio_previo": (
        "Comparar medianas de tempo, blocos compartilhados hit+read, WAL e bytes de índice. "
        "Sinal relevante: redução >=50% de blocos e >=1 ms na operação, ou >=10% de espaço/escrita. "
        "Adicionar FK exige ainda frequência/volume real que justifique o custo contínuo de escrita; "
        "a amostra não contém essa frequência. Remover temporal exige equivalência para o trabalho "
        "real e ausência de acesso distinto demonstrado. Preservar quando a evidência for insuficiente."
    ),
    "limites": "Cache aquecido, sem concorrência, host de desenvolvimento, dados sintéticos; sem SLA/frequência real.",
}
CATALOG = """
SELECT coalesce(json_agg(x ORDER BY tabela, nome),'[]'::json) FROM (
 SELECT t.relname tabela, c.relname nome, pg_get_indexdef(c.oid) definicao,
        i.indisvalid valido, i.indisready pronto, i.indislive vivo,
        i.indisunique unico, i.indisprimary primario,
        pg_relation_size(c.oid) bytes,
        (SELECT coalesce(json_agg(json_build_object('nome',conname,'tipo',contype,
                 'validada',convalidated)),'[]') FROM pg_constraint WHERE conindid=c.oid) constraints,
        (SELECT coalesce(json_agg(json_build_object('tipo',d.deptype,
                 'objeto',pg_describe_object(d.classid,d.objid,d.objsubid))),'[]')
         FROM pg_depend d WHERE d.refclassid='pg_class'::regclass AND d.refobjid=c.oid) dependentes,
        (SELECT coalesce(json_agg(json_build_object('tipo',d.deptype,
                 'referencia',pg_describe_object(d.refclassid,d.refobjid,d.refobjsubid))),'[]')
         FROM pg_depend d WHERE d.classid='pg_class'::regclass AND d.objid=c.oid) dependencias
 FROM pg_index i JOIN pg_class c ON c.oid=i.indexrelid JOIN pg_class t ON t.oid=i.indrelid
 WHERE t.relnamespace='public'::regnamespace
) x;
"""
FK_CATALOG = """
SELECT coalesce(json_agg(x ORDER BY tabela,nome),'[]'::json) FROM (
 SELECT t.relname tabela, c.conname nome,
        (SELECT array_agg(a.attname ORDER BY k.ord)
         FROM unnest(c.conkey) WITH ORDINALITY k(attnum,ord)
         JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attnum=k.attnum) colunas,
        r.relname tabela_referenciada,
        (SELECT array_agg(a.attname ORDER BY k.ord)
         FROM unnest(c.confkey) WITH ORDINALITY k(attnum,ord)
         JOIN pg_attribute a ON a.attrelid=c.confrelid AND a.attnum=k.attnum) colunas_referenciadas,
        pg_get_constraintdef(c.oid,true) definicao, c.convalidated validada,
        c.confupdtype acao_update, c.confdeltype acao_delete, c.confmatchtype match,
        c.condeferrable adiavel, c.condeferred inicialmente_adiada
 FROM pg_constraint c JOIN pg_class t ON t.oid=c.conrelid
 JOIN pg_class r ON r.oid=c.confrelid
 WHERE c.contype='f' AND c.connamespace='public'::regnamespace
) x;
"""


def next_parent_id(ordinal, prefix=""):
    return f"{ordinal * 100000}+right({prefix}usuario_id,3)::integer*10+({prefix}id+1)%4+1"


def validate_failed_delete(result):
    if result.get("sqlstate") != "23503":
        raise RuntimeError(f"DELETE com vínculo retornou SQLSTATE {result.get('sqlstate')}; esperado 23503")


def command(args, sql=None):
    result = subprocess.run(args, input=sql, text=True, encoding="utf-8", capture_output=True)
    if result.returncode:
        raise RuntimeError(f"Comando falhou ({result.returncode}): {args}\n{result.stderr}")
    return result.stdout


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=pathlib.Path, default=ROOT / "_bmad-output/implementation-artifacts/7-4-index-evidence/measurement.json")
    parser.add_argument("--image", default="postgres:16-alpine", help="Imagem PostgreSQL, incluindo referência por digest")
    args = parser.parse_args()
    name = "aureus-index-evaluation-" + uuid.uuid4().hex[:12]
    output = {"protocolo_definido_antes_da_coleta": PROTOCOL,
              "revisao": command(["git", "-C", str(ROOT), "rev-parse", "HEAD"]).strip(),
              "data_utc": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
              "imagem_referencia": args.image, "candidatos": [], "validacao_update_titularidade": []}
    output["sha256_fontes"] = {str(path.relative_to(ROOT)): hashlib.sha256(path.read_bytes()).hexdigest()
                              for path in [pathlib.Path(__file__), ROOT / "backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql",
                                           ROOT / "backend-api/src/main/resources/db/migration/V2__tenant_indexes_and_redundancy.sql"]
                              if path.exists()}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    # A coleta publicada permanece intacta; somente o arquivo privado recebe progresso.
    with tempfile.NamedTemporaryFile(prefix=args.output.name + ".", suffix=".pending",
                                     dir=args.output.parent, delete=False) as pending:
        progress_path = pathlib.Path(pending.name)
    progress_path.write_text(json.dumps(output, ensure_ascii=False, indent=2), encoding="utf-8")

    def sql(query):
        return command(["docker", "exec", "-i", name, "psql", "-XqAt", "-v", "ON_ERROR_STOP=1", "-U", "postgres", "-d", "indices"], query)

    def value(query):
        return json.loads(sql(query))

    def fk_catalog():
        catalog = value(FK_CATALOG)
        if len(catalog) != 8 or not all(fk["validada"] for fk in catalog):
            raise RuntimeError("Inventário esperado: exatamente oito FKs validadas")
        return catalog

    def explain(query):
        return value("BEGIN; EXPLAIN (ANALYZE, BUFFERS, WAL, FORMAT JSON) " + query + "; ROLLBACK;")[0]

    def sample(query, failed_delete=False):
        results = []
        for n in range(PROTOCOL["aquecimentos"] + PROTOCOL["repeticoes"]):
            result = (value("BEGIN; SELECT medir_delete(" + "'" + query.replace("'", "''") + "'); ROLLBACK;")
                      if failed_delete else explain(query))
            if failed_delete:
                validate_failed_delete(result)
            if n >= PROTOCOL["aquecimentos"]:
                results.append(result)
        if failed_delete:
            return {"sql": query, "amostras": results}
        return {"sql": query, "plano_representativo": results[0], "amostras": [
            {"tempo_ms": result["Execution Time"], "planejamento_ms": result["Planning Time"],
             "blocos_hit": result["Plan"].get("Shared Hit Blocks", 0),
             "blocos_read": result["Plan"].get("Shared Read Blocks", 0),
             "blocos_dirty": result["Plan"].get("Shared Dirtied Blocks", 0),
             "wal_bytes": result["Plan"].get("WAL Bytes", 0), "triggers": result.get("Triggers", [])}
            for result in results]}

    def collect(table, ordinal, column=None):
        sql(f"VACUUM FULL {table}; ANALYZE {table};")
        base = ordinal * 100000
        present, absent = base + 421, base + 429
        variable = table.endswith("variaveis")
        fields = "valor_parcela,quantidade_parcelas,data_fim" if variable else "valor"
        amounts = "123.45,12,DATE '2026-12-01'" if variable else "123.45"
        queries = {
            "listagem_real_tenant_seletivo": f"SELECT * FROM {table} WHERE usuario_id='tenant-042' ORDER BY descricao",
            "consolidacao_real_tenant_maior": f"SELECT * FROM {table} WHERE usuario_id='tenant-000'",
            "periodo_tenant_hipotetico": f"SELECT * FROM {table} WHERE usuario_id='tenant-042' AND data_inicio >= DATE '2025-01-01' AND data_inicio < DATE '2025-02-01'",
            "periodo_global_hipotetico": f"SELECT * FROM {table} WHERE data_inicio >= DATE '2025-01-01' AND data_inicio < DATE '2025-02-01'",
            "insert_1000": f"INSERT INTO {table}(usuario_id,descricao,conta_id,categoria_id,data_inicio,{fields}) SELECT 'tenant-042','Escrita',{present},{present},DATE '2026-01-01',{amounts} FROM generate_series(1,1000)",
            "update_1000": f"UPDATE {table} SET data_inicio=data_inicio+1,conta_id={next_parent_id(ordinal)},categoria_id={next_parent_id(ordinal)} WHERE id BETWEEN 20001 AND 21000",
        }
        if column:
            parent = "contas" if column == "conta_id" else "categorias"
            queries.update({
                "sondagem_presente_hipotetica": f"SELECT 1 FROM {table} WHERE {column}={present} LIMIT 1",
                "sondagem_ausente_hipotetica": f"SELECT 1 FROM {table} WHERE {column}={absent} LIMIT 1",
                "delete_ausente_real_rollback": f"DELETE FROM {parent} WHERE id={absent} AND usuario_id='tenant-042'",
            })
        result = {key: sample(query) for key, query in queries.items()}
        if column:
            result["delete_presente_real_rejeitado"] = sample(
                f"DELETE FROM {parent} WHERE id={present} AND usuario_id='tenant-042'", failed_delete=True)
        return result

    try:
        command(["docker", "run", "--rm", "-d", "--name", name,
                 "-e", "POSTGRES_PASSWORD=descartavel", "-e", "POSTGRES_DB=indices", args.image])
        output["imagem_id"] = command(["docker", "inspect", "--format", "{{.Image}}", name]).strip()
        output["imagem_digest"] = json.loads(command(["docker", "image", "inspect", "--format", "{{json .RepoDigests}}", args.image]))
        for _ in range(60):
            ready = subprocess.run(["docker", "exec", name, "pg_isready", "-h", "127.0.0.1", "-U", "postgres"], capture_output=True)
            if ready.returncode == 0:
                break
            time.sleep(0.5)
        else:
            raise RuntimeError("PostgreSQL descartável não ficou pronto")
        sql((ROOT / "backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql").read_text(encoding="utf-8"))
        output["ambiente"] = value("SELECT json_build_object('versao',version(),'shared_buffers',current_setting('shared_buffers'),'work_mem',current_setting('work_mem'),'random_page_cost',current_setting('random_page_cost'),'effective_cache_size',current_setting('effective_cache_size'),'jit',current_setting('jit'))")
        output["catalogo_v1"] = value(CATALOG)
        output["fks_v1"] = fk_catalog()
        sql("CREATE INDEX idx_contas_usuario_id ON contas(usuario_id); CREATE INDEX idx_categorias_usuario_id ON categorias(usuario_id);")
        sql("""CREATE FUNCTION medir_delete(comando text) RETURNS jsonb LANGUAGE plpgsql AS $$
            DECLARE inicio timestamptz := clock_timestamp(); codigo text := '00000';
            BEGIN
              BEGIN EXECUTE comando;
              EXCEPTION WHEN foreign_key_violation THEN codigo := SQLSTATE; END;
              RETURN jsonb_build_object('sqlstate',codigo,'tempo_ms',extract(epoch FROM clock_timestamp()-inicio)*1000);
            END $$;""")
        for ordinal, table in enumerate(TABLES, 1):
            base = ordinal * 100000
            for parent in ["contas", "categorias"]:
                sql(f"INSERT INTO {parent}(id,usuario_id,descricao) SELECT {base}+u*10+s,'tenant-'||lpad(u::text,3,'0'),'Sintetica' FROM generate_series(0,99) u CROSS JOIN unnest(ARRAY[1,2,3,4,9]) s")
            variable = table.endswith("variaveis")
            fields = "valor_parcela,quantidade_parcelas,data_fim" if variable else "valor"
            amounts = "123.45,12,d+330" if variable else "123.45"
            date = "d" if variable else "CASE WHEN g%20=0 THEN NULL ELSE d END"
            sql(f"""INSERT INTO {table}(id,usuario_id,descricao,observacoes,conta_id,categoria_id,data_inicio,{fields})
                SELECT g,'tenant-'||lpad(u::text,3,'0'),'Movimentacao '||(g%1000),repeat('x',40),
                {base}+u*10+g%4+1,{base}+u*10+g%4+1,{date},{amounts}
                FROM (SELECT g,CASE WHEN g<=16000 THEN 0 ELSE 1+(g-16001)%99 END u,
                      DATE '2016-01-01'+((g*17)%3650)::int d FROM generate_series(1,40000) g) x;
                SELECT setval(pg_get_serial_sequence('{table}','id'),40000);""")
            ownership = value(f"""SELECT json_build_object('tabela','{table}','linhas',count(*),
                'vinculos_invalidos',count(*) FILTER (WHERE conta.usuario_id IS DISTINCT FROM m.usuario_id
                  OR categoria.usuario_id IS DISTINCT FROM m.usuario_id))
                FROM {table} m LEFT JOIN contas conta ON conta.id={next_parent_id(ordinal, 'm.')}
                LEFT JOIN categorias categoria ON categoria.id={next_parent_id(ordinal, 'm.')}
                WHERE m.id BETWEEN 20001 AND 21000""")
            if ownership["linhas"] != 1000 or ownership["vinculos_invalidos"] != 0:
                raise RuntimeError(f"Lote UPDATE não preserva titularidade: {ownership}")
            output["validacao_update_titularidade"].append(ownership)
        sql("VACUUM ANALYZE;")
        output["distribuicao_observada"] = value("SELECT json_agg(x) FROM (SELECT usuario_id,count(*) linhas FROM despesas_fixas GROUP BY usuario_id ORDER BY usuario_id) x")
        for ordinal, table in enumerate(TABLES, 1):
            temporal = "idx_" + table + ("_datas" if table.endswith("variaveis") else "_data_inicio")
            for column in ["conta_id", "categoria_id", None]:
                name_index = "idx_" + table + "_" + column if column else temporal
                print("Medindo " + name_index, flush=True)
                candidate = {"id": name_index, "tabela": table,
                             "mudanca_experimental": "adicionar FK" if column else "remover temporal",
                             "catalogo_antes": value(CATALOG), "fks_antes": fk_catalog(),
                             "antes": collect(table, ordinal, column)}
                sql(f"CREATE INDEX {name_index} ON {table}({column})" if column else f"DROP INDEX {name_index}")
                candidate["catalogo_depois"] = value(CATALOG)
                candidate["fks_depois"] = fk_catalog()
                if candidate["fks_antes"] != output["fks_v1"] or candidate["fks_depois"] != output["fks_v1"]:
                    raise RuntimeError("A alteração experimental modificou uma FK")
                candidate["depois"] = collect(table, ordinal, column)
                sql(f"DROP INDEX {name_index}" if column else
                    f"CREATE INDEX {name_index} ON {table}(data_inicio" + (",data_fim)" if table.endswith("variaveis") else ")"))
                output["candidatos"].append(candidate)
                progress_path.write_text(json.dumps(output, ensure_ascii=False, indent=2), encoding="utf-8")

        # Catálogo antes/depois da migração entregue, no mesmo banco sintético descartável.
        migration = ROOT / "backend-api/src/main/resources/db/migration/V2__tenant_indexes_and_redundancy.sql"
        output["catalogo_pre_v2"] = value(CATALOG)
        output["fks_pre_v2"] = fk_catalog()
        sql("BEGIN;" + migration.read_text(encoding="utf-8") + "COMMIT;")
        output["catalogo_pos_v2"] = value(CATALOG)
        output["fks_pos_v2"] = fk_catalog()
        if output["fks_pre_v2"] != output["fks_v1"] or output["fks_pos_v2"] != output["fks_v1"]:
            raise RuntimeError("A migração modificou uma FK")
        progress_path.write_text(json.dumps(output, ensure_ascii=False, indent=2), encoding="utf-8")
        progress_path.replace(args.output)
        print(str(args.output), flush=True)
    finally:
        subprocess.run(["docker", "rm", "-f", name], capture_output=True, check=False)
        progress_path.unlink(missing_ok=True)


if __name__ == "__main__":
    main()

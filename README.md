# Continuum Control

Sistema de Gestão e Controle da Integridade Temporal — Continuum Authority.

## 1. Serviços necessários

| Serviço | Versão | Papel |
|---|---|---|
| PostgreSQL | 16 | Relacional: agentes, eventos, anomalias, missões, autorizações, intervenções, encerramentos, usuários |
| Redis | 7 | Chave-valor: cache `ops:overview`, `stability:index` (TTL) + `jwt:blacklist:*` |
| MongoDB | 7 | Documentos: `ocorrencias` (schema variável) + `historico_anomalia` (append-only) |
| Java + Maven | 21 / 3.8+ | Build e execução |
| Docker + Compose | 29 / v5 | Testes (Testcontainers) e deploy |

## 2. Configuração sem recompilar

```bash
cp .env.example .env
```

`src/main/resources/application.yml` lê tudo via `${VAR:default}`:

| Var | Default | Uso |
|---|---|---|
| PORT | 8080 | Porta HTTP |
| DB_URL / DB_USER / DB_PASS | jdbc:postgresql://localhost:5432/continuum | JPA/Flyway |
| REDIS_HOST / REDIS_PORT | localhost / 6379 | Cache + blacklist |
| MONGO_URI | mongodb://continuum:continuum@localhost:27017/continuum | Ocorrências/histórico |
| JWT_SECRET / JWT_EXPIRATION | (trocar) / 86400000 | Auth |
| STABILITY_TTL / STABILITY_PESO_* | 60 / 15,5,3,10,8 | Índice 0-100 |
| SEED_ENABLED | true | Carga inicial dev |

## 3. Construir o artefato

```bash
./mvnw package -DskipTests
ls target/continuum-control-*.jar
```

## 4. Executar os testes

```bash
./mvnw test
```

Exige Docker ativo (Testcontainers sobe Postgres/Mongo/Redis isolados). Suíte: `MissaoRegrasTest` (8), `ApiContratoTest` (4), `SecurityTest` (2), contexto (1).

## 5. Subir os contêineres

```bash
cp .env.example .env
docker compose up --build -d
docker compose ps
docker compose logs -f app
```

## 6. Verificar se está operacional

```bash
curl localhost:8080/actuator/health
curl -X POST localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"continuum123"}'
TOKEN=$(curl -s -X POST localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"continuum123"}' | python3 -c "import json,sys; print(json.load(sys.stdin)['token'])")
curl localhost:8080/stability -H "Authorization: Bearer $TOKEN"
curl localhost:8080/ops/overview -H "Authorization: Bearer $TOKEN"
curl localhost:8080/anomalias/ativas -H "Authorization: Bearer $TOKEN"
```

Swagger: `http://localhost:8080/swagger-ui.html`

## 7. Fluxo de domínio

```bash
# criar evento + anomalia (OPERADOR+)
# criar missão PLANEJADA
# designar agente DISPONIVEL: POST /missoes/{id}/agentes {"agenteId":"..."}
# autorizar se EMERGENCIA ou risco CRITICO: POST /missoes/{id}/autorizacoes
# iniciar: POST /missoes/{id}/iniciar (vira EM_EXECUCAO, agente EM_MISSAO, anomalia EM_CORRECAO)
# intervir: POST /missoes/{id}/intervencoes (só EM_EXECUCAO)
# ocorrência: POST /missoes/{id}/ocorrencias {"tipo":"DISTORSAO_TEMPORAL",...}
# concluir: POST /missoes/{id}/concluir | falhar: POST /missoes/{id}/falhar
# histórico: GET /anomalias/{id}/historico
```

Regras: sem agente não inicia; EMERGENCIA/CRITICO exige AUTORIZADA; IRREVERSIVEL não recebe missão; falha não auto-resolve; agente com histórico não é removido.

## 8. Usuários seed (SEED_ENABLED=true)

`operador / agente1 / agente2 / supervisor / admin` — senha `continuum123`.

Perfis: OPERADOR (cadastra evento/anomalia), AGENTE (intervém só na própria missão), SUPERVISOR (autoriza/inicia/encerra), ADMINISTRADOR (tudo).

## 9. Persistência

| Banco | Guarda | Por que não no mesmo banco |
|---|---|---|
| PostgreSQL | Domínio relacional + `@Transactional` em iniciar/encerrar | Exige FK e atomicidade multi-tabela |
| Redis | Cache com TTL + blacklist com expiração | Recalcular estabilidade a cada GET seria caro; K-V com TTL é o modelo certo |
| MongoDB | Ocorrências polimórficas + histórico imutável | Tipos com campos distintos (distorção vs evidência); EAV no relacional piora |

## 10. Commits

Conventional Commits + escopos do projeto. Ver `.gitmessage`.

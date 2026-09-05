# Respostas da Atividade — BabaLegends

## 1. Quais tabelas você definiu inicialmente?

- `usuarios` — dados de cadastro do jogador (nome, e-mail, senha, foto)
- `grupos` — grupos fixos de peladeiros
- `grupo_membros` — associação entre usuário e grupo, incluindo o papel (`admin` ou `membro`)
- `atributos_jogador` — atributos usados para gerar a carta e calcular a nota de balanceamento
- `partidas` — data, horário, local, vagas e status de cada baba
- `presencas` — confirmação (RSVP) de cada jogador em cada partida
- `times` — os times sorteados dentro de uma partida
- `time_jogadores` — associação entre time e jogadores sorteados
- `votos` — votos dados pelos jogadores ao final de cada partida (categoria + votante + votado)

## 2. Você utilizou migrations? Se sim, quantas migrations? Descreva em uma frase o que cada uma faz.

Sim, via **Flyway**. Uma migration por tabela, seguindo a convenção `V{n}__descricao.sql`:

| Migration | Descrição |
|---|---|
| `V1__create_usuarios_table.sql` | Cria a tabela de usuários/jogadores |
| `V2__create_grupos_table.sql` | Cria a tabela de grupos |
| `V3__create_grupo_membros_table.sql` | Cria a associação entre usuários e grupos, com papel (admin/membro) |
| `V4__create_atributos_jogador_table.sql` | Cria a tabela de atributos usados nas cartas e no balanceamento |
| `V5__create_partidas_table.sql` | Cria a tabela de partidas agendadas |
| `V6__create_presencas_table.sql` | Cria a tabela de confirmação de presença |
| `V7__create_times_table.sql` | Cria a tabela de times sorteados por partida |
| `V8__create_time_jogadores_table.sql` | Cria a associação entre times e jogadores sorteados |
| `V9__create_votos_table.sql` | Cria a tabela de votação pós-partida |

## 3. Qual o caminho do arquivo que gera a seed do seu banco?

```
src/main/resources/db/dev/R__seed_dados_teste.sql
```

## 4. Quais os endpoints que você irá implementar inicialmente?

| Método | Path |
|---|---|
| POST | `/auth/registrar` |
| POST | `/auth/login` |
| POST | `/grupos` |
| POST | `/grupos/{id}/membros` |
| PATCH | `/grupos/{id}/membros/{usuarioId}` |
| POST | `/grupos/{id}/partidas` |
| POST | `/partidas/{id}/presencas` |
| POST | `/partidas/{id}/sorteio` |
| POST | `/partidas/{id}/votos` |
| GET | `/partidas/{id}` |

A priorização segue o fluxo principal da aplicação: cadastro e login são pré-requisito para tudo, e o resto cobre o fluxo uma pelada que é criar grupo, gerenciar membros, marcar partida, confirmar presença, sortear times balanceados e votar no final. O endpoint de gerenciamento de papel dentro do grupo tá incluso desde o início pra validar a regra de negócio em que ações como criar uma partida devem ficar restritas a administradores do grupo, então essa peça precisa existir antes de fazer sentido proteger as demais rotas.

## 5. Você está usando algum framework para escrever os endpoints da sua API? Se sim, qual?

**Spring Web** (Spring Boot), com controllers anotados com `@RestController` expondo os endpoints em JSON.

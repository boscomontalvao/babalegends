# Baba Legends

> Aplicação para organização do baba entre amigos.

## Resumo do Projeto

O Baba Legends é uma aplicação para grupos de amigos que jogam baba organizarem suas partidas de forma mais divertida e justa. Além de marcar os jogos e confirmar presença, o app sorteia os times de forma balanceada com base no nível de cada jogador, permite que o grupo vote nos destaques de cada partida e gera cartas de jogador no estilo de cards que evoluem conforme o histórico e o desempenho de cada um.

## Funcionalidades

- **Grupo de jogadores**: criação e gerenciamento de um grupo fixo de atletas
- **Agendamento de partidas**: definição de data, horário, local e número de vagas
- **Confirmação de presença**: jogadores confirmam ou recusam presença em cada partida
- **Sorteio de times balanceado**: distribuição automática dos jogadores presentes em times equilibrados, com base na nota/atributos de cada um
- **Votação pós-jogo**: votação do grupo ao final da partida em categorias como melhor jogador, artilheiro do dia, etc.
- **Cartas de jogador**: card de cada jogador com atributos que evoluem conforme estatísticas e votos recebidos
- **Histórico e estatísticas**: histórico de partidas e evolução individual de cada jogador.
- **Ranking do grupo**: classificação geral dos jogadores dentro do grupo

## Funcionalidades Futuras

- Chat do grupo para combinar detalhes da partida
- Divisão automática do valor da quadra entre os presentes
- Modo "convidado" para jogadores avulsos, fora do grupo fixo

## Arquitetura Proposta

A aplicação terá duas frentes com papéis complementares:

- **Web (painel do organizador)**: criação de partidas, gerenciamento do grupo, sorteio de times, acompanhamento de estatísticas
- **Mobile nativo (app do jogador)**: confirmação de presença, visualização da própria carta, votação pós-jogo, acompanhamento do ranking
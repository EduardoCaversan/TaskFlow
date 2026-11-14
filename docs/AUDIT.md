# Matriz da revisão final

Base inspecionada: `master` em `653a8b3` (também confirmada no remoto). Nenhuma alteração preexistente. Auditoria inicial em 11/11/2026; resultados de execução finais em [TESTING.md](TESTING.md).

| Requisito | Implementação na base | Ação / evidência a obter |
| --- | --- | --- |
| Java, XML, offline, API 24 | Presentes; sem permissão INTERNET | Preservar; compilar e executar na API 24 |
| Cadastro e trim | Form valida vazio e prioridade | Centralizar regras; bloquear reenvio; testar espaços e falhas |
| Listagem e ordenação | Executor; pendentes primeiro, ID decrescente | Testar ordem, retorno e contagem global |
| Filtros e estados vazios | Existem; filtro usa texto e não é salvo explicitamente | Salvar estado estável e testar recriação |
| Status pela lista | Listener após setChecked | Reverter visual em falha e bloquear operações concorrentes |
| Detalhes e edição | Busca por ID; carga assíncrona sobrescreve formulário recriado | Preservar rascunho, tratar ID ausente e callbacks após destruição |
| Exclusão | Confirmação existente | Mostrar erro sem fechar; verificar linhas afetadas |
| Room / SQLite | taskflow.db, versão 1, singleton com Application Context | Preservar schema/dados; exportar schema e testar CRUD |
| Erros do repository | Exceções ignoradas | Callback explícito de erro e log técnico |
| Temas | Claro/escuro/sistema em SharedPreferences e Application | Testar preferência e recriação; revisar contraste |
| Navegação / splash | Activities, finish, API de resultado antiga | Migrar resultado; revisar splash em background |
| UI e acessibilidade | Scroll nos formulários; cabeçalho fixo; alvos 40/44dp | Scroll completo, 48dp, fontes grandes e insets |
| Ícone / backup | Vetor próprio no manifest; adaptativos sem uso; backup padrão | Ligar ícones corretos e explicitar política local |
| Testes | Dois exemplos sem cobertura funcional | Unitários de regras, Room e Espresso |
| Build | Gradle 9.5.0, AGP 9.3.3, daemon Java 25; código Java 11 | Executar clean/test/lint/debug/release; corrigir README (JDK 11 incorreto) |
| Repositório | .idea gerada versionada | Retirar apenas do índice e ignorar artefatos |
| Documentação / entrega | README parcial | Arquitetura, testes, relatório, changelog e ZIP de fontes |

Todos os Java, layouts, resources XML, manifest, arquivos Gradle, wrapper, regras de backup e arquivos versionados da IDE foram inspecionados. A existência do código nesta tabela não constitui comprovação de execução. O relatório final distingue testes executados de inspeções e limitações.

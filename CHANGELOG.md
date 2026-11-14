# Changelog

## 1.0.0 — 2026-11-14

### Funcionalidades preservadas
- CRUD de tarefas com descrição, prioridade e status; alteração rápida pela lista e exclusão confirmada.
- Filtros Todas/Pendentes/Concluídas, contadores globais e estados vazios contextuais.
- Funcionamento offline com Room/SQLite, banco `taskflow.db`, mantendo o schema versão 1.
- Splash, identidade TaskFlow e temas claro, escuro e sistema com preferência persistida.

### Correções finais
- Erros de banco deixam de ser ignorados: log técnico, callback de falha e mensagem compreensível na tela.
- Verificação de linhas afetadas e inserção com conflito `ABORT` para evitar sucesso falso e substituição silenciosa.
- Validação compartilhada de descrição/prioridade e proteção contra reenvio durante gravação.
- Estado de operação preservado na recriação; rascunho de edição não é sobrescrito pela carga assíncrona.
- Filtro restaurado e callbacks antigos descartados; navegação migrada para Activity Result APIs.
- Tratamento de ID inexistente e reversão do CheckBox quando a gravação falha.

### Interface
- Conteúdo principal rolável, controles de 48dp, labels nos Spinners e descrições de acessibilidade contextuais.
- Texto longo abreviado na lista, com conteúdo integral nos detalhes.
- Melhor contraste, cores noturnas para prioridade/status, ícones próprios legados e adaptativos.
- Insets para Android recente e comportamento nativo de teclado nas versões anteriores.
- Política de dados locais explícita nas regras de backup.

### Testes, documentação e distribuição
- Substituição dos exemplos por 10 testes locais e 21 instrumentados: Room, repository, estado e Espresso.
- Schema Room exportado para acompanhamento de futuras mudanças.
- README, arquitetura, matriz de auditoria, guia de testes, relatório final e screenshots reais.
- `.idea` gerada retirada do versionamento; caches, APKs, chaves e ZIP de entrega ignorados.
- `versionName` 1.0.0 e `versionCode` 2; builds debug e release sem assinatura de distribuição.
- ZIP de código-fonte separado do APK; nenhuma apresentação ou gravação de vídeo incluída.

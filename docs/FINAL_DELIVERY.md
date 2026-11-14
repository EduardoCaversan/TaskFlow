# Relatório da entrega final — TaskFlow 1.0.0

**Aluno:** Eduardo Caversan da Silva Rocha

**R.A.:** 2475391

**Curso:** Engenharia de Software

**Instituição:** Universidade Tecnológica Federal do Paraná — UTFPR

**Câmpus:** Cornélio Procópio

**Disciplina:** Programação para Dispositivos Móveis

**Plataforma e linguagem:** Android nativo, Java, layouts XML

**Conclusão da revisão:** 14/11/2026

## Objetivo e escopo

Finalizar o aplicativo acadêmico mantendo as funcionalidades da `master` e corrigindo riscos de persistência, concorrência, ciclo de vida e usabilidade. A base foi `653a8b3`, merge do PR #2; a revisão usa `entrega-final`. Não houve reescrita do histórico, alteração de `entrega-2`/`entrega-3` ou merge automático.

O TaskFlow organiza tarefas com descrição, prioridade e conclusão, usando banco local e temas configuráveis. **Apresentação, slides, gravação de vídeo, câmera e narração não fazem parte desta revisão técnica.**

## Requisitos e evidências

| Área | Resultado |
| --- | --- |
| Java/XML, Android 7.0+ | Mantidos; minSdk 24, compileSdk/targetSdk 37; executado na API 24. |
| CRUD | Criação, leitura, edição e exclusão confirmada cobertas por Room e Espresso. |
| Validação | Descrição vazia/espaços rejeitada; trim e prioridade testados; reenvio bloqueado. |
| Listagem | Ordenação consistente, contadores globais, filtros e estados vazios preservados. |
| Status | Lista e detalhes atualizam o registro; teste nos dois sentidos. |
| Persistência | `taskflow.db`, schema 1 preservado; reabertura de arquivo e processo verificadas. |
| Erros | Sem catch silencioso; log técnico e retorno de falha; teste de exceções nas operações do repository. |
| Ciclo de vida | Filtro e rascunho restaurados; ViewModel conserva escrita durante recriação; callbacks protegidos. |
| Temas e identidade | Claro/escuro/sistema, preferência persistida, splash e ícones próprios mantidos. |
| Offline | Sem backend ou permissão de internet; todos os dados do app são locais. |
| Usabilidade | Rolagem, texto longo, fonte 200%, labels, alvos de toque e cores noturnas revisados. |
| Build | Debug e release compilados; versão 1.0.0, versionCode 2. |

A [matriz inicial](AUDIT.md) registra os problemas encontrados antes das mudanças. [TESTING.md](TESTING.md) distingue testes automatizados, inspeções visuais e limitações; não se presume funcionamento apenas pela existência de código.

## Correções e melhorias

- `TaskRepository` passou a entregar sucesso ou exceção em callback na main thread, registrando erros sem expor detalhes internos ao usuário.
- DAO informa linhas afetadas e usa `ABORT` na inserção; desaparecimento de registro não produz sucesso falso.
- Regras de descrição, prioridades, filtros e contagem foram centralizadas em `TaskRules`.
- `TaskEditorState` mantém uma escrita em curso na rotação e impede toques duplicados; campos permanecem disponíveis após falha.
- A edição não sobrescreve um rascunho restaurado com o resultado da consulta. ID ausente tem retorno explícito.
- Activity Result APIs substituem a navegação obsoleta, com feedback e proteção contra aberturas repetidas.
- A Main inteira pode rolar em telas baixas. CheckBoxes e botões voltar têm 48dp; Spinners possuem labels e altura flexível.
- Cores noturnas, contraste, ícones adaptativos/legados e regras de backup local foram corrigidos. O schema permaneceu inalterado.
- Arquivos gerados da IDE saíram apenas do índice Git; testes de exemplo foram removidos e substituídos por testes funcionais.

## Arquivos principais

O código fica em `app/src/main/java/com/eduardocaversan/taskflow/`: `Task`, `TaskDao`, `TaskDatabase`, `TaskRepository`, `TaskRules`, `TaskEditorState`, as quatro Activities, `ThemePreferences`, `TaskFlowApplication` e `ScreenInsets`.

Layouts e recursos ficam em `app/src/main/res`; o schema exportado fica em `app/schemas`. Os testes estão em `app/src/test` e `app/src/androidTest`. Configuração: `app/build.gradle.kts`, `gradle/libs.versions.toml` e wrapper Gradle.

Documentação entregue: `README.md`, `CHANGELOG.md`, `docs/ARCHITECTURE.md`, `docs/AUDIT.md`, `docs/TESTING.md`, este relatório e nove screenshots reais em `docs/screenshots/`.

## Validação final e artefatos

Todos os comandos abaixo terminaram com **código 0 / BUILD SUCCESSFUL** no Windows:

| Comando | Evidência |
| --- | --- |
| `.\gradlew.bat clean --console=plain` | Limpeza executada antes dos builds finais. |
| `.\gradlew.bat test --console=plain` | 10 testes locais, zero falhas/erros. |
| `.\gradlew.bat lint --console=plain` | Zero erros; oito avisos informativos avaliados. |
| `.\gradlew.bat assembleDebug --console=plain` | APK debug gerado. |
| `.\gradlew.bat assembleRelease --console=plain` | APK release sem assinatura de distribuição gerado. |
| `.\gradlew.bat connectedAndroidTest --console=plain` | 21 casos instrumentados aprovados na API 24, 412 × 915. |

Os 21 casos são: oito DAO, quatro repository, três estado de formulário e seis Espresso. Os seis fluxos de UI também passaram em 360 × 640. Capturas e inspeções adicionais cobriram claro/escuro, fonte 200%, horizontal e tamanho de tablet. Os testes verificaram teclado aberto, recriação e texto longo; encerramento/reabertura do processo preservou tarefa e tema escuro na verificação via ADB.

**APK debug:** `app/build/outputs/apk/debug/app-debug.apk`

**SHA-256:** `86ca9b28c642183015e15c90a70b3197e18c73d41a121f115d288704c5588c32`

**APK release:** `app/build/outputs/apk/release/app-release-unsigned.apk`

**SHA-256:** `e3fc5d72c538e0ba4db918be197960beb703cf9e081e8691f48119f4ca3de0af`

Para localizar o debug no Android Studio, abra a pasta `app/build/outputs/apk/debug` após `assembleDebug`. Instalação de desenvolvimento: `adb install -r app/build/outputs/apk/debug/app-debug.apk`. O release precisa de assinatura própria para distribuição; nenhuma chave privada foi incluída.

O pacote de fontes é `TaskFlow_Entrega_Final_Eduardo_Caversan_2475391.zip`, gerado a partir dos arquivos versionados. Contém wrapper, código e documentação; não contém `.git`, `.idea`, `.gradle`, `build`, SDK local, caches, credenciais ou APKs. ZIP e APKs ficam fora do Git. Logs e comprovantes auxiliares locais ficam em `artifacts/`, também ignorado.

## Commits da revisão

| Hash | Mensagem |
| --- | --- |
| `6cfda8e` | `chore: prepare final delivery branch and repository hygiene` |
| `b62009d` | `fix: harden task persistence and error handling` |
| `7a9de52` | `refactor: improve task validation and UI state handling` |
| `26564af` | `fix: improve accessibility and layout resilience` |
| `c6be0e2` | `test: replace placeholders with meaningful automated tests` |
| `7c7859c` | `chore: finalize version 1.0.0 delivery` |
| `31e2eaa` | `docs: complete final project documentation` |

A documentação é registrada depois dos commits de implementação, com a mensagem `docs: complete final project documentation`. Seu hash é obtido por `git log -1 --format="%h %s" -- docs/FINAL_DELIVERY.md`; o hash do próprio commit não pode ser escrito dentro dele sem mudá-lo. A relação completa e verificável é `git log --oneline master..HEAD`, também fornecida no relatório de conclusão da revisão.

## Limitações reais

- O emulador Android 37.1 permaneceu offline em duas tentativas. Não houve execução do app nele; a validação em runtime de API 35+ precisa de outro AVD/dispositivo funcional.
- Ícone adaptativo em launcher recente, splash do sistema Android 12+ e acompanhamento de mudança real do tema do sistema recente não foram validados visualmente. Código e recursos foram compilados.
- Não houve sessão completa com TalkBack nem testes em aparelho físico. A revisão de acessibilidade foi estrutural e visual.
- A lista carrega todos os registros; não é destinada a milhares de tarefas. Não há sincronização, exportação ou backup automático. Desinstalar/limpar dados remove o conteúdo local.
- Uma escrita é preservada durante recriação, mas não há garantia de continuar após morte do processo; é necessário conferir a lista antes de reenviar nesse caso.

## Checklist e próximos passos

- [x] Base correta e branches anteriores preservadas.
- [x] Java, XML, Room e recursos offline preservados.
- [x] Build debug/release, testes locais/instrumentados e lint executados.
- [x] CRUD, status, filtros, contadores, validação e tema cobertos pelas evidências descritas.
- [x] Ausência de testes de exemplo e de tratamento silencioso de erros.
- [x] Documentação, changelog, schema e screenshots reais preparados.
- [x] Commits incrementais e artefatos separados do código-fonte.
- [ ] Complementar testes visuais em Android recente e percurso completo com TalkBack.
- [ ] Aluno revisar a branch, publicá-la e abrir PR contra `master`, sem merge automático.

Revisão Git a reproduzir: `git diff master...HEAD`, `git log --oneline master..HEAD` e `git status`. O empacotamento usa os arquivos versionados, evitando incluir configurações locais. Apresentação e vídeo ficam exclusivamente a cargo do aluno, fora desta entrega técnica.

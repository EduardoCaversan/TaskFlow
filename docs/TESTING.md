# Testes e validação

## Ambiente e resultado

Revisão iniciada em 11/11/2026 e finalizada em 14/11/2026, em Windows 11 amd64, PowerShell, Gradle 9.5.0, AGP 9.3.3 e daemon Java 25. Código Java com compatibilidade 11, SDK 37 e minSdk 24. O wrapper foi inicialmente identificado com launcher Eclipse Adoptium 17.0.15; a JVM do daemon é definida pelo arquivo de toolchain.

Emulador funcional: `Medium_Phone_2`, Android 7.0/API 24, x86, serial `emulator-5554`. As configurações compacta, ampliada e tablet foram simuladas nesse mesmo dispositivo por resolução/densidade, não por aparelhos físicos distintos.

**Resultado final: 10 testes locais e 21 testes instrumentados aprovados, sem falhas ou testes ignorados. Lint: zero erros e oito avisos informativos avaliados.**

| Comando final na raiz | Código de saída | Resultado |
| --- | --- | --- |
| `.\gradlew.bat clean --console=plain` | 0 | BUILD SUCCESSFUL; limpeza concluída |
| `.\gradlew.bat test --console=plain` | 0 | BUILD SUCCESSFUL; 10 testes locais, 0 falhas, 0 erros |
| `.\gradlew.bat lint --console=plain` | 0 | BUILD SUCCESSFUL; 0 erros, 8 avisos |
| `.\gradlew.bat assembleDebug --console=plain` | 0 | BUILD SUCCESSFUL; APK debug 1.0.0, versionCode 2 |
| `.\gradlew.bat assembleRelease --console=plain` | 0 | BUILD SUCCESSFUL; APK release unsigned 1.0.0, versionCode 2 |
| `.\gradlew.bat connectedAndroidTest --console=plain` | 0 | BUILD SUCCESSFUL; 21/21 testes na API 24, 412 × 915, densidade 160 |

Os logs da máquina de entrega ficam em `artifacts/final-*.log` e os códigos de saída em `artifacts/final-validation.json` e `artifacts/final-connected-result.json`. São evidências locais ignoradas pelo Git; os relatórios Gradle são regenerados pelos comandos acima.

## Cobertura automatizada

| Classe | Casos | O que verifica |
| --- | --- | --- |
| `TaskRulesTest` | 10 locais | Null, vazio, espaços comuns/Unicode, trim sem remover espaços internos, prioridades aceitas/rejeitadas, três filtros, contadores, cópia sem mutar entrada, ID/status preservados, texto longo e validação na fronteira de persistência. |
| `TaskDaoTest` | 8 instrumentados | Inserção e campos completos, edição isolada, status nos dois sentidos, exclusão isolada, ordem, ID inexistente, conflito sem substituir dados e arquivo fechado/reaberto. |
| `TaskRepositoryTest` | 4 instrumentados | Normalização e retorno do ID, dados inválidos sem inserção, registros ausentes como falha, exceções simuladas em todas as operações e callbacks na main thread. |
| `TaskEditorStateTest` | 3 instrumentados | Duplo envio durante escrita e após sucesso, falha seguida de correção/nova tentativa, estado explícito de tarefa ausente. |
| `TaskFlowUiTest` | 6 Espresso | CRUD completo; prioridade e status; contadores globais; filtros e recriação; cancelar/confirmar exclusão; espaços e toque duplo; rascunho recriado; três temas e preferência; ID ausente; texto longo salvo com teclado aberto. |

Room in-memory é criado sem `allowMainThreadQueries`. O caso de persistência usa exclusivamente `taskflow-persistence-test.db`, removido ao terminar. Não foi necessária a dependência `room-testing`.

Espresso usa um `IdlingResource` que acompanha o executor e a entrega de callbacks do repository. O teste de teclado aguarda o IME visível; o de ID inexistente aguarda o evento de destruição da Activity. Não se substituem falhas por sleeps arbitrários nem se desabilitam asserções.

Os testes de UI identificam suas tarefas pelo prefixo único `UI_TEST_` e sua limpeza remove somente esses registros. Entretanto, o runner Gradle pode instalar/desinstalar o aplicativo: **execute instrumentação em emulador ou dispositivo dedicado a testes**, não em uma instalação com dados pessoais a preservar.

## Interface, navegação e evidências visuais

| Configuração / fluxo | Validação efetiva |
| --- | --- |
| 360 × 640, fonte padrão | Os seis testes Espresso passaram; relatório da rodada: `OK (6 tests)`. |
| 412 × 915, fonte padrão | Suíte final completa: 21 testes aprovados; capturas em claro e escuro. |
| 360 × 640, fonte 200% | Inspeção da lista, rolagem, abertura de descrição longa e salvamento nos detalhes. Controles inferiores alcançáveis por rolagem. |
| 640 × 360 | Inspeção em formato horizontal e rolagem até os cartões. |
| 800 × 1280 | Inspeção visual simulando tablet; dados e controles presentes. |
| Teclado aberto e texto longo | Teste Espresso específico aprovado, sem fechar o teclado antes de salvar. |
| Splash → Main | APK instalado via ADB e Activity lançada com `am start -W`; abertura observada. |
| Encerrar e reabrir processo | `am force-stop`, nova abertura da splash e verificação das tarefas e preferência escura preservadas. |
| Atualização do APK | Instalação `adb install -r` preservou tarefas já presentes antes da instrumentação. |
| Claro, escuro, sistema | Seleção, persistência e manutenção de filtro/dados verificadas no Espresso; claro/escuro também inspecionados visualmente. |

As imagens em [screenshots](screenshots/) são capturas reais via `adb exec-out screencap -p`, sem composição artificial. Os nomes descrevem o tamanho lógico configurado; o framebuffer do emulador pode manter resolução física e faixas pretas. Cortes em capturas roladas correspondem à posição da rolagem, não à perda do conteúdo.

Labels, hints, alvos de toque, descrições dos CheckBoxes e informações textuais além das cores foram revisados em XML e na hierarquia da interface. Não foi realizada uma sessão completa com TalkBack nem certificação de acessibilidade. Não se afirma validação em todo aparelho Android ou em todas as combinações de fonte/orientação.

## Avisos do lint

| Aviso | Quantidade | Decisão |
| --- | --- | --- |
| `AndroidGradlePluginVersion` | 1 | Há AGP mais recente; mantida a versão da base, compilada com sucesso. |
| `GradleDependency` | 6 | Há versões mais recentes de AppCompat, Material, Room e bibliotecas de teste. Atualização ampla não é necessária para corrigir os fluxos desta entrega. |
| `CustomSplashScreen` | 1 | Splash própria mantida por requisito acadêmico. Android 12+ também exibe a inicialização do sistema. |

Não há supressão global, baseline para esconder erros ou validação desligada. Foram corrigidos recursos sem uso, texto fixo de contadores e desenho redundante apontados inicialmente. As animações do dispositivo foram temporariamente desativadas para instrumentação; isso não desabilita validações do app.

## Falhas encontradas durante o desenvolvimento

A primeira compilação dos novos testes apontou uso inadequado de try-with-resources com Room, imports ambíguos do Espresso e inferência genérica; todos corrigidos. A primeira rodada de 17 instrumentados teve três falhas de UI relacionadas ao teclado e à espera de encerramento. Após ajuste de comportamento nativo da janela na API 24 e sincronização, os 17 passaram. A suíte ampliada de 20 passou; o caso adicional de teclado precisou aguardar a abertura do IME e também passou. A rodada final reuniu os 21 casos e passou integralmente.

Essa sequência é registrada para não confundir tentativas de desenvolvimento com os resultados finais.

## Limitações de ambiente e como complementar

Foi tentado um emulador temporário Android 37.1 com a imagem x86_64 de páginas de 16 KB já instalada. Ele permaneceu `offline`, inclusive após reinício com logs de kernel; não foi possível instalar ou testar o app nele. Os logs locais `artifacts/emulator37*.log` registram a tentativa. Não se atribui esse problema ao aplicativo, pois ele não chegou a ser executado nesse emulador.

Assim, o tratamento de insets para API 35+ foi implementado e compilado, mas a validação visual em Android recente, ícones adaptativos em launcher recente e comportamento da tela de inicialização do sistema permanecem pendentes de aparelho/emulador funcional. Também falta verificar a mudança real do tema do sistema em um Android recente; a seleção e a preferência `system` foram testadas.

Para complementar no Android Studio: sincronize o projeto, crie um AVD estável API 35 ou superior no Device Manager, execute `app`, repita CRUD/teclado/rotação e altere o tema do sistema com a opção Sistema selecionada. Depois execute o pacote `app/src/androidTest` pelo menu Run ou use o wrapper. Para TalkBack, habilite-o nas configurações de acessibilidade do dispositivo de teste e percorra cadastro, detalhes, filtros e confirmação de exclusão.

## Como repetir

No Windows, execute os comandos da tabela inicial. Para selecionar um dispositivo entre vários:

```powershell
$env:ANDROID_SERIAL = 'emulator-5554'
.\gradlew.bat connectedAndroidTest
.\gradlew.bat connectedAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.eduardocaversan.taskflow.TaskFlowUiTest'
```

No Linux/macOS, use `chmod +x gradlew` e substitua `.\gradlew.bat` por `./gradlew`. No Android Studio, execute testes locais em `app/src/test` e instrumentados em `app/src/androidTest` com um dispositivo selecionado.

Relatórios: `app/build/reports/tests/testDebugUnitTest/index.html`, `app/build/reports/lint-results-debug.html` e `app/build/reports/androidTests/connected/debug/index.html`. O comando `clean` remove relatórios anteriores; execute a sequência novamente para regenerá-los.

# TaskFlow

Aplicativo Android nativo para organização simples e eficiente de tarefas pessoais.

![Android](https://img.shields.io/badge/Android-Native-3DDC84?logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-11-ED8B00?logo=openjdk&logoColor=white)
![Room](https://img.shields.io/badge/Room-2.7.2-3DDC84)
![Material Design](https://img.shields.io/badge/Material%20Design-3-757575?logo=materialdesign&logoColor=white)

## Sobre o projeto

O TaskFlow é um aplicativo Android nativo voltado à organização de tarefas pessoais. Foi desenvolvido com foco em simplicidade, produtividade, funcionamento offline, persistência local e uma experiência de uso moderna.

O TaskFlow foi desenvolvido durante a disciplina de Programação para Dispositivos Móveis do curso de Engenharia de Software da Universidade Tecnológica Federal do Paraná (UTFPR), Câmpus Cornélio Procópio.

## ✨ Funcionalidades

- Criação de tarefas com descrição e prioridade (baixa, média ou alta).
- Listagem de tarefas com contadores de pendentes e concluídas.
- Filtros para visualizar todas, pendentes ou concluídas.
- Marcação rápida de tarefa como concluída ou pendente pela lista.
- Edição de descrição, prioridade e situação da tarefa.
- Exclusão com confirmação em diálogo.
- Validação de descrição obrigatória e de prioridade válida.
- Estados vazios contextualizados conforme o filtro selecionado.
- Feedback após criar, editar ou excluir tarefas por meio de `Snackbar`.
- Tela de abertura (splash screen) e suporte a tema claro/escuro.
- Armazenamento local, sem dependência de conexão com a internet para as funcionalidades principais.

## Interface

A interface segue Material Design 3 e utiliza uma identidade visual própria, com paleta de cores para temas claro e escuro, logo vetorial do TaskFlow e componentes Material. A tela principal reúne um cartão de progresso, chips de filtro, cards de tarefa com indicadores de prioridade e status, e um botão de ação flutuante estendido para criar tarefas.

Os formulários usam campos de texto contornados, seletores para prioridade e situação, botões Material e diálogo de confirmação para exclusão.

## Screenshots

<!-- Screenshots da interface podem ser adicionadas aqui futuramente. -->

## 🛠️ Tecnologias

| Tecnologia | Papel no projeto |
| --- | --- |
| Java 11 | Implementação das telas, regras de interação e camada de dados. |
| Android SDK | Plataforma para desenvolvimento do aplicativo nativo. |
| XML | Definição de layouts, temas, cores, strings e demais recursos. |
| Material Components / Material Design 3 | Componentes visuais, tema, cards, chips, campos, botões, diálogos e `Snackbar`. |
| Room 2.7.2 | Persistência local e abstração de acesso ao SQLite. |
| SQLite | Banco de dados local utilizado internamente pelo Room. |
| Gradle | Automação de compilação e gerenciamento das dependências. |

O projeto utiliza `minSdk 24`, `targetSdk 37` e `compileSdk 37`.

## 💾 Persistência de dados

Os dados são mantidos localmente no banco `taskflow.db`. O Room atua como camada de abstração sobre o SQLite:

- `Task` é a entidade da tabela `tasks`.
- `TaskDao` define inserção, consulta, atualização de tarefa/status e exclusão.
- `TaskDatabase` configura o `RoomDatabase` e disponibiliza o DAO.
- `TaskRepository` centraliza as operações e as executa fora da thread de interface, retornando o resultado à tela principal.

Assim, as tarefas permanecem armazenadas no dispositivo entre usos do aplicativo.

## 🗂️ Estrutura do projeto

```text
TaskFlow/
├── app/
│   ├── src/main/
│   │   ├── java/com/eduardocaversan/taskflow/
│   │   │   ├── MainActivity.java
│   │   │   ├── SplashActivity.java
│   │   │   ├── TaskFormActivity.java
│   │   │   ├── TaskDetailActivity.java
│   │   │   ├── Task.java
│   │   │   ├── TaskDao.java
│   │   │   ├── TaskDatabase.java
│   │   │   └── TaskRepository.java
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── drawable/
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

`MainActivity` exibe e filtra as tarefas; `TaskFormActivity` realiza o cadastro; `TaskDetailActivity` permite editar, alterar a situação e excluir; e `SplashActivity` apresenta a tela inicial. A camada formada por `Task`, `TaskDao`, `TaskDatabase` e `TaskRepository` organiza o acesso persistente aos dados.

## 🔄 CRUD

- **Create:** `TaskFormActivity` cria tarefas por `TaskRepository.add()`.
- **Read:** `MainActivity` obtém a lista com `TaskRepository.getAll()` e `TaskDao.getAll()`.
- **Update:** `TaskDetailActivity` atualiza descrição, prioridade e situação; a lista também altera rapidamente o status com `updateStatus()`.
- **Delete:** `TaskDetailActivity` remove a tarefa confirmada pelo usuário via `TaskRepository.delete()`.

## ▶️ Como executar

**Requisitos:** Android Studio, JDK 11 e um emulador ou dispositivo Android com API 24 (Android 7.0) ou superior.

1. Clone o repositório:

   ```bash
   git clone https://github.com/EduardoCaversan/TaskFlow.git
   ```

2. Abra a pasta do projeto no Android Studio.
3. Aguarde ou execute a sincronização do Gradle.
4. Selecione um emulador ou conecte um dispositivo físico compatível.
5. Execute o módulo `app`.

## 📱 Funcionamento offline

As tarefas são armazenadas localmente com Room/SQLite. Por isso, criação, consulta, edição, alteração de status, exclusão e filtros funcionam sem conexão com a internet.

## 🎓 Contexto acadêmico

Este é um projeto acadêmico desenvolvido durante a disciplina de **Programação para Dispositivos Móveis**, do curso de **Engenharia de Software**, da **Universidade Tecnológica Federal do Paraná (UTFPR) — Câmpus Cornélio Procópio**.

**Aluno:** Eduardo Caversan da Silva Rocha  
**R.A.:** 2475391

O projeto aplica, na prática, desenvolvimento Android nativo, construção de interfaces, navegação entre telas, persistência local, banco de dados e operações CRUD.

## 🧠 Conceitos aplicados

- Desenvolvimento Android nativo com Java e layouts XML.
- `Activities` e navegação entre telas com `Intent`.
- Material Design 3, temas claro/escuro e gerenciamento de resources.
- Persistência local com Room, SQLite, `Entity`, `DAO` e `RoomDatabase`.
- Operações CRUD e execução de acesso ao banco fora da thread de interface.
- Validação de formulário, filtros por status e feedback visual ao usuário.

## 👤 Autor

**Eduardo Caversan da Silva Rocha**  
R.A.: 2475391  
Engenharia de Software — UTFPR, Câmpus Cornélio Procópio  
[GitHub: EduardoCaversan](https://github.com/EduardoCaversan)

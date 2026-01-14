# 🏛️ Aureus

![Status](https://img.shields.io/badge/Status-Em_Andamento-yellow?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

---

## 📄 Sobre o Projeto

**Aureus** é uma plataforma Fullstack para gestão de finanças pessoais. O nome remete à moeda de ouro da Roma Antiga, simbolizando valor e estabilidade.

O objetivo principal do **Aureus** é ser um portfólio das minhas habilidades como desenvolvedor. Para este projeto, escolhi o tema finanças pessoais por ser um assunto que tenho certa experiência e sei o que dá certo ou errado, tendo em vista que já faço um controle fino das minhas finanças por conta própria. Isso me permitiu ter regras de negócio reais para o projeto, em vez de criar cenários hipotéticos e criar implementações aleatórias.

Visando mostrar todas as minhas habilidades, vou tentar utilizar tudo o que já aprendi de tecnologia e que for pertinente para o projeto. Algumas implementações, como Kubernetes, podem soar como over engineering, mas estarão implementadas no futuro meramente para exemplificar minhas habilidades, e não como necessidade arquitetural.

### 🎯 Objetivo

Construir um portfólio de suma autoria, aplicando boas práticas como:
- **Design First:** Modelagem de dados e wireframes antes do código.
- **API First:** Contratos definidos (Swagger/OpenAPI) e testáveis.
- **DevOps:** Containerização, orquestração e CI/CD.

---

## 📚 Central de Documentação

Este repositório atua como o **Hub Central** do projeto, adotando um conceito conhecido como **Single Source of Truth**, em português **Fonte Única da Verdade**. Aqui você encontra toda a documentação técnica, decisões arquiteturais e o plano de execução.

### Navegue pelos Componentes da Documentação

| Disponível | Componente (Hiperlink) | Descrição | O que você vai encontrar |
| :--- | :--- | :--- | :--- |
| 🔴 | [![GitHub](https://img.shields.io/badge/🚧_(Em_breve)-black?style=for-the-badge)](https://github.com/gPagio/projeto-aureus-docs) | **Gestão do Projeto** | Plano de execução, requisitos. |
| 🟢 | **[📂 /database](./database)** | **Modelagem de Dados** | O DER (Diagrama Entidade-Relacionamento). |
| 🟢 | **[📂 /api](./api)** | **Especificação Backend** | Diagramas de Classe (Domínio Java), Swagger e fluxos. |
| 🔴 | [![GitHub](https://img.shields.io/badge/🚧_(Em_breve)-black?style=for-the-badge)](https://github.com/gPagio/projeto-aureus-docs) | **Coleções Postman** | Coleções do Postman que mostram o comportamento da aplicação por meio de seus endpoints. |
| 🔴 | [![GitHub](https://img.shields.io/badge/🚧_(Em_breve)-black?style=for-the-badge)](https://github.com/gPagio/projeto-aureus-docs) | **Design Frontend** | Wireframes, Guias de Estilo e protótipos de tela. |

---

## 🏗️ Módulos do Projeto (Repositórios)

O projeto **Aureus** adota uma estratégia **Multi-Repo** para separar responsabilidades.

| Disponível | Módulo | Stack Tecnológica | Repositório (Hiperlink) |
| :--- | :--- | :--- | :--- |
| 🟢 | **Docs (Este)** | Markdown, Mermaid, Postman | *Você está aqui* |
| 🔴 | **Backend API** | Java 21, Spring Boot 4 | [![GitHub](https://img.shields.io/badge/GitHub-🚧_(Em_breve)-black?style=for-the-badge&logo=github)](https://github.com/gPagio/projeto-aureus-docs)  |
| 🔴 | **Frontend Web** | Vite, React 19, TailwindCSS | [![GitHub](https://img.shields.io/badge/GitHub-🚧_(Em_breve)-black?style=for-the-badge&logo=github)](https://github.com/gPagio/projeto-aureus-docs) |

---

## 🚦 Status e Roadmap: Acompanhamento do Projeto

O status de cada módulo comunica a situação geral dos mesmos. O objetivo é basicamente informar de forma rápida como está o desenvolvimento de cada módulo com uma simples badge de status no início do **README** principal de cada módulo, com determinada legenda e cor.

Já nos projetos do **GitHub Projects** estão todas as tarefas de cada módulo de forma detalhada. Assim como o status, cada módulo tem seu projeto no **GitHub Projects**, aninhando todas as etapas de desenvolvimento em seus respectivos estados atuais.

### 🏷️ Badges de Status: Legendas e Cores

Para facilitar a visualização do status de cada módulo do projeto **Aureus**, utilizamos a seguinte convenção de cores semânticas nas badges:

| Cor | Exemplo Visual | Significado e Uso |
| :--- | :--- | :--- |
| **Verde** | ![Verde](https://img.shields.io/badge/Verde-Verde-success?style=for-the-badge) | Concluído |
| **Amarelo** | ![Amarelo](https://img.shields.io/badge/Amarelo-Amarelo-yellow?style=for-the-badge) | Em Andamento |
| **Vermelho** | ![Vermelho](https://img.shields.io/badge/Vermelho-Vermelho-red?style=for-the-badge) | Parado |
| **Cinza** | ![Cinza](https://img.shields.io/badge/Cinza-Cinza-lightgrey?style=for-the-badge) | Arquivado |

> [!CAUTION]
> Esta convenção de cores aplica-se exclusivamente às badges de status do projeto. Outras badges do repositório podem não seguir esta mesma paleta.

### 🖼️ Board de Tarefas: O Roadmap

O roadmap do projeto, que permite realizar o acompanhamento detalhado das tarefas, especificações e progresso de cada módulo do projeto é gerenciado através do **GitHub Projects**.

Cada módulo terá seu projeto no **GitHub Projects** para acompanhamento e gerenciamento próprio e uma badge no **README** principal que leva ao projeto.

Para acessar o quadro do **GitHub Projects** e ver o que está sendo trabalhado em tempo real em cada módulo do projeto **Aureus**, clique em badges semelhantes a badge abaixo.

[![Acessar Board de Documentação](https://img.shields.io/badge/Roadmap_Board_GitHub_Projects-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/users/gPagio/projects/2)

---

## 🛠️ Stack Tecnológica Essencial Planejada

| Contexto | Tecnologias |
| :--- | :--- |
| **Back-end** | ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) |
| **Front-end** | ![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white) ![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB) ![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white) |
| **Banco de Dados** | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white) |
| **Tools & DevOps** | ![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white) ![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Kubernetes](https://img.shields.io/badge/kubernetes-%23326ce5.svg?style=for-the-badge&logo=kubernetes&logoColor=white) |
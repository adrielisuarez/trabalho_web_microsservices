# Trabalho Web — Microsserviços (Etapa 4)

Sistema de autenticação por e-mail (login sem senha, via código de verificação),
cadastro de nome/cargo e dashboard protegido por JWT, construído com uma
arquitetura de microsserviços.

## Arquitetura

O projeto é composto por **três serviços independentes**:

```
┌──────────────┐        HTTP        ┌──────────────┐
│  Frontend    │ ──────────────────▶│ User Service │
│  (Node/      │   /auth/*          │ (Spring Boot)│
│  Express)    │   /users/*         │  porta 8081  │
│  porta 3000  │◀────────────────── │              │
└──────────────┘     JWT/JSON       └──────┬───────┘
                                            │ RabbitMQ
                                            │ (CloudAMQP)
                                            ▼
                                     ┌──────────────┐
                                     │ Email Service│
                                     │ (Spring Boot)│
                                     │  porta 8082  │
                                     └──────┬───────┘
                                            │ SMTP
                                            ▼
                                       Gmail (envio
                                       do código)
```

- **Frontend** (`/frontend`): servidor Node.js/Express que serve as páginas
  HTML (login por e-mail, verificação de código, cadastro de nome/cargo e
  dashboard) e atua como proxy entre o navegador e o User Service.
- **User Service** (`/user-service`): API REST em Spring Boot responsável por
  cadastro/login de usuários, geração e validação de código de verificação,
  emissão de JWT, atualização de perfil (nome/cargo) e endpoints protegidos
  por role (`ROLE_CUSTOMER` / `ROLE_ADMINISTRATOR`). Persiste no banco
  `ms_user` (MySQL) e publica eventos de "enviar código" no RabbitMQ.
- **Email Service** (`/email-service`): consome a fila do RabbitMQ e envia o
  e-mail com o código de verificação via SMTP (Gmail). Persiste no banco
  `ms_email` (MySQL).

### Fluxo de autenticação

1. Usuário informa o e-mail na tela inicial (`/`).
2. Frontend chama `POST /auth/request-code` no User Service.
3. User Service gera o código, salva em cache e publica uma mensagem no
   RabbitMQ.
4. Email Service consome a mensagem e envia o código por e-mail.
5. Usuário digita o código em `/verify` → Frontend chama
   `POST /auth/verify-code` → User Service valida e retorna um **JWT**.
6. Frontend guarda o token em `sessionStorage` e leva o usuário para
   `/register`, onde ele informa **nome** e **cargo**
   (`ROLE_CUSTOMER`/`ROLE_ADMINISTRATOR`).
7. O formulário chama `POST /users/update-profile` (com o JWT no header
   `Authorization`), que atualiza o usuário no banco.
8. Usuário é redirecionado para `/dashboard`, onde pode:
   - Testar um endpoint protegido (`GET /api/protected` → `GET /users/test/customer`)
   - Ver seu perfil (`GET /api/me` → `GET /users/me`)
   - Sair (limpa o token e volta para `/`)

## Pré-requisitos

- **JDK 17+**
- **Maven** (o projeto já inclui o Maven Wrapper, `mvnw`/`mvnw.cmd`)
- **Node.js 18+** e **npm**
- **MySQL** (local ou remoto), com dois bancos criados: `ms_user` e `ms_email`
- Uma conta no **CloudAMQP** (RabbitMQ como serviço) — usada para a fila de
  envio de e-mails
- Uma conta **Gmail** com **senha de app** (App Password) habilitada para
  envio via SMTP — não funciona com a senha normal da conta

## Configuração

> ⚠️ **Segurança**: os arquivos `application.properties` deste repositório
> contêm exemplos de configuração. **Nunca** deixe senhas reais de banco,
> credenciais do CloudAMQP ou senha de app do Gmail commitadas em texto
> puro no repositório. Prefira usar variáveis de ambiente, como mostrado
> abaixo, e adicione `application-local.properties` ao `.gitignore` se for
> usar esse tipo de arquivo.

### 1. Banco de dados

Crie os dois bancos no MySQL:

```sql
CREATE DATABASE ms_user;
CREATE DATABASE ms_email;
```

As tabelas são criadas automaticamente pelo Hibernate
(`spring.jpa.hibernate.ddl-auto=update`).

### 2. Variáveis de ambiente

Cada serviço Spring Boot lê configuração de `application.properties`, mas
você pode (e deve) sobrescrever os valores sensíveis por variáveis de
ambiente do sistema operacional antes de iniciar cada serviço:

**User Service** (`user-service/src/main/resources/application.properties`):

| Variável                     | Descrição                                  |
|-------------------------------|---------------------------------------------|
| `SPRING_DATASOURCE_URL`       | URL JDBC do banco `ms_user`                  |
| `SPRING_DATASOURCE_USERNAME`  | Usuário do MySQL                             |
| `SPRING_DATASOURCE_PASSWORD`  | Senha do MySQL                               |
| `SPRING_RABBITMQ_ADDRESSES`   | URL `amqps://...` fornecida pelo CloudAMQP    |
| `BROKER_QUEUE_EMAIL_NAME`     | Nome da fila usada para enviar o código      |

**Email Service** (`email-service/src/main/resources/application.properties`):

| Variável                     | Descrição                                   |
|-------------------------------|----------------------------------------------|
| `SPRING_DATASOURCE_URL`       | URL JDBC do banco `ms_email`                  |
| `SPRING_DATASOURCE_USERNAME`  | Usuário do MySQL                              |
| `SPRING_DATASOURCE_PASSWORD`  | Senha do MySQL                                |
| `SPRING_MAIL_USERNAME`        | E-mail do Gmail que envia os códigos          |
| `SPRING_MAIL_PASSWORD`        | Senha de app do Gmail (App Password)          |
| `SPRING_RABBITMQ_ADDRESSES`   | Mesma URL do CloudAMQP usada no User Service  |
| `BROKER_QUEUE_EMAIL_NAME`     | Mesmo nome de fila usado no User Service      |

No Windows (PowerShell), por exemplo:

```powershell
$env:SPRING_DATASOURCE_PASSWORD = "sua_senha_mysql"
$env:SPRING_MAIL_PASSWORD = "sua_senha_de_app_gmail"
```

**Frontend** (`frontend/server.js`): por padrão aponta para
`http://localhost:8081` (User Service) e sobe na porta `3000`. Ajuste essas
constantes no `server.js` caso rode os serviços em outras portas/hosts.

## Como executar

### 1. Instalar dependências do frontend

```bash
cd frontend
npm install
```

### 2. Subir o User Service (porta 8081)

```bash
cd user-service
./mvnw spring-boot:run        # Linux/Mac
mvnw.cmd spring-boot:run      # Windows
```

### 3. Subir o Email Service (porta 8082)

```bash
cd email-service
./mvnw spring-boot:run        # Linux/Mac
mvnw.cmd spring-boot:run      # Windows
```

### 4. Subir o Frontend (porta 3000)

```bash
cd frontend
npm start
```

### 5. Ou usar o script de inicialização (Windows)

Na raiz do projeto:

```powershell
.\iniciar.ps1
```

Esse script abre um terminal separado para cada um dos três serviços
(User Service, Email Service e Frontend).

## Testando o fluxo completo

1. Acesse `http://localhost:3000`.
2. Digite um e-mail válido e clique em enviar → você receberá um código
   real no e-mail informado (verifique a caixa de spam também).
3. Digite o código recebido → você será levado para a tela de cadastro.
4. Preencha seu nome e escolha o cargo (`ROLE_CUSTOMER` ou
   `ROLE_ADMINISTRATOR`) → você será redirecionado para o dashboard.
5. No dashboard:
   - Clique em **"Testar endpoint protegido"** e confira que retorna sucesso.
   - Clique em **"Meu perfil"** e confira que o nome/cargo cadastrados
     aparecem corretamente.
   - Clique em **"Sair"** para encerrar a sessão.

### Capturas de tela

> Adicione aqui screenshots do fluxo completo (tela de e-mail, código,
> cadastro, dashboard e resultado dos endpoints protegidos) antes da
> entrega final. Sugestão de pasta: `docs/screenshots/`.

```
docs/screenshots/01-login-email.png
docs/screenshots/02-verificacao-codigo.png
docs/screenshots/03-cadastro-nome-cargo.png
docs/screenshots/04-dashboard.png
docs/screenshots/05-endpoint-protegido.png
```

## Estrutura do repositório

```
.
├── user-service/      # API REST principal: usuários, JWT, perfil, roles
├── email-service/     # Serviço de envio de e-mail (consumidor RabbitMQ)
├── frontend/           # Servidor Node/Express + páginas HTML
├── iniciar.ps1         # Script para subir os 3 serviços (Windows)
├── pom.xml             # POM pai (build multi-módulo Maven)
└── .gitignore
```

## Principais endpoints (User Service)

| Método | Rota                       | Acesso        | Descrição                                  |
|--------|----------------------------|---------------|---------------------------------------------|
| POST   | `/auth/request-code`       | Público       | Solicita código de verificação por e-mail   |
| POST   | `/auth/verify-code`        | Público       | Valida código e retorna o token JWT         |
| POST   | `/users`                   | Público       | Cria um novo usuário                        |
| POST   | `/users/login`             | Público       | Login com e-mail/senha (fluxo alternativo)  |
| POST   | `/users/update-profile`    | Autenticado   | Atualiza nome e cargo do usuário logado     |
| GET    | `/users/me`                | Autenticado   | Retorna o perfil do usuário logado          |
| GET    | `/users/test/customer`     | `ROLE_CUSTOMER`       | Endpoint de teste para customers   |
| GET    | `/users/test/administrator`| `ROLE_ADMINISTRATOR`  | Endpoint de teste para admins      |

## Tecnologias

- **Backend**: Java 17, Spring Boot, Spring Security, Spring Data JPA,
  Spring AMQP (RabbitMQ), JWT, BCrypt, MySQL
- **Mensageria**: RabbitMQ (CloudAMQP)
- **E-mail**: Spring Mail (SMTP/Gmail)
- **Frontend**: Node.js, Express, Axios, HTML/CSS/JS puro

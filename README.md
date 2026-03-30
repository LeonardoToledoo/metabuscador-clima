# MetaClima

Metabuscador de clima para projeto acadêmico.  
Consulta duas fontes meteorológicas (Open-Meteo e WeatherAPI), normaliza os dados, consolida resultados e exibe comparação em interface web limpa e responsiva.

## 1) Arquitetura do projeto

O backend segue estilo **Clean Architecture** com camadas bem definidas:

- **`metabuscadorclima-api/src/main/java/domain`**: núcleo da aplicação, DTOs de domínio, exceções e portas
- **`metabuscadorclima-api/src/main/java/application`**: casos de uso, configurações e serviços de orquestração
- **`metabuscadorclima-api/src/main/java/adapter/entrypoint`**: pontos de entrada HTTP (controllers)
- **`metabuscadorclima-api/src/main/java/adapter/outbound`**: integrações concretas com APIs externas, mapeadores e provedores

### Padrão de pacotes de controller

- **Pacote**: `adapter.entrypoint.controller.{module}.{feature}`
- **Nomenclatura**: `{Operation}{Entity}Controller`
- Exemplos implementados:
  - `SearchWeatherController`
  - `SearchLocationController`
  - `GetHealthController`

### Classes de apoio obrigatórias (implementadas)

- `SearchWeatherInput` (input do use case)
- `SearchWeatherOutput` (output do use case)
- `SearchLocationInput` (input do use case)
- `SearchLocationOutput` (output do use case)

### Fluxo obrigatório (implementado)

`HTTP Request → Controller → UseCase → Port → Provider → Response`

No endpoint principal:
1. Controller recebe requisição REST.
2. Controller cria `SearchWeatherInput`.
3. Controller chama `useCase.executar(inputData)`.
4. UseCase chama `WeatherSearchPort`.
5. Provider integra Open-Meteo + WeatherAPI, normaliza e consolida.
6. Controller retorna `SearchWeatherOutput`.

## 2) Estrutura de pastas

```text
.
├── metabuscadorclima-api
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   ├── adapter
│       │   │   │   ├── entrypoint/controller
│       │   │   │   └── outbound
│       │   │   │       ├── client
│       │   │   │       ├── mapper
│       │   │   │       └── provider
│       │   │   ├── application
│       │   │   │   ├── config
│       │   │   │   ├── service
│       │   │   │   └── usecase
│       │   │   └── domain
│       │   │       ├── dto
│       │   │       ├── exception
│       │   │       └── port
│       │   └── resources/application.yml
│       └── test/java
│           ├── controller
│           ├── mapper
│           └── service
├── metabuscadorclima-app
│   ├── Dockerfile
│   ├── index.html
│   ├── nginx.conf
│   ├── assets
│   ├── css
│   │   ├── style.css
│   │   └── responsive.css
│   └── js
│       ├── app.js
│       ├── api.js
│       ├── config.js
│       ├── search-select.js
│       ├── ui.js
│       └── utils.js
├── docker-compose.yml
└── .env.example
```

## 3) Back-end (Spring Boot)

### Stack
- Java 21
- Spring Boot 3
- Maven
- API REST
- RestClient
- Springdoc OpenAPI (Swagger)

### Endpoints
- `GET /api/health`
- `GET /api/weather/search?city=Campo Grande`
- `GET /api/locations/countries?q=bra`
- `GET /api/locations/states?country=Brasil&q=mat`
- `GET /api/locations/cities?country=Brasil&state=Mato%20Grosso%20do%20Sul&q=cam`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### Principais classes do back-end
- `application/MetaClimaApplication`
- `application/usecase/weather/search/SearchWeatherUseCase`
- `application/usecase/location/search/SearchLocationUseCase`
- `domain/port/WeatherSearchPort`
- `domain/port/LocationSearchPort`
- `adapter/outbound/provider/ExternalWeatherSearchProvider`
- `adapter/outbound/provider/ExternalLocationSearchProvider`

### Formato de resposta do endpoint principal
- `query`
- `location`
- `sources`
- `consolidated`
- `forecast`

### Regras de consolidação implementadas
- Temperatura, sensação térmica, umidade, vento, máxima e mínima: média simples quando há duas fontes.
- Se apenas uma fonte retornar campo válido, usa o valor disponível.
- Fontes contribuídas ficam explícitas em `consolidated.sourcesUsed`.
- Falha parcial: se uma API falhar, a outra continua sendo exibida.

### Tratamento de erros
- Cidade não encontrada: `404`
- Erro de comunicação com fontes externas: `502`
- Requisição inválida: `400`

## 4) Front-end (HTML/CSS/JS puro)

Observação: o front legado foi unificado e agora há somente `metabuscadorclima-app/index.html`.

### Características
- Home estilo landing page acadêmica (clean, centralizada, azul + cinza claro).
- Busca guiada por `País -> Estado -> Cidade` com autocomplete.
- Chips de cidades populares com busca automática.
- Estados completos:
  - Inicial
  - Loading (spinner + skeleton)
  - Resultado (consolidado + fontes + previsão)
  - Erro amigável com retry
- Responsivo para desktop/tablet/mobile.
- Acessibilidade básica:
  - HTML semântico
  - `label` oculto para busca
  - foco visível em inputs e botões
  - mensagens claras

### Configuração da URL da API no front
Arquivos: `metabuscadorclima-app/js/config.js` e `metabuscadorclima-app/js/api.js`
```js
// config.js
window.METACLIMA_API_BASE_URL = window.METACLIMA_API_BASE_URL || "/api";

// api.js
const API_CONFIG = {
  BASE_URL: window.METACLIMA_API_BASE_URL || "/api",
  TIMEOUT_MS: 12000
};
```

## 5) Como rodar localmente

### Pré-requisitos
- Java 21
- Maven 3.9+
- Navegador moderno

### Back-end
```bash
cd metabuscadorclima-api
export WEATHERAPI_KEY=sua_chave_weatherapi
export CSCAPI_KEY=sua_chave_countrystatecity
export SERVER_PORT=8080
mvn spring-boot:run
```

### Front-end
Use qualquer servidor estático. Exemplo:
```bash
cd metabuscadorclima-app
python3 -m http.server 8081
```
Se usar servidor estático simples (sem proxy), ajuste `metabuscadorclima-app/js/config.js` para:
```js
window.METACLIMA_API_BASE_URL = "http://localhost:8080/api";
```
Ao voltar para Docker Compose, restaure para:
```js
window.METACLIMA_API_BASE_URL = window.METACLIMA_API_BASE_URL || "/api";
```
Depois abra:
- `http://localhost:8081`

## 6) Rodando com Docker

No Docker, o frontend já está integrado ao backend por proxy Nginx em `/api` (mesmo host do frontend).

```bash
cp .env.example .env
# edite WEATHERAPI_KEY e CSCAPI_KEY no .env
docker compose up --build
```

URLs:
- Front-end: `http://localhost:8081`
- Back-end: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

## 7) Exemplo de requisição

```http
GET /api/weather/search?city=Campo%20Grande
```

```http
GET /api/locations/countries?q=bra
```

## 8) Exemplo de resposta JSON

```json
{
  "query": "Campo Grande",
  "location": {
    "name": "Campo Grande",
    "region": "Mato Grosso do Sul",
    "country": "Brazil",
    "latitude": -20.45,
    "longitude": -54.62
  },
  "sources": [
    {
      "source": "Open-Meteo",
      "available": true,
      "message": "Dados carregados com sucesso.",
      "current": {
        "temperatureC": 28.3,
        "feelsLikeC": 30.1,
        "humidity": 63,
        "windKph": 11.2,
        "condition": "Parcialmente nublado",
        "maxTempC": 32.0,
        "minTempC": 22.0
      },
      "forecast": [
        { "date": "2026-03-29", "minTempC": 22.0, "maxTempC": 32.0, "condition": "Parcialmente nublado" }
      ]
    },
    {
      "source": "WeatherAPI",
      "available": true,
      "message": "Dados carregados com sucesso.",
      "current": {
        "temperatureC": 27.4,
        "feelsLikeC": 29.0,
        "humidity": 68,
        "windKph": 9.8,
        "condition": "Ensolarado",
        "maxTempC": 31.0,
        "minTempC": 21.0
      },
      "forecast": [
        { "date": "2026-03-29", "minTempC": 21.0, "maxTempC": 31.0, "condition": "Ensolarado" }
      ]
    }
  ],
  "consolidated": {
    "sourcesUsed": ["Open-Meteo", "WeatherAPI"],
    "current": {
      "temperatureC": 27.85,
      "feelsLikeC": 29.55,
      "humidity": 66,
      "windKph": 10.5,
      "condition": "Parcialmente nublado",
      "maxTempC": 31.5,
      "minTempC": 21.5
    }
  },
  "forecast": [
    { "date": "2026-03-29", "minTempC": 21.5, "maxTempC": 31.5, "condition": "Parcialmente nublado" }
  ]
}
```

## 9) Testes implementados

Back-end:
- normalização Open-Meteo
- normalização WeatherAPI
- consolidação
- controller principal (`/api/weather/search`)

Comando:
```bash
cd metabuscadorclima-api
mvn test
```

Observação: neste ambiente, a execução foi validada com:
```bash
cd metabuscadorclima-api
mvn -Dmaven.repo.local=/tmp/metaclima-m2 test
```

Front-end:
- não foi adicionado framework de testes para manter simplicidade e rapidez de apresentação acadêmica.

## 10) Limitações
- Dependência de disponibilidade das APIs externas.
- Para autocomplete mundial completo de países/estados/cidades, configure `CSCAPI_KEY`.
- Sem `CSCAPI_KEY`, o projeto usa catálogo local reduzido como fallback para demonstração.
- Alguns códigos de condição climática da Open-Meteo usam mapeamento textual simplificado.
- Sem persistência em banco (intencional, foco em integração e consolidação).

## 11) Melhorias futuras
- Cache de consultas por cidade.
- Internacionalização completa (pt/en/es).
- Histórico de buscas no front-end.
- Testes E2E para fluxo completo.
- Métricas de latência por fonte e observabilidade ampliada.

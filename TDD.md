## Technical Design Document (TDD): DailyBrief CLI

### Document Meta

| Document Meta | Details            |
| ------------- | ------------------ |
| **Project**     | DailyBrief         |
| **Version**     | 1.0                |
| **Tech Lead**   | Senior Architect   |
| **Based on PRD**| v1.0               |

### 1. System Architecture

#### 1.1 High-Level Design
The application follows a Command-Line MVC (Model-View-Controller) pattern adapted for stateless execution.

1.  **Controller (Picocli):** Parses command line arguments.
2.  **Service Layer (Business Logic):** Initiates parallel HTTP requests.
3.  **Model Layer (Records):** Maps JSON responses to immutable Java objects.
4.  **View (Renderer):** Formats the aggregated data into ANSI-colored text and ASCII art.

#### 1.2 Project Directory Structure
We will adhere to the standard Maven layout. **Architecture Decision:** We are using package-by-feature/layer to keep concerns separated.

```text
src/main/java/com/dailybrief/
├── App.java                    # Entry Point (public static void main)
├── commands/
│   └── RootCommand.java        # Controller Layer
│                               # Picocli @Command implementation
├── config/
│   └── AppConfig.java          # Dotenv loading & HttpClient Singleton
├── services/
│   ├── WeatherService.java     # OpenWeatherMap logic
│   ├── NewsService.java        # NewsAPI logic
│   └── DashboardService.java   # Orchestrator (Async logic)
├── models/                     # Data Layer
│   ├── WeatherResponse.java    # Java Record
│   └── NewsResponse.java       # Java Record
└── ui/
    ├── ConsoleRenderer.java    # View Layer
    │                           # System.out wrapper
    ├── AsciiArt.java           # Weather Icon constants
    └── AnsiColors.java         # Color constants
```

### 2. Data Models (Schema)
Since this is a stateless V1 CLI, there is no SQL Database. The "Schema" is defined by the External API JSON structure which we map to Java Records.

#### 2.1 Weather Model (Record)
Maps to: `api.openweathermap.org/data/2.5/weather`
```java
public record WeatherResponse(
    String name,
    Main main,
    List<Weather> weather
) {
    public record Main(double temp, int humidity) {}
    public record Weather(String main, String description, String icon) {}
}
```

#### 2.2 News Model (Record)
Maps to: `newsapi.org/v2/top-headlines`
```java
public record NewsResponse(
    String status,
    List<Article> articles
) {
    public record Article(String title, String description, Source source) {}
    public record Source(String name) {}
}
```

### 3. API Interface (External)
The CLI acts as a Client. It consumes the following REST endpoints.

#### 3.1 Weather Service
*   **Endpoint:** `GET https://api.openweathermap.org/data/2.5/weather`
*   **Query Params:**
    *   `q`: City Name (from CLI arg or .env)
    *   `appid`: API Key
    *   `units`: `metric` (default) or `imperial`
*   **Success:** 200 OK (JSON)
*   **Failure:** 404 (City not found), 401 (Invalid Key)

#### 3.2 News Service
*   **Endpoint:** `GET https://newsapi.org/v2/top-headlines`
*   **Query Params:**
    *   `country`: `us` (default)
    *   `apiKey`: API Key
    *   `pageSize`: `5` (Limit to 5 stories)
*   **Success:** 200 OK (JSON)

### 4. Component Design

#### 4.1 `AppConfig.java` (Singleton)
*   **Responsibility:** Load `.env` file using `dotenv-java`.
*   **Key Method:** `getHttpClient()` -> Returns a shared `HttpClient` instance configured with a 5-second connection timeout (per PRD 5.1).

#### 4.2 `DashboardService.java` (The Core Orchestrator)
*   **Responsibility:** This is the most critical class. It handles the concurrency.
*   **Logic:**
    1.  Accepts `city` and `flags` (news-only, etc.).
    2.  Creates `CompletableFuture` for Weather.
    3.  Creates `CompletableFuture` for News.
    4.  Uses `CompletableFuture.allOf(...)` to wait.
    5.  Returns a `DashboardData` DTO containing both results.
*   **Error Handling:** Must wrap calls in `exceptionally` blocks to ensure one failure doesn't crash the other.

#### 4.3 `ConsoleRenderer.java`
*   **Responsibility:** Pure functions that take data and return formatted strings.
*   **Logic:**
    *   `renderWeather(WeatherResponse w)`: Selects ASCII art based on `w.weather[0].icon`.
    *   `renderNews(NewsResponse n)`: Loops through articles, applying ANSI Cyan to titles and Grey to sources.

### 5. Security & Risks

| Risk              | Impact                       | Mitigation Strategy                                                                                           |
| ----------------- | ---------------------------- | ------------------------------------------------------------------------------------------------------------- |
| **API Key Leak**    | Quota theft, billing overages | 1. Use `io.github.cdimascio:dotenv-java`.<br>2. Add `.env` to `.gitignore` immediately.<br>3. Strictly forbid committing keys in code. |
| **Input Injection** | Malformed URLs               | Use `URLEncoder.encode(city, StandardCharsets.UTF_8)` before appending to the API URL string.                 |
| **Dependency Vulns** | Supply chain attacks         | Use Maven `versions-maven-plugin` to check for updates regularly.                                             |

### 6. Testing Strategy

#### 6.1 Unit Tests (JUnit 5 + Mockito)
1.  **`WeatherServiceTest.java`**: Mock `HttpClient`. Return a fake JSON string. Assert that the JSON is correctly parsed into the `WeatherResponse` record (checking temp and city name).
2.  **`UrlBuilderTest.java`**: Test that spaces in city names (e.g., "New York") are correctly encoded to "New%20York".
3.  **`ArgParserTest.java`**: Test that passing `--no-news` sets the boolean flag correctly in the Command object.

#### 6.2 Integration Test
1.  **`AsyncOrchestratorTest.java`**:
    *   Mock both Services to sleep for 1 second.
    *   Run the orchestrator.
    *   Assert: Total execution time is ~1 second (Parallel), NOT ~2 seconds (Sequential).
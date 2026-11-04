# SmartDuken AI

Қазақстандағы шағын азық-түлік дүкенінің өнім қорын есепке алатын алғашқы нұсқа.
Java 21, Spring Boot 3.5.7, Maven 3.9.11 және PostgreSQL 16+ қолданылады.
Бұл нұсқада өнімдерді басқару ғана бар.

## Windows жүйесінде орнату (PowerShell)

1. JDK 21 (мысалы, Eclipse Temurin), Apache Maven 3.9.11, PostgreSQL 16 немесе одан жаңа нұсқасын және Git орнатыңыз. PostgreSQL орнатқышында әкімші құпиясөзін өзіңіз таңдаңыз. PostgreSQL қызметі жұмыс істеп тұруы керек.
2. `JAVA_HOME` айнымалысын JDK қалтасына бағыттаңыз; `PATH` ішіне `%JAVA_HOME%\bin`, Maven-нің `bin` қалтасын және PostgreSQL-дің `bin` қалтасын қосыңыз. Терминалды қайта ашыңыз. Әдеттегі PostgreSQL жолы: `C:\Program Files\PostgreSQL\16\bin`.
3. Нұсқаларды тексеріңіз:

```powershell
java -version
mvn -version
psql --version
```

Java және Maven көрсететін Java нұсқасы 21 болуы керек.

```powershell
git clone https://github.com/baizhanrakhimbek-code/smartduken-ai.git
cd smartduken-ai
psql -h localhost -U postgres -d postgres
```

`psql` әкімші құпиясөзін сұрайды. SQL консольде орындаңыз:

```sql
CREATE ROLE smartduken_user LOGIN;
\password smartduken_user
CREATE DATABASE smartduken OWNER smartduken_user;
\q
```

`\password` пәрмені жаңа құпиясөзді екі рет сұрайды; ол кодта сақталмайды.
PowerShell ішінде қолданбаға қосылу деректерін беріңіз:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/smartduken"
$env:DB_USERNAME = "smartduken_user"
$dbCredential = Get-Credential -UserName "smartduken_user" -Message "Дерекқор құпиясөзі"
$env:DB_PASSWORD = $dbCredential.GetNetworkCredential().Password
mvn test
mvn spring-boot:run
```

Браузерде `http://localhost:8080` ашыңыз. Тоқтату: `Ctrl+C`.
Басқа порт үшін іске қоспас бұрын `$env:PORT = "8081"` орнатыңыз.
Жаңа PowerShell терезесінде DB айнымалыларын қайта орнатыңыз. `.env` файлы автоматты оқылмайды.
Жұмыс аяқталғанда `Remove-Item Env:DB_PASSWORD` және `Remove-Variable dbCredential` орындауға болады.

Жиналған JAR арқылы іске қосу:

```powershell
mvn clean package
java -jar target/smartduken-ai-0.0.1-SNAPSHOT.jar
```

`application-example.properties` — құпиясөзсіз үлгі. Негізгі баптау `src/main/resources/application.properties` ішінде: DB_URL, DB_USERNAME және DB_PASSWORD міндетті. Hibernate `update` кестені жергілікті әзірлеу кезінде жасайды/жаңартады. Нақты пайдалануға шығарар алдында дерекқор миграциялары қажет.

## API

| Әдіс | Мекенжай | Нәтиже |
|---|---|---|
| GET | `/api/products` | Барлық өнім, 200 |
| GET | `/api/products/{id}` | Бір өнім, 200 немесе 404 |
| POST | `/api/products` | Қосу, 201 және Location тақырыбы |
| PUT | `/api/products/{id}` | Барлық енгізілетін өрісті жаңарту, 200 немесе 404 |
| DELETE | `/api/products/{id}` | Өшіру, 204 немесе 404 |

PowerShell мысалы (қолданба басқа терминалда жұмыс істесін):

```powershell
$product = @{ name = "Сүт"; category = "Сүт өнімдері"; price = 550.25; stockQuantity = 12; minimumStock = 3 }
$json = $product | ConvertTo-Json
$created = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/products -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
Invoke-RestMethod http://localhost:8080/api/products
Invoke-RestMethod "http://localhost:8080/api/products/$($created.id)"
$product.stockQuantity = 8
$json = $product | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/products/$($created.id)" -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/products/$($created.id)"
```

Өнім атауы міндетті (150 таңбаға дейін). Санат міндетті емес (100 таңбаға дейін). Баға теріс емес, үтірден кейін ең көбі 2 таңба; JSON ішінде ондық бөлгіш — нүкте. Қор мен ең аз қор — теріс емес бүтін сандар, ең көбі 2147483647. Жоқ немесе қате өрістер үшін 400 және ProblemDetail JSON қайтарылады; `errors` өрісінде өрістер бойынша түсіндірме бар. ID серверде жасалады, сұрауға қоспаңыз. Белгісіз JSON өрістері қабылданбайды.

## Қалталар мен кодтың қарапайым түсіндірмесі

```text
pom.xml                                      Maven тәуелділіктері мен Java нұсқасы
application-example.properties               Құпиясыз қосылу үлгісі
src/main/java/kz/smartduken/app/
  SmartDukenApplication.java                  Қолданбаны іске қосады
  product/
    Product.java                             Дерекқордағы өнім кестесі
    ProductRequest.java                      Енгізілетін өрістер мен тексеру ережелері
    ProductRepository.java                   Дерекқордан оқу және сақтау
    ProductService.java                      CRUD логикасы мен транзакциялар
    ProductController.java                   HTTP сұрауларын қабылдау
    ProductNotFoundException.java            Өнім табылмағанын білдіреді
    ApiExceptionHandler.java                 Қателерді түсінікті JSON-ға айналдыру
src/main/resources/
  application.properties                     Дерекқор мен сервер баптауы
  static/index.html                          Қазақша өнімдер беті және қосу формасы
  static/style.css                           Телефон мен компьютерге бейімделген көрініс
  static/app.js                              API арқылы өнімді оқу, қосу, өңдеу, өшіру
src/test/java/.../product/                    CRUD интеграциялық және сервис unit тесттері
src/test/resources/application-test.properties  Тестке арналған уақытша H2 дерекқоры
```

Сұрау Controller → Service → Repository → PostgreSQL жолымен өтеді. `ProductRequest` қате деректерді тоқтатады. `Product` сақталған деректерді көрсетеді. Бет өнім атауын қауіпсіз `textContent` арқылы шығарады; азайған қорды сары түспен белгілейді. Әр жолдағы «Өңдеу» батырмасы өнімді формаға толтырады; «Өзгерістерді сақтау» оны жаңартады. «Бас тарту» қосу режиміне қайтарады. «Өшіру» растаудан кейін өнімді жояды. Қор саны ең аз қордан аспаса, жол сары болады.

## Тексеру

`mvn test` дерекқор орнатусыз H2-нің PostgreSQL режимінде орындалады. Интеграциялық тесттер нақты Controller, Service және JPA Repository қабаттарын бірге тексереді: қосу, тізім, ID бойынша оқу, өзгерту, өшіру, 404, қате JSON, валидация және нөлдік мәндер. Unit тест жоқ өнімді өшіруге жол берілмейтінін тексереді. H2 тесттері PostgreSQL серверімен тексерудің орнына жүрмейді; PostgreSQL тексеруі үшін жоғарыдағы іске қосу мен API мысалын орындаңыз.

Қосылу қатесі болса PostgreSQL қызметін, DB_URL портын және пайдаланушы құпиясөзін тексеріңіз. Maven тәуелділіктерді жүктеу үшін Maven Central-ға желілік қатынас қажет.

Осы бұлттық ортадағы нақты нәтиже: Java 21 және Maven 3.9.11 арқылы `mvn verify` орындалды — 6 тест, 0 қате, 0 өткізіп жіберілген; JAR сәтті жиналды. Қайта орындалатын орнату сценарийі де өтті. H2 тест профилімен іске қосылған серверде қазақша басты бет және нақты HTTP CRUD циклі тексерілді. `node --check` JavaScript синтаксисін тексерді; браузердегі визуалды тексеру орындалмады. PostgreSQL серверімен тексеру орындалмады: бұл ортада DB_URL, DB_USERNAME және DB_PASSWORD берілмеген.

MVP интерфейсінің қосымша тексеруі: тізім, аз қор белгісі, мәтінді қауіпсіз шығару, қосу (POST), өңдеу (PUT), өшіру (204 жауабы) және бас тарту әрекеттері Node.js ішінде модельденген DOM/fetch арқылы тексерілді. Бұл браузердегі визуалды тексеру емес.

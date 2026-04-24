# ICD-10 Онош Санал Болгох Систем

Эмчид оноштой холбоотой ICD-10 кодыг хурдан хайж олох боломжийг олгох микросервис систем.

---

## Системийн бүтэц

```
┌─────────────────────────────────────────────────┐
│                   Хэрэглэгч / UI                 │
│              http://localhost:8080               │
└─────────────────────┬───────────────────────────┘
                      │ REST
┌─────────────────────▼───────────────────────────┐
│           icd10-suggest-service (:8080)          │
│  - Онош хайх                                     │
│  - Chapter / Category / Subcategory шүүлтүүр    │
│  - PostgreSQL DB                                 │
└─────────────────────┬───────────────────────────┘
                      │ REST (seed үед нэг удаа)
┌─────────────────────▼───────────────────────────┐
│           icd10-code-service (:8082)             │
│  - ICD-10 JSON файл уншиж буцаана               │
└─────────────────────────────────────────────────┘
```

---

## Технологи

| Технологи         | Зориулалт                |
| ----------------- | ------------------------ |
| Java 17 / 21      | Програмчлалын хэл        |
| Spring Boot 3.2   | Фреймворк                |
| Spring Data JPA   | Өгөгдлийн сантай ажиллах |
| PostgreSQL        | Өгөгдлийн сан            |
| Hibernate         | ORM                      |
| RestTemplate      | Сервис хоорондын холбоо  |
| Lombok            | Boilerplate код багасгах |
| SpringDoc OpenAPI | Swagger UI               |
| spring-dotenv     | .env файлын дэмжлэг      |
| Maven             | Build хэрэгсэл           |

---

## Шаардлагууд (ФШ)

| Дугаар | Тайлбар                                                        | MoSCoW |
| ------ | -------------------------------------------------------------- | ------ |
| ФШ-01  | Эмчид оношны мэдээллийг мэдээллийн сангаас хайх боломж олгох   | M      |
| ФШ-02  | Оношийг ерөнхий төрөл, төрөл, дэд төрөл, түлхүүр үгээр хайх    | M      |
| ФШ-03  | Хайлтын үр дүнгээс оношийг шууд сонгон оруулах                 | M      |
| ФШ-04  | Хайлтын үр дүн ойлгомжтой, жагсаалт хэлбэрээр харуулах         | S      |
| ФШ-05  | Сонгосон оношны код болон нэрийг автоматаар бөглөх             | S      |
| ФШ-06  | Буруу эсвэл хоосон утга оруулсан үед алдааны мэдэгдэл харуулах | M      |

---

## Суулгах заавар

### Урьдчилсан шаардлага

- Java 17 эсвэл 21
- Maven 3.8+
- PostgreSQL 14+
- Git

### 1. Repository татах

```bash
git clone https://github.com/Aydana06/icd10-code-service.git
git clone https://github.com/Aydana06/icd10-suggest-service.git
```

### 2. PostgreSQL тохируулах

```sql
CREATE DATABASE hospital_db;
```

### 3. icd10-code-service тохируулах

```bash
cd icd10-code-service
```

`.env` файл үүсгэнэ:

```env
SERVER_PORT=8082
LOG_LEVEL_ROOT=INFO
```

Эхлүүлэх:

```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

### 4. icd10-suggest-service тохируулах

```bash
cd icd10-suggest-service
```

`.env` файл үүсгэнэ:

```env
SERVER_PORT=8080
CONTEXT_PATH=/api

DB_HOST=localhost
DB_PORT=5432
DB_NAME=hospital_db
DB_USERNAME=postgres
DB_PASSWORD=нууц_үг

ICD10_API_URL=http://localhost:8082/api/icd10
ICD10_API_TIMEOUT=5000

JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
```

Эхлүүлэх:

```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

---

## Ажиллах зарчим

### DB Seed (нэг удаа автоматаар)

```
suggest-service эхэлнэ
    → DB хоосон эсэхийг шалгана (repository.count())
    → Хоосон бол: GET http://localhost:8082/api/icd10 дуудна
    → JSON parse хийж бүх кодыг DB-д хадгална
    → Дараагийн restart-д DB-д өгөгдөл байвал seed алгасна
```

### Хайлт

```
Хэрэглэгч хайлт хийнэ
    → POST /api/diagnosis/suggest
    → Local DB-с LIKE хайлт (findByKeywordWithLimit)
    → Relevance score тооцно
    → Score > 0.2 шүүж, эрэмбэлж буцаана
```

### 4 баганат UI шүүлтүүр

```
Chapter сонгоно → GET /diagnosis/categories?chapter=I
    → Category сонгоно → GET /diagnosis/subcategories?category=A00-A09
        → Subcategory сонгоно → GET /diagnosis/codes?subcategory=A00
```

---

## API Endpoints

### icd10-suggest-service — `http://localhost:8080/api`

| Method | URL                                         | Тайлбар              |
| ------ | ------------------------------------------- | -------------------- |
| POST   | `/diagnosis/suggest`                        | Онош хайх            |
| GET    | `/diagnosis/chapters`                       | Chapter жагсаалт     |
| GET    | `/diagnosis/categories?chapter=I`           | Category жагсаалт    |
| GET    | `/diagnosis/subcategories?category=A00-A09` | Subcategory жагсаалт |
| POST   | `/diagnosis/init`                           | Seed дахин дуудах    |
| GET    | `/diagnosis/health`                         | Сервисийн төлөв      |
| GET    | `/swagger-ui.html`                          | API документ         |

### icd10-code-service — `http://localhost:8082`

| Method | URL                 | Тайлбар              |
| ------ | ------------------- | -------------------- |
| GET    | `/api/icd10`        | Бүх chapter жагсаалт |
| GET    | `/api/icd10/{code}` | Chapter код-оор хайх |

### Хүсэлтийн жишээ

**Онош хайх:**

```bash
curl -X POST http://localhost:8080/api/diagnosis/suggest \
  -H "Content-Type: application/json" \
  -d '{"diagnosis": "халдвар", "resultLimit": 10}'
```

**Хариу:**

```json
{
  "success": true,
  "diagnosis": "халдвар",
  "message": "5 онош олдлоо",
  "suggestions": [
    {
      "code": "A09",
      "name": "Гэдэсний халдварт бус гаралтай суулгалт",
      "detail": "",
      "relevanceScore": 0.8
    }
  ],
  "responseTime": 45
}
```

---

## Төслийн бүтэц

### icd10-suggest-service

```
src/main/java/mn/num/hospital/icd10_service/
├── controller/
│   └── DiagnosisController.java      # REST endpoint
├── service/
│   ├── DiagnosisService.java         # Бизнес логик
│   └── ExternalICD10Service.java     # code-service REST дуудлага
├── domain/
│   ├── ICD10Code.java                # Үндсэн entity
│   ├── Chapter.java                  # Бүлэг
│   ├── Category.java                 # Ангилал
│   └── Subcategory.java              # Дэд ангилал
├── dto/
│   ├── ICD10CodeDTO.java
│   ├── ICD10CodeMapper.java
│   ├── DiagnosisRequest.java
│   └── DiagnosisResponse.java
├── repo/
│   ├── ICD10CodeRepository.java
│   ├── ChapterRepository.java
│   ├── CategoryRepository.java
│   └── SubcategoryRepository.java
└── ICD10ServiceApplication.java

src/main/resources/
├── static/index.html                 # Web UI
└── application.properties
.env                                  # Нууц тохиргоо
```

### icd10-code-service

```
src/main/java/mn/edu/num/icd10_service/
├── controller/
│   └── ICD10Controller.java          # REST endpoint
├── service/
│   └── ICD10Service.java             # JSON parse
├── domain/
│   ├── Chapter.java
│   ├── Category.java
│   ├── Subcategory.java
│   └── Subcode.java
└── Icd10ServiceApplication.java

src/main/resources/
└── application.properties
.env
.env.example
```

---

## Өгөгдлийн сангийн загвар

```
chapters
├── id (PK)
├── chapter        "I", "II", ...
├── name           "Халдварт ба шимэгчит өвчин"
├── order_index    1, 2, ...
└── range          "A00-B99"

categories
├── id (PK)
├── range          "A00-A09"
├── range_start    "A00"
├── name           "Гэдэсний халдварт өвчин"
└── chapter_id (FK → chapters)

subcategories
├── id (PK)
├── code           "A00"
├── name           "Урвах тахал"
└── category_id (FK → categories)

icd10_codes
├── id (PK)
├── code           "A00.0"
├── name           "Урвах тахлын вибрион..."
├── detail         "Сонгодог хэлбэр"
├── relevance_score
├── chapter_id (FK → chapters)
├── category_id (FK → categories)
└── subcategory_id (FK → subcategories)
```

---

## Алдааг засах

### DB дахин seed хийх

```sql
TRUNCATE TABLE icd10_codes CASCADE;
TRUNCATE TABLE subcategories CASCADE;
TRUNCATE TABLE categories CASCADE;
TRUNCATE TABLE chapters CASCADE;
```

```bash
curl -X POST http://localhost:8080/api/diagnosis/init
```

### PostgreSQL нууц үг алдаа

`.env` файлын `DB_PASSWORD` зөв эсэхийг шалгана:

```bash
psql -U postgres -h localhost -c "\l"
```

### code-service холбогдохгүй байвал

```bash
curl http://localhost:8082/api/icd10
# Хариу байхгүй бол code-service эхлүүлнэ
cd icd10-code-service && mvn spring-boot:run
```

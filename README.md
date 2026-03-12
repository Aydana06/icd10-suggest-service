## 5-Minute Quick Start

```bash
# 1. Open Eclipse
# 2. File → Import → Existing Projects
# 3. Select project folder
# 4. Right-click project → Maven → Update Project (Alt+F5)
# 5. Run: Ctrl+F11
# 6. Browser: http://localhost:8080/api
```

---

## 🔗 Quick URLs

| Purpose          | URL                                        |
| ---------------- | ------------------------------------------ |
| **Frontend**     | http://localhost:8080                      |
| **Health Check** | http://localhost:8080/api/diagnosis/health |
| **API Docs**     | http://localhost:8080/api/swagger-ui.html  |
| **Database**     | http://localhost:8080/api/h2-console       |
| **API Base**     | http://localhost:8080/api                  |

---

## API Endpoints

### Initialize Data

```bash
curl -X POST http://localhost:8080/api/diagnosis/init \
  -H "Content-Type: application/json" \
  -d '{}'
```

### Search Codes

```bash
curl -X POST http://localhost:8080/api/diagnosis/suggest \
  -H "Content-Type: application/json" \
  -d '{"diagnosis":"diabetes","resultLimit":5}'
```

### Health Check

```bash
curl http://localhost:8080/api/diagnosis/health
```

---

## Common Issues

| Issue                | Fix                                                   |
| -------------------- | ----------------------------------------------------- |
| Port 8080 in use     | Change `server.port=8081` in `application.properties` |
| 0 suggestions        | POST `/api/diagnosis/init` to load sample data        |
| Frontend not loading | Check `src/main/resources/static/index.html` exists   |
| Dependencies error   | Right-click → Maven → Update Project (Alt+F5)         |
| API error            | Check internet, verify backend running                |

---

## Maven Commands

```bash
# Update dependencies
mvn clean install

# Run Spring Boot
mvn spring-boot:run

# Package JAR
mvn package
```

---

## Database Queries (H2 Console)

```sql
-- Count codes
SELECT COUNT(*) FROM icd10_codes;

-- Show all codes
SELECT * FROM icd10_codes LIMIT 10;

-- Search by diagnosis
SELECT * FROM icd10_codes
WHERE description LIKE '%diabetes%';

-- View table structure
SELECT * FROM information_schema.tables
WHERE table_name = 'ICD10_CODES';
```

---

## Dependencies Used

| Dependency        | Version | Purpose         |
| ----------------- | ------- | --------------- |
| Spring Boot       | 3.2.0   | Framework       |
| Spring Data JPA   | 3.2.0   | Database        |
| H2 Database       | Latest  | Development DB  |
| Lombok            | Latest  | Code generation |
| Jackson           | Latest  | JSON parsing    |
| Springdoc OpenAPI | 2.0.4   | API docs        |

---

## Frontend Technologies

- **HTML5** - Structure
- **CSS3** - Styling (Glassmorphism)
- **JavaScript** - Interactivity
- **Bootstrap 5** - (Optional) UI framework
- **FontAwesome** - Icons

---

## External APIs

### ICD-10 Data Source

```
URL: http://localhost:8082/api/icd10
Method: GET
Response: JSON array of codes
Codes: 69,000+
```

---

## Performance Tips

```
1. First query slow? → External API fetching + saving to DB
2. Second query fast? → Database cache working ✓
3. Too many results? → Lower resultLimit value
4. Slow network? → Increase icd10.api.timeout
```

---

## File Reference

| File                       | Purpose            | Location                        |
| -------------------------- | ------------------ | ------------------------------- |
| `README.md`                | Project overview   | root                            |
| `pom.xml`                  | Maven dependencies | root                            |
| `application.properties`   | Configuration      | `src/main/resources/`           |
| `index.html`               | Frontend UI        | `src/main/resources/static/`    |
| `DiagnosisController.java` | REST endpoints     | `src/main/java/.../controller/` |
| `DiagnosisService.java`    | Business logic     | `src/main/java/.../service/`    |

---

## Success Indicators

**You're ready when:**

- [ ] Java 17 installed
- [ ] Maven 3.8+ installed
- [ ] Eclipse opened
- [ ] Project imported
- [ ] Dependencies downloaded
- [ ] Application runs (Ctrl+F11)
- [ ] http://localhost:8080/ loads
- [ ] API responds to requests
- [ ] Frontend displays

---

## Emergency Fixes

### Everything broken?

```bash
1. Stop app (Ctrl+F2)
2. Clean: Maven → Clean
3. Update: Maven → Update Project
4. Rebuild: Project → Clean
5. Run: Ctrl+F11
```

---

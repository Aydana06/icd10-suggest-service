package mn.num.hospital.icd10_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mn.num.hospital.icd10_service.dto.DiagnosisRequest;
import mn.num.hospital.icd10_service.dto.DiagnosisResponse;
import mn.num.hospital.icd10_service.service.DiagnosisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Онош хайх REST controller.
 * Base URL: /api/diagnosis
 *
 * Шаардлага:
 * ФШ-01: POST /suggest - онош хайх
 * ФШ-02: GET /chapters, /categories, /subcategories - шүүлтүүр
 * ФШ-06: Validation алдааг HTTP 400 буцаана
 */
@RestController
@RequestMapping("/diagnosis")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class DiagnosisController {

    private static final int DEFAULT_LIMIT = 50;

    private final DiagnosisService diagnosisService;

    @PostMapping("/init")
    public ResponseEntity<String> initData() {
        return ResponseEntity.ok(diagnosisService.seedFromExternalAPI());
    }

    /**
     * ICD-10 код санал болгох
     * POST /api/diagnosis/suggest
     */
    @PostMapping("/suggest")
    public ResponseEntity<DiagnosisResponse> suggestCodes(
            @RequestBody DiagnosisRequest request) {
        int limit = request.getResultLimit() > 0 ? request.getResultLimit() : DEFAULT_LIMIT;
        log.info("Хайлтын хүсэлт: diagnosis='{}', limit={}",
                request.getDiagnosis(), limit);
        DiagnosisResponse response = diagnosisService.suggestCodes(request.getDiagnosis(), limit);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * Chapter жагсаалт буцаана
     * GET /api/diagnosis/chapters
     * Web UI-н эхний dropdown-д ашиглагдана.
     */
    @GetMapping("/chapters")
    public ResponseEntity<List<String>> getChapters() {
        return ResponseEntity.ok(diagnosisService.getChapters());
    }

    /**
     * Chapter-аар category жагсаалт буцаана (ФШ-02).
     * GET /api/diagnosis/categories?chapter=I
     *
     * @param chapter chapter код — жишээ: "I", "II"
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories(
            @RequestParam String chapter) {
        return ResponseEntity.ok(diagnosisService.getCategoriesByChapter(chapter));
    }

    /**
     * Category-аар subcategory жагсаалт буцаана (ФШ-02).
     * GET /api/diagnosis/subcategories?category=A00-A09
     *
     * @param category category range - жишээ: "A00-A09"
     */
    @GetMapping("/subcategories")
    public ResponseEntity<List<String>> getSubcategories(
            @RequestParam String category) {
        return ResponseEntity.ok(diagnosisService.getSubcategoriesByCategory(category));
    }

    /**
     * Сервисийн ажиллагааг шалгах.
     * GET /api/diagnosis/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ICD-10 Service is running");
    }
}
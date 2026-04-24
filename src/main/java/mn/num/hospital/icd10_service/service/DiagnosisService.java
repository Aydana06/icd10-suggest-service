package mn.num.hospital.icd10_service.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import mn.num.hospital.icd10_service.domain.*;
import mn.num.hospital.icd10_service.dto.*;
import mn.num.hospital.icd10_service.repo.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
@Service
@Slf4j
public class DiagnosisService {

    private final ICD10CodeRepository icd10Repo;
    private final ChapterRepository chapterRepo;
    private final CategoryRepository categoryRepo;
    private final SubcategoryRepository subcategoryRepo;
    private final ExternalICD10Service externalService;

    public DiagnosisService(ICD10CodeRepository icd10Repo,
                            ChapterRepository chapterRepo,
                            CategoryRepository categoryRepo,
                            SubcategoryRepository subcategoryRepo,
                            ExternalICD10Service externalService) {
        this.icd10Repo = icd10Repo;
        this.chapterRepo = chapterRepo;
        this.categoryRepo = categoryRepo;
        this.subcategoryRepo = subcategoryRepo;
        this.externalService = externalService;
    }

    // SeedRequestProducer хасаж, REST seed буцаав
    @PostConstruct
    public void init() {
        if (icd10Repo.count() == 0) {
            log.info("DB хоосон байна, REST-ээр seed хийж байна...");
            seedFromExternalAPI();
        } else {
            log.info("DB-д {} код байна, seed алгасав", icd10Repo.count());
        }
    }

    /**
     * code-service-с бүх кодыг REST-ээр татаж DB-д хадгална.
     * @PostConstruct болон /diagnosis/init endpoint-аас дуудагдана.
     */
    public String seedFromExternalAPI() {
        log.info("Seed эхэллээ...");
        List<ICD10CodeDTO> allCodes = externalService.fetchAllCodes();

        if (allCodes.isEmpty()) {
            return "API-с өгөгдөл ирсэнгүй";
        }

        int saved = 0, skipped = 0;
        for (ICD10CodeDTO dto : allCodes) {
            if (icd10Repo.findByCode(dto.getCode()).isEmpty()) {
                icd10Repo.save(convertToEntity(dto));
                saved++;
            } else {
                skipped++;
            }
        }

        String msg = String.format("Seed дууслаа: %d хадгалсан, %d давхардсан", saved, skipped);
        log.info(msg);
        return msg;
    }

    public List<String> getChapters() {
        return chapterRepo.findAll().stream()
                .sorted(Comparator.comparing(
                        ch -> ch.getOrderIndex() != null ? ch.getOrderIndex() : 999))
                .map(ch -> ch.getChapter() + " " + ch.getName())
                .toList();
    }

    public List<String> getCategoriesByChapter(String chapterCode) {
        if (chapterCode == null || chapterCode.isBlank()) return List.of();
        return icd10Repo.findCategoriesByChapter(chapterCode);
    }

    public List<String> getSubcategoriesByCategory(String categoryRange) {
        if (categoryRange == null || categoryRange.isBlank()) return List.of();
        return icd10Repo.findSubcategoriesByCategory(categoryRange);
    }

    public DiagnosisResponse suggestCodes(String diagnosis, int limit) {
        long startTime = System.currentTimeMillis();
        DiagnosisResponse response = new DiagnosisResponse();
        response.setDiagnosis(diagnosis);

        if (diagnosis == null || diagnosis.trim().isBlank()) {
            return buildErrorResponse(response, "Оношны нэр хоосон байна", startTime);
        }
        if (diagnosis.trim().length() < 2) {
            return buildErrorResponse(response, "Хайлтын үг хэтэрхий богино (2+ тэмдэгт)", startTime);
        }
        if (diagnosis.trim().length() > 100) {
            return buildErrorResponse(response, "Хайлтын үг хэтэрхий урт (100- тэмдэгт)", startTime);
        }

        try {
            List<ICD10CodeDTO> results = icd10Repo
                    .findByKeywordWithLimit(diagnosis.trim(), PageRequest.of(0, limit * 2))
                    .stream()
                    .map(ICD10CodeMapper::toDto)
                    .collect(Collectors.toList());

            results.forEach(s -> s.setRelevanceScore(calculateScore(s, diagnosis)));

            results = results.stream()
                    .filter(s -> s.getRelevanceScore() > 0.2)
                    .sorted(Comparator.comparingDouble(ICD10CodeDTO::getRelevanceScore).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

            response.setSuggestions(results);
            response.setSuccess(true);
            response.setMessage(results.isEmpty() ? "Тохирох онош олдсонгүй"
                    : results.size() + " онош олдлоо");

        } catch (Exception e) {
            log.error("Хайлтад алдаа:", e);
            return buildErrorResponse(response, "Хайлт хийхэд алдаа гарлаа", startTime);
        }

        response.setResponseTime(System.currentTimeMillis() - startTime);
        return response;
    }

    private ICD10Code convertToEntity(ICD10CodeDTO dto) {
        ICD10Code entity = ICD10CodeMapper.toEntity(dto);

        if (dto.getChapterCode() != null) {
            var chapter = chapterRepo.findByChapter(dto.getChapterCode())
                    .orElseGet(() -> {
                        Chapter ch = new Chapter();
                        ch.setChapter(dto.getChapterCode());
                        ch.setName(dto.getChapterName());
                        return chapterRepo.save(ch);
                    });
            entity.setChapter(chapter);
        }

        if (dto.getCategoryRange() != null) {
            var category = categoryRepo.findByRange(dto.getCategoryRange())
                    .orElseGet(() -> {
                        Category c = new Category();
                        c.setRange(dto.getCategoryRange());
                        c.setName(dto.getCategoryName());
                        chapterRepo.findByChapter(dto.getChapterCode())
                                .ifPresent(c::setChapter);
                        return categoryRepo.save(c);
                    });
            entity.setCategory(category);
        }

        if (dto.getSubcategoryCode() != null && !dto.getSubcategoryCode().isBlank()) {
            var sub = subcategoryRepo.findByCode(dto.getSubcategoryCode())
                    .orElseGet(() -> {
                        Subcategory s = new Subcategory();
                        s.setCode(dto.getSubcategoryCode());
                        s.setName(dto.getSubcategoryName());
                        categoryRepo.findByRange(dto.getCategoryRange())
                                .ifPresent(s::setCategory);
                        return subcategoryRepo.save(s);
                    });
            entity.setSubcategory(sub);
        }

        return entity;
    }

    private double calculateScore(ICD10CodeDTO dto, String diagnosis) {
        String keyword = diagnosis.toLowerCase();
        String name = dto.getName().toLowerCase();
        double score = 0;
        if (name.equals(keyword))   score += 1.0;
        if (name.contains(keyword)) score += 0.6;
        for (String word : Arrays.stream(keyword.split(" ")).distinct().toList()) {
            if (!word.isBlank() && name.contains(word)) score += 0.2;
        }
        return Math.min(score, 1.0);
    }

    private DiagnosisResponse buildErrorResponse(DiagnosisResponse response,
                                                  String message, long startTime) {
        response.setSuccess(false);
        response.setMessage(message);
        response.setSuggestions(Collections.emptyList());
        response.setResponseTime(System.currentTimeMillis() - startTime);
        return response;
    }
}
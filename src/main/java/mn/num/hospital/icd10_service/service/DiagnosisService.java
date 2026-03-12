package mn.num.hospital.icd10_service.service;

import lombok.extern.slf4j.Slf4j;
import mn.num.hospital.icd10_service.domain.ICD10Code;
import mn.num.hospital.icd10_service.dto.DiagnosisResponse;
import mn.num.hospital.icd10_service.dto.ICD10CodeDTO;
import mn.num.hospital.icd10_service.repo.ICD10CodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DiagnosisService {

    private final ICD10CodeRepository repository;
    private final ExternalICD10Service externalService;

    public DiagnosisService(ICD10CodeRepository repository,
                            ExternalICD10Service externalService) {
        this.repository = repository;
        this.externalService = externalService;
    }

    /**
     * Онош текстээс ICD-10 код санал болгох
     */
    public DiagnosisResponse suggestCodes(String diagnosis, int limit) {

        long startTime = System.currentTimeMillis();
        DiagnosisResponse response = new DiagnosisResponse();
        response.setDiagnosis(diagnosis);

        if (isInvalidDiagnosis(diagnosis)) {
            return buildErrorResponse(response, "Онош текст хоосон байна", startTime);
        }

        try {

            log.info("Хайлт эхэллээ: '{}'", diagnosis);

            List<ICD10CodeDTO> suggestions = getLocalResults(diagnosis);

            if (suggestions.size() < limit) {
                suggestions.addAll(fetchExternalResults(diagnosis, limit - suggestions.size()));
            }

            suggestions = sortAndLimit(suggestions, limit);

            response.setSuggestions(suggestions);
            response.setSuccess(true);
            response.setMessage(suggestions.size() + " кодын санал олгосон");

        } catch (Exception e) {

            log.error("Код санал болгох үед алдаа:", e);
            return buildErrorResponse(response, "Код санал болгох үед алдаа гарлаа", startTime);
        }

        response.setResponseTime(System.currentTimeMillis() - startTime);
        return response;
    }

    /**
     * Diagnosis validation
     */
    private boolean isInvalidDiagnosis(String diagnosis) {
        return diagnosis == null || diagnosis.trim().isEmpty();
    }

    /**
     * Local DB query
     */
    private List<ICD10CodeDTO> getLocalResults(String diagnosis) {

        log.info("Локал database-ээс хайж байна...");

        List<ICD10CodeDTO> results = repository.findByKeywordWithLimit(diagnosis)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("Локал DB-с {} үр дүн олдсон", results.size());

        return results;
    }

    /**
     * External API call
     */
    private List<ICD10CodeDTO> fetchExternalResults(String diagnosis, int remainingLimit) {

        log.info("External API дуудаж байна...");

        List<ICD10CodeDTO> externalResults =
                externalService.fetchFromExternalAPI(diagnosis, remainingLimit);

        saveExternalResults(externalResults);

        return externalResults;
    }

    /**
     * External results DB-д хадгалах
     */
    private void saveExternalResults(List<ICD10CodeDTO> externalResults) {

        for (ICD10CodeDTO dto : externalResults) {

            if (repository.findByCode(dto.getCode()).isEmpty()) {

                ICD10Code entity = convertToEntity(dto);

                repository.save(entity);

                log.debug("Код хадгалсан: {} - {}", entity.getCode(), entity.getName());
            }
        }
    }

    /**
     * Sort results
     */
    private List<ICD10CodeDTO> sortAndLimit(List<ICD10CodeDTO> suggestions, int limit) {

        return suggestions.stream()
                .sorted(Comparator.comparingDouble(ICD10CodeDTO::getRelevanceScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Error response builder
     */
    private DiagnosisResponse buildErrorResponse(DiagnosisResponse response,
                                                 String message,
                                                 long startTime) {

        response.setSuccess(false);
        response.setMessage(message);
        response.setSuggestions(Collections.emptyList());
        response.setResponseTime(System.currentTimeMillis() - startTime);

        return response;
    }

    /**
     * Entity → DTO
     */
    private ICD10CodeDTO convertToDTO(ICD10Code code) {

        ICD10CodeDTO dto = new ICD10CodeDTO();
        dto.setCode(code.getCode());
        dto.setName(code.getName());
        dto.setDetail(code.getDetail());
        dto.setCategory(code.getCategory());
        dto.setRelevanceScore(code.getRelevanceScore());

        return dto;
    }

    /**
     * DTO → Entity
     */
    private ICD10Code convertToEntity(ICD10CodeDTO dto) {

        ICD10Code code = new ICD10Code();
        code.setCode(dto.getCode());
        code.setName(dto.getName());
        code.setDetail(dto.getDetail());
        code.setCategory(dto.getCategory());
        code.setRelevanceScore(dto.getRelevanceScore());
        code.setCreatedAt(LocalDateTime.now());

        return code;
    }

    /**
     * Sample data initialization
     */
    public void initializeSampleData() {

        if (repository.count() > 0) {
            return;
        }

        List<ICD10Code> samples = List.of(
                createCode("E11.9",
                        "Type 2 diabetes mellitus without complications",
                        "Хүний 2-р төрлийн сахар цээсийн эмгэг",
                        "Endocrine", 0.95),

                createCode("K21.9",
                        "Unspecified reflux esophagitis",
                        "Цөөхөн мэдэгдэхүүлэх хоолойн үрэвсэл",
                        "Gastrointestinal", 0.92),

                createCode("J06.9",
                        "Acute upper respiratory infection, unspecified",
                        "Үл нарийвчлалтай хурц дээд амьсгалын инфекц",
                        "Respiratory", 0.88),

                createCode("I10",
                        "Essential (primary) hypertension",
                        "Үндсэн (нэгдэгч) цусны дүүргэл",
                        "Cardiovascular", 0.90)
        );

        repository.saveAll(samples);

        log.info("{} sample ICD10 codes хадгалагдсан", samples.size());
    }

    /**
     * Helper method
     */
    private ICD10Code createCode(String code,
                                 String name,
                                 String detail,
                                 String category,
                                 double score) {

        ICD10Code icd = new ICD10Code();
        icd.setCode(code);
        icd.setName(name);
        icd.setDetail(detail);
        icd.setCategory(category);
        icd.setRelevanceScore(score);
        icd.setCreatedAt(LocalDateTime.now());

        return icd;
    }
}
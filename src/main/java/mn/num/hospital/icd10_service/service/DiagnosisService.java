package mn.num.hospital.icd10_service.service;

import lombok.extern.slf4j.Slf4j;
import mn.num.hospital.icd10_service.domain.ICD10Code;
import mn.num.hospital.icd10_service.dto.DiagnosisResponse;
import mn.num.hospital.icd10_service.dto.ICD10CodeDTO;
import mn.num.hospital.icd10_service.repo.ICD10CodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * MAIN METHOD
     */
    public DiagnosisResponse suggestCodes(String diagnosis, int limit) {

        long startTime = System.currentTimeMillis();
        DiagnosisResponse response = new DiagnosisResponse();
        response.setDiagnosis(diagnosis);

        // VALIDATION
        if (isInvalidDiagnosis(diagnosis)) {
            return buildErrorResponse(response, "Онош буруу байна", startTime);
        }

        try {
            log.info("Хайлт эхэллээ: '{}'", diagnosis);

            // PARALLEL FETCH (DB + API)
            CompletableFuture<List<ICD10CodeDTO>> localFuture =
                    CompletableFuture.supplyAsync(() -> getLocalResults(diagnosis));

            CompletableFuture<List<ICD10CodeDTO>> externalFuture =
                    CompletableFuture.supplyAsync(() ->
                            externalService.fetchFromExternalAPI(diagnosis, limit));

            List<ICD10CodeDTO> suggestions =
                    Stream.concat(localFuture.join().stream(), externalFuture.join().stream())
                          .collect(Collectors.toList());

            // external data хадгалах
            saveExternalResults(externalFuture.join());

            // duplicate remove
            suggestions = removeDuplicates(suggestions);

            // score calculate (ranking)
            suggestions.forEach(s -> s.setRelevanceScore(
                    calculateScore(s, diagnosis)
            ));

            // SORT + FILTER + LIMIT
            suggestions = suggestions.stream()
                    .filter(s -> s.getRelevanceScore() > 0.3)
                    .sorted(Comparator.comparingDouble(ICD10CodeDTO::getRelevanceScore).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

            response.setSuggestions(suggestions);
            response.setSuccess(true);
            response.setMessage(suggestions.size() + " код санал болгосон");

        } catch (Exception e) {
            log.error("Алдаа:", e);
            return buildErrorResponse(response, "Алдаа гарлаа", startTime);
        }

        response.setResponseTime(System.currentTimeMillis() - startTime);
        return response;
    }

    /**
     * VALIDATION
     */
    private boolean isInvalidDiagnosis(String diagnosis) {
        return diagnosis == null
                || diagnosis.trim().length() < 2
                || diagnosis.length() > 100;
    }

    /**
     * LOCAL DB
     */
    private List<ICD10CodeDTO> getLocalResults(String diagnosis) {

        log.debug("Local DB query...");

        return repository.findByKeywordWithLimit(diagnosis)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * REMOVE DUPLICATES
     */
    private List<ICD10CodeDTO> removeDuplicates(List<ICD10CodeDTO> list) {
        return new ArrayList<>(
                list.stream()
                        .collect(Collectors.toMap(
                                ICD10CodeDTO::getCode,
                                x -> x,
                                (a, b) -> a
                        ))
                        .values()
        );
    }

    /**
     * SCORE CALCULATION (search quality)
     */
    private double calculateScore(ICD10CodeDTO code, String query) {

        double score = 0;
        String q = query.toLowerCase();

        if (code.getName() != null && code.getName().toLowerCase().contains(q))
            score += 0.6;

        if (code.getCode() != null && code.getCode().toLowerCase().contains(q))
            score += 0.8;

        if (code.getDetail() != null && code.getDetail().toLowerCase().contains(q))
            score += 0.4;

        return score;
    }

    /**
     * SAVE EXTERNAL RESULTS
     */
    private void saveExternalResults(List<ICD10CodeDTO> externalResults) {

        for (ICD10CodeDTO dto : externalResults) {

            if (repository.findByCode(dto.getCode()).isEmpty()) {

                ICD10Code entity = convertToEntity(dto);
                repository.save(entity);

                log.debug("Saved: {} - {}", entity.getCode(), entity.getName());
            }
        }
    }

    /**
     * ERROR RESPONSE
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
     * ENTITY → DTO
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
     * DTO → ENTITY
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
}
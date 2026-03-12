package mn.num.hospital.icd10_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mn.num.hospital.icd10_service.dto.ICD10CodeDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class ExternalICD10Service {

    private static final double DEFAULT_RELEVANCE = 0.8;

    @Value("${icd10.api.url}")
    private String apiUrl;

    @Value("${icd10.api.timeout}")
    private int timeout;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ExternalICD10Service(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<ICD10CodeDTO> fetchFromExternalAPI(String diagnosis, int limit) {

        log.info("External API calling... URL: {}", apiUrl);

        try {
            String response = restTemplate.getForObject(apiUrl, String.class);

            if (response == null || response.isEmpty()) {
                log.warn("External API returned empty response");
                return List.of();
            }

            JsonNode dataArray = extractDataArray(response);

            if (dataArray == null) {
                return List.of();
            }

            return searchMatchingCodes(dataArray, diagnosis, limit);

        } catch (Exception e) {
            log.error("External API error: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * JSON structure-с array гаргаж авах
     */
    private JsonNode extractDataArray(String response) throws Exception {

        JsonNode root = objectMapper.readTree(response);

        if (root.isArray()) {
            log.debug("Detected direct array JSON structure");
            return root;
        }

        if (root.has("results") && root.get("results").isArray()) {
            log.debug("Detected wrapped JSON structure");
            return root.get("results");
        }

        log.warn("Unknown JSON structure. Root type: {}", root.getNodeType());
        return null;
    }

    /**
     * Keyword matching хийж DTO үүсгэх
     */
    private List<ICD10CodeDTO> searchMatchingCodes(JsonNode dataArray, String diagnosis, int limit) {

        List<ICD10CodeDTO> results = new ArrayList<>();

        Iterator<JsonNode> iterator = dataArray.elements();

        while (iterator.hasNext() && results.size() < limit) {

            JsonNode item = iterator.next();

            ICD10CodeDTO dto = parseItem(item);

            if (dto == null) {
                continue;
            }

            if (isMatch(dto, diagnosis)) {
                results.add(dto);
                log.debug("Matched code: {} - {}", dto.getCode(), dto.getName());
            }
        }

        log.info("External API returned {} matching codes", results.size());

        return results;
    }

    /**
     * JSON item → DTO хөрвүүлэх
     */
    private ICD10CodeDTO parseItem(JsonNode item) {

        try {

            String code = getText(item, "code");
            String name = getText(item, "name");
            String detail = getText(item, "detail");

            if (code.isEmpty() || name.isEmpty()) {
                return null;
            }

            ICD10CodeDTO dto = new ICD10CodeDTO();
            dto.setCode(code);
            dto.setName(name);
            dto.setDetail(detail);
            dto.setCategory("External API");
            dto.setRelevanceScore(DEFAULT_RELEVANCE);

            return dto;

        } catch (Exception e) {
            log.debug("Failed to parse item: {}", e.getMessage());
            return null;
        }
    }

    /**
     * JSON field safely авах
     */
    private String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null ? value.asText() : "";
    }

    /**
     * Keyword match logic
     */
    private boolean isMatch(ICD10CodeDTO dto, String diagnosis) {

        String keyword = diagnosis.toLowerCase();

        return dto.getName().toLowerCase().contains(keyword) ||
               dto.getCode().toLowerCase().contains(keyword);
    }
}
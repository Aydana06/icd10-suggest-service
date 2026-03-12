package mn.num.hospital.icd10_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ICD10CodeDTO {
    private String code;
    private String name;
    private String detail;
    private String category;
    private Double relevanceScore;
}


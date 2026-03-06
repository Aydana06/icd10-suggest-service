package mn.num.hospital.icd10_service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisResponse {
	 private String diagnosis;
	 private boolean success;
	 private String message;
	 private List<ICD10CodeDTO> suggestions;
	 private long responseTime;
}
package mn.num.hospital.icd10_service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "icd10_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ICD10Code {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    @Column(nullable = false, length = 500)
    private String name;
    
    @Column(length = 1000)
    private String detail;
   
    @Column(length = 100)
    private String category;
    
    @Column(columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double relevanceScore;
    
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private java.time.LocalDateTime createdAt;
}
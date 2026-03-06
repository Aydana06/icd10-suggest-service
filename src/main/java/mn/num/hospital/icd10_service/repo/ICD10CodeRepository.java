package mn.num.hospital.icd10_service.repo;

import mn.num.hospital.icd10_service.entity.ICD10Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICD10CodeRepository extends JpaRepository<ICD10Code, Long> {

    Optional<ICD10Code> findByCode(String code);

    @Query("SELECT i FROM ICD10Code i WHERE " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.detailDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY i.relevanceScore DESC")
    List<ICD10Code> searchByKeyword(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM icd10_codes " +
           "WHERE description LIKE CONCAT('%', ?1, '%') " +
           "ORDER BY relevance_score DESC " +
           "LIMIT 2", 
           nativeQuery = true)
    List<ICD10Code> findTopByDescriptionMySQL(String keyword, int limit);

    @Query("SELECT i FROM ICD10Code i WHERE " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY i.relevanceScore DESC")
    List<ICD10Code> findByKeywordWithLimit(@Param("keyword") String keyword);
}
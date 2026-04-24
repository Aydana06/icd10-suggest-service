package mn.num.hospital.icd10_service.repo;

import mn.num.hospital.icd10_service.domain.ICD10Code;
import mn.num.hospital.icd10_service.dto.ChapterDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ICD-10 код хайх repository.
 * Spring Data JPA автоматаар implementation үүсгэнэ.
 */
@Repository
public interface ICD10CodeRepository extends JpaRepository<ICD10Code, Long> {

    /**
     * Кодоор хайна - давхардал шалгахад ашиглагдана.
     * @param code  ICD-10 код - жишээ: "A00.0"
     */
    Optional<ICD10Code> findByCode(String code);

    /**
     * Нэр болон дэлгэрэнгүй тайлбараар keyword хайлт.
     * Хамаарлын оноогоор эрэмблэнэ.
     */
    @Query("SELECT i FROM ICD10Code i WHERE " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.detail) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY i.relevanceScore DESC")
    List<ICD10Code> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Нэрээр keyword хайлт, Pageable-ээр тоо хязгаарлана.
     * suggestCodes() дотор ашиглагдана.
     * COALESCE - relevanceScore null байвал 0 болгоно.
     */
    @Query("SELECT i FROM ICD10Code i WHERE " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY COALESCE(i.relevanceScore, 0) DESC")
    List<ICD10Code> findByKeywordWithLimit(@Param("keyword") String keyword,
                                           Pageable pageable);

    /**
     * Бүх chapter-уудыг DTO болгон буцаана.
     * DiagnosisService.getChapters()-д ашиглагдана.
     * DISTINCT - давхардсан chapter-г нэг болгоно.
     */
    @Query("""
           SELECT DISTINCT new mn.num.hospital.icd10_service.dto.ChapterDto(
               ch.chapter, ch.name, ch.orderIndex
           )
           FROM ICD10Code i
           JOIN i.subcategory s
           JOIN s.category c
           JOIN c.chapter ch
           ORDER BY ch.orderIndex
           """)
    List<ChapterDto> findDistinctChapters();

    /**
     * Chapter код-оор category жагсаалт авна.
     * rangeStart - эрэмблэхэд ашиглах, pipe(|)-оор тусгаарлана.
     * UI-д харуулахдаа pipe-ийн баруун талыг авна.
     *
     * @param chapter  chapter код — жишээ: "I"
     */
    @Query("SELECT DISTINCT CONCAT(c.rangeStart, '|', c.range, ' ', c.name) " +
           "FROM ICD10Code i " +
           "JOIN i.subcategory s " +
           "JOIN s.category c " +
           "JOIN c.chapter ch " +
           "WHERE ch.chapter = :chapter " +
           "ORDER BY CONCAT(c.rangeStart, '|', c.range, ' ', c.name)")
    List<String> findCategoriesByChapter(@Param("chapter") String chapter);

    /**
     * Category range-аар subcategory жагсаалт авна.
     * @param category  range - жишээ: "A00-A09"
     */
    @Query("SELECT DISTINCT CONCAT(s.code, ' ', s.name) " +
           "FROM ICD10Code i " +
           "JOIN i.subcategory s " +
           "JOIN s.category c " +
           "WHERE c.range = :category " +
           "ORDER BY CONCAT(s.code, ' ', s.name)")
    List<String> findSubcategoriesByCategory(@Param("category") String category);
}
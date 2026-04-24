package mn.num.hospital.icd10_service.dto;

import mn.num.hospital.icd10_service.domain.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ICD10Code entity -> ICD10CodeDTO хөрвүүлэлт.
 */
public class ICD10CodeMapper {

    private ICD10CodeMapper() {}

    /**
     * Entity → DTO хөрвүүлэлт.
     * Relational холбоосуудаас (Chapter, Category, Subcategory)
     * шаардлагатай талбаруудыг flat DTO болгон гаргана.
     */
    public static ICD10CodeDTO toDto(ICD10Code entity) {
        if (entity == null) return null;

        ICD10CodeDTO dto = new ICD10CodeDTO();
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDetail(entity.getDetail());
        dto.setRelevanceScore(entity.getRelevanceScore());

        Chapter chapter = entity.getChapter();
        if (chapter != null) {
            dto.setChapterCode(chapter.getChapter());
            dto.setChapterName(chapter.getName());
        }

        Category category = entity.getCategory();
        if (category != null) {
            dto.setCategoryRange(category.getRange());
            dto.setCategoryName(category.getName());
        }

        Subcategory subcategory = entity.getSubcategory();
        if (subcategory != null) {
            dto.setSubcategoryCode(subcategory.getCode());
            dto.setSubcategoryName(subcategory.getName());
        }

        return dto;
    }

    /**
     * DTO → Entity хөрвүүлэлт.
     * Зөвхөн code/name/detail тохируулна.
     * Chapter, Category, Subcategory FK-г service layer-т тохируулна.
     */
    public static ICD10Code toEntity(ICD10CodeDTO dto) {
        if (dto == null) return null;

        ICD10Code entity = new ICD10Code();
        entity.setCode(dto.getCode() != null ? dto.getCode().trim() : null);
        entity.setName(dto.getName() != null ? dto.getName().trim() : null);
        entity.setDetail(dto.getDetail() != null ? dto.getDetail().trim() : "");
        return entity;
    }

    /** Entity жагсаалтыг DTO жагсаалт болгоно */
    public static List<ICD10CodeDTO> toDtoList(List<ICD10Code> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(ICD10CodeMapper::toDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** DTO жагсаалтыг entity жагсаалт болгоно */
    public static List<ICD10Code> toEntityList(List<ICD10CodeDTO> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(ICD10CodeMapper::toEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
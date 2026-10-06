package dev.leo.library.infrastructure.adapter.output.persistence.repository;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.ExamEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamJpaRepository extends JpaRepository<ExamEntity, Long> {

    @Query("SELECT e FROM ExamEntity e WHERE " +
           "(:q IS NULL OR LOWER(e.subject) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(e.classroom) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:active IS NULL OR e.active = :active)")
    Page<ExamEntity> search(@Param("q") String q, @Param("active") Boolean active, Pageable pageable);
}

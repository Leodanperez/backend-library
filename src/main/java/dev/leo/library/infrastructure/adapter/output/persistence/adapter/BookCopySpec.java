package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class BookCopySpec {
    private BookCopySpec() {}

    public static Specification<BookCopyEntity> filter(String q, Long bookId, CopyStatus status, CopyCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), like),
                        cb.like(cb.lower(root.get("location")), like)
                ));
            }
            if (bookId != null)    predicates.add(cb.equal(root.get("book").get("id"), bookId));
            if (status != null)    predicates.add(cb.equal(root.get("status"), status));
            if (condition != null) predicates.add(cb.equal(root.get("condition"), condition));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

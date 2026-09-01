package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class BookSpec {
    private BookSpec() {}

    public static Specification<BookEntity> filter(String q, Long authorId, Long categoryId, String language, Boolean active) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("isbn")), like),
                        cb.like(cb.lower(root.get("publisher")), like)
                ));
            }
            if (authorId != null)   predicates.add(cb.equal(root.get("author").get("id"), authorId));
            if (categoryId != null) predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            if (language != null && !language.isBlank())
                predicates.add(cb.like(cb.lower(root.get("language")), "%" + language.toLowerCase() + "%"));
            if (active != null)     predicates.add(cb.equal(root.get("active"), active));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

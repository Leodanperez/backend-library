package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class AuthorSpec {
    private AuthorSpec() {}

    public static Specification<AuthorEntity> filter(String q, String nationality, Boolean active) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like),
                        cb.like(cb.lower(root.get("email")), like)
                ));
            }
            if (nationality != null && !nationality.isBlank())
                predicates.add(cb.like(cb.lower(root.get("nationality")), "%" + nationality.toLowerCase() + "%"));
            if (active != null)
                predicates.add(cb.equal(root.get("active"), active));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

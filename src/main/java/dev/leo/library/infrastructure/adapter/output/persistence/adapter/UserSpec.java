package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.domain.model.UserRole;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class UserSpec {
    private UserSpec() {}

    public static Specification<UserEntity> filter(String q, UserRole role, Boolean active) {
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
            if (role != null)   predicates.add(cb.equal(root.get("role"), role));
            if (active != null) predicates.add(cb.equal(root.get("active"), active));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

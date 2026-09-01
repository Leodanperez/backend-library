package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class LoanSpec {
    private LoanSpec() {}

    public static Specification<LoanEntity> filter(Long userId, Long bookCopyId, Long loanStatusId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null)       predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (bookCopyId != null)   predicates.add(cb.equal(root.get("bookCopy").get("id"), bookCopyId));
            if (loanStatusId != null) predicates.add(cb.equal(root.get("loanStatus").get("id"), loanStatusId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

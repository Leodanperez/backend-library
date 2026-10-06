package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.ExamRequest;
import dev.leo.library.application.dto.response.ExamResponse;
import dev.leo.library.shared.dto.PaginatedResponse;

public interface ExamUseCase {
    PaginatedResponse<ExamResponse> findAll(String q, Boolean active, int page, int perPage);
    ExamResponse findById(Long id);
    ExamResponse save(ExamRequest dto);
    void update(Long id, ExamRequest dto);
    void activate(Long id);
    void deactivate(Long id);
    void delete(Long id);
}

package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.AuthorRequest;
import dev.leo.library.application.dto.response.AuthorResponse;
import dev.leo.library.application.dto.response.SelectOptionsResponse.SelectItem;
import dev.leo.library.shared.dto.PaginatedResponse;
import java.util.List;

public interface AuthorUseCase {
    PaginatedResponse<AuthorResponse> findAll(String q, String nationality, Boolean active, int page, int perPage);
    List<SelectItem> findAllForSelect();
    AuthorResponse findById(Long id);
    AuthorResponse save(AuthorRequest dto);
    void update(Long id, AuthorRequest dto);
    void activate(Long id);
    void deactivate(Long id);
    void delete(Long id);
}

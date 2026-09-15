package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.response.SelectOptionsResponse;
import dev.leo.library.domain.port.input.AuthorUseCase;
import dev.leo.library.domain.port.input.BookCopyUseCase;
import dev.leo.library.domain.port.input.BookUseCase;
import dev.leo.library.domain.port.input.CategoryUseCase;
import dev.leo.library.domain.port.input.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/selects")
@RequiredArgsConstructor
public class SelectController {

    private final UserUseCase userUseCase;
    private final AuthorUseCase authorUseCase;
    private final BookUseCase bookUseCase;
    private final BookCopyUseCase bookCopyUseCase;
    private final CategoryUseCase categoryUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public SelectOptionsResponse getAll() {
        return new SelectOptionsResponse(
                userUseCase.findAllForSelect(),
                authorUseCase.findAllForSelect(),
                bookUseCase.findAllForSelect(),
                bookCopyUseCase.findAllForSelect(),
                categoryUseCase.findAllActive()
        );
    }
}

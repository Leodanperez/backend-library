package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.BookRequest;
import dev.leo.library.domain.exception.BookNotFoundException;
import dev.leo.library.domain.port.input.BookUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.adapter.BookSpec;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService implements BookUseCase {

    private final BookJpaRepository repository;
    private final AuthorService authorService;
    private final CategoryService categoryService;

    @Override
    public PaginatedResponse<BookEntity> findAll(String q, Long authorId, Long categoryId, String language, Boolean active, int page, int perPage) {
        Page<BookEntity> result = repository.findAll(
                BookSpec.filter(q, authorId, categoryId, language, active),
                PageRequest.of(page - 1, perPage, Sort.by("title").ascending())
        );
        return PaginatedResponse.of(result.getContent(), page, perPage, result.getTotalElements());
    }

    @Override
    public BookEntity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @Override
    @Transactional
    public BookEntity save(BookRequest dto) {
        if (dto.isbn() != null && repository.existsByIsbn(dto.isbn()))
            throw new IllegalStateException("El ISBN ya está registrado: " + dto.isbn());
        return repository.save(BookEntity.builder()
                .title(dto.title()).isbn(dto.isbn()).description(dto.description())
                .publicationYear(dto.publicationYear()).pages(dto.pages())
                .language(dto.language()).publisher(dto.publisher()).coverUrl(dto.coverUrl())
                .author(authorService.findById(dto.authorId()))
                .category(categoryService.findById(dto.categoryId()))
                .active(true).build());
    }

    @Override
    @Transactional
    public BookEntity update(Long id, BookRequest dto) {
        BookEntity book = findById(id);
        if (dto.isbn() != null && !dto.isbn().equals(book.getIsbn()) && repository.existsByIsbn(dto.isbn()))
            throw new IllegalStateException("El ISBN ya está registrado: " + dto.isbn());
        book.setTitle(dto.title()); book.setIsbn(dto.isbn()); book.setDescription(dto.description());
        book.setPublicationYear(dto.publicationYear()); book.setPages(dto.pages());
        book.setLanguage(dto.language()); book.setPublisher(dto.publisher()); book.setCoverUrl(dto.coverUrl());
        book.setAuthor(authorService.findById(dto.authorId()));
        book.setCategory(categoryService.findById(dto.categoryId()));
        return repository.save(book);
    }

    @Override
    @Transactional
    public BookEntity activate(Long id) {
        BookEntity book = findById(id);
        book.setActive(true);
        return repository.save(book);
    }

    @Override
    @Transactional
    public BookEntity deactivate(Long id) {
        BookEntity book = findById(id);
        book.setActive(false);
        return repository.save(book);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }
}

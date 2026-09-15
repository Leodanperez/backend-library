package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.BookRequest;
import dev.leo.library.application.dto.response.BookCatalogResponse;
import dev.leo.library.application.dto.response.BookDetailResponse;
import dev.leo.library.application.dto.response.BookResponse;
import dev.leo.library.application.dto.response.BookCopyResponse;
import dev.leo.library.application.dto.response.SelectOptionsResponse.SelectItem;
import dev.leo.library.domain.exception.BookNotFoundException;
import dev.leo.library.domain.port.input.BookUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.adapter.BookSpec;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookCopyJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService implements BookUseCase {

    private final BookJpaRepository repository;
    private final BookCopyJpaRepository bookCopyRepository;
    private final LoanJpaRepository loanRepository;
    private final AuthorService authorService;
    private final CategoryService categoryService;

    @Override
    public List<SelectItem> findAllForSelect() {
        return repository.findAll(Sort.by("title").ascending()).stream()
                .filter(BookEntity::isActive)
                .map(b -> new SelectItem(b.getId(), b.getTitle()))
                .toList();
    }

    @Override
    public PaginatedResponse<BookResponse> findAll(String q, Long authorId, Long categoryId, String language, Boolean active, int page, int perPage) {
        Page<BookEntity> result = repository.findAll(
                BookSpec.filter(q, authorId, categoryId, language, active),
                PageRequest.of(page - 1, perPage, Sort.by("title").ascending())
        );
        return PaginatedResponse.of(result.getContent().stream().map(BookResponse::from).toList(), page, perPage, result.getTotalElements());
    }

    @Override
    public PaginatedResponse<BookCatalogResponse> searchCatalog(String q, Long authorId, Long categoryId, String language, int page, int perPage) {
        Page<BookEntity> result = repository.findAll(
                BookSpec.filter(q, authorId, categoryId, language, true),
                PageRequest.of(page - 1, perPage, Sort.by("title").ascending())
        );
        List<Long> ids = result.getContent().stream().map(BookEntity::getId).toList();
        Set<Long> availableIds = ids.isEmpty() ? Set.of() : repository.findBookIdsWithAvailableCopies(ids);
        List<BookCatalogResponse> mapped = result.getContent().stream()
                .map(b -> BookCatalogResponse.from(BookResponse.from(b), availableIds.contains(b.getId()))).toList();
        return PaginatedResponse.of(mapped, page, perPage, result.getTotalElements());
    }

    @Override
    public BookDetailResponse detailCatalog(Long id) {
        BookEntity book = findEntityById(id);
        List<BookCopyEntity> copies = bookCopyRepository.findByBookId(id);
        List<Long> copyIds = copies.stream().map(BookCopyEntity::getId).toList();
        Set<Long> requestedIds = copyIds.isEmpty() ? Set.of() : new HashSet<>(loanRepository.findRequestedCopyIds(copyIds));
        List<BookCopyResponse> copyResponses = copies.stream().map(BookCopyResponse::from).toList();
        return BookDetailResponse.from(BookResponse.from(book), copyResponses, requestedIds);
    }

    @Override
    public Set<Long> findAvailableBookIds(List<Long> bookIds) {
        return repository.findBookIdsWithAvailableCopies(bookIds);
    }

    public BookEntity findEntityById(Long id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @Override
    public BookResponse findById(Long id) {
        return BookResponse.from(findEntityById(id));
    }

    @Override
    @Transactional
    public BookResponse save(BookRequest dto) {
        if (dto.isbn() != null && repository.existsByIsbn(dto.isbn()))
            throw new IllegalStateException("El ISBN ya está registrado: " + dto.isbn());
        return BookResponse.from(repository.save(BookEntity.builder()
                .title(dto.title()).isbn(dto.isbn()).description(dto.description())
                .publicationYear(dto.publicationYear()).pages(dto.pages())
                .language(dto.language()).publisher(dto.publisher()).coverUrl(dto.coverUrl())
                .author(authorService.findEntityById(dto.authorId()))
                .category(categoryService.findEntityById(dto.categoryId()))
                .active(true).build()));
    }

    @Override
    @Transactional
    public void update(Long id, BookRequest dto) {
        BookEntity book = findEntityById(id);
        if (dto.isbn() != null && !dto.isbn().equals(book.getIsbn()) && repository.existsByIsbn(dto.isbn()))
            throw new IllegalStateException("El ISBN ya está registrado: " + dto.isbn());
        book.setTitle(dto.title()); book.setIsbn(dto.isbn()); book.setDescription(dto.description());
        book.setPublicationYear(dto.publicationYear()); book.setPages(dto.pages());
        book.setLanguage(dto.language()); book.setPublisher(dto.publisher()); book.setCoverUrl(dto.coverUrl());
        book.setAuthor(authorService.findEntityById(dto.authorId()));
        book.setCategory(categoryService.findEntityById(dto.categoryId()));
        repository.save(book);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        BookEntity book = findEntityById(id);
        book.setActive(true);
        repository.save(book);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        BookEntity book = findEntityById(id);
        book.setActive(false);
        repository.save(book);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findEntityById(id));
    }
}

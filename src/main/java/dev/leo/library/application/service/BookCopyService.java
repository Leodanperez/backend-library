package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.BookCopyRequest;
import dev.leo.library.application.dto.response.BookCopyResponse;
import dev.leo.library.application.dto.response.SelectOptionsResponse.SelectItem;
import dev.leo.library.domain.exception.BookCopyNotFoundException;
import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.domain.port.input.BookCopyUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.adapter.BookCopySpec;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookCopyJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCopyService implements BookCopyUseCase {

    private final BookCopyJpaRepository repository;
    private final BookService bookService;

    @Override
    public List<SelectItem> findAllForSelect() {
        return repository.findAll(Sort.by("code").ascending()).stream()
                .map(bc -> new SelectItem(bc.getId(), bc.getCode() + " — " + bc.getBook().getTitle()))
                .toList();
    }

    @Override
    public PaginatedResponse<BookCopyResponse> findAll(String q, Long bookId, CopyStatus status, CopyCondition condition, int page, int perPage) {
        Page<BookCopyEntity> result = repository.findAll(
                BookCopySpec.filter(q, bookId, status, condition),
                PageRequest.of(page - 1, perPage, Sort.by("code").ascending())
        );
        return PaginatedResponse.of(result.getContent().stream().map(BookCopyResponse::from).toList(), page, perPage, result.getTotalElements());
    }

    @Override
    public BookCopyEntity findEntityById(Long id) {
        return repository.findById(id).orElseThrow(() -> new BookCopyNotFoundException(id));
    }

    @Override
    public BookCopyResponse findById(Long id) {
        return BookCopyResponse.from(findEntityById(id));
    }

    @Override
    @Transactional
    public BookCopyResponse save(BookCopyRequest dto) {
        if (repository.existsByCode(dto.code()))
            throw new IllegalStateException("El código del ejemplar ya existe: " + dto.code());
        return BookCopyResponse.from(repository.save(BookCopyEntity.builder()
                .book(bookService.findEntityById(dto.bookId())).code(dto.code())
                .status(dto.status() != null ? dto.status() : CopyStatus.AVAILABLE)
                .condition(dto.condition()).acquisitionDate(dto.acquisitionDate())
                .price(dto.price()).location(dto.location()).build()));
    }

    @Override
    @Transactional
    public void update(Long id, BookCopyRequest dto) {
        BookCopyEntity copy = findEntityById(id);
        if (!dto.code().equals(copy.getCode()) && repository.existsByCode(dto.code()))
            throw new IllegalStateException("El código del ejemplar ya existe: " + dto.code());
        copy.setCode(dto.code());
        if (dto.status() != null) copy.setStatus(dto.status());
        if (dto.condition() != null) copy.setCondition(dto.condition());
        copy.setAcquisitionDate(dto.acquisitionDate());
        copy.setPrice(dto.price());
        copy.setLocation(dto.location());
        repository.save(copy);
    }

    @Override
    @Transactional
    public void markAsLost(Long id) {
        BookCopyEntity copy = findEntityById(id);
        if (copy.getStatus() == CopyStatus.LOST)
            throw new IllegalStateException("El ejemplar ya está marcado como perdido");
        copy.setStatus(CopyStatus.LOST);
        repository.save(copy);
    }

    @Override
    @Transactional
    public void markAsDamaged(Long id, CopyCondition condition) {
        BookCopyEntity copy = findEntityById(id);
        if (copy.getStatus() == CopyStatus.LOANED)
            throw new IllegalStateException("No se puede marcar como dañado un ejemplar que está prestado, primero debe ser devuelto");
        copy.setStatus(CopyStatus.DAMAGED);
        copy.setCondition(condition != null ? condition : CopyCondition.DAMAGED);
        repository.save(copy);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        BookCopyEntity copy = findEntityById(id);
        if (copy.getStatus() != CopyStatus.DAMAGED && copy.getStatus() != CopyStatus.LOST)
            throw new IllegalStateException("Solo se pueden restaurar ejemplares en estado DAÑADO o PERDIDO");
        copy.setStatus(CopyStatus.AVAILABLE);
        copy.setCondition(CopyCondition.FAIR);
        repository.save(copy);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BookCopyEntity copy = findEntityById(id);
        if (copy.getStatus() == CopyStatus.LOANED)
            throw new IllegalStateException("No se puede eliminar un ejemplar que está prestado");
        repository.delete(copy);
    }
}

package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.CategoryRequest;
import dev.leo.library.application.dto.response.CategorySelectResponse;
import dev.leo.library.domain.exception.CategoryNotFoundException;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.CategoryEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.CategoryJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryJpaRepository repository;

    @InjectMocks
    private CategoryService service;

    private CategoryEntity category;
    private CategoryRequest request;

    @BeforeEach
    void setUp() {
        category = CategoryEntity.builder().id(1L).name("Fiction").description("Fiction books").active(true).build();
        request = new CategoryRequest("Fiction", "Fiction books");
    }

    @Test
    void findAll_returnsPagedResponse() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(category)));

        PaginatedResponse<CategoryEntity> result = service.findAll(null, null, 1, 10);

        assertThat(result.data()).hasSize(1);
        assertThat(result.total()).isEqualTo(1);
    }

    @Test
    void findAllActive_returnsMappedList() {
        when(repository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(category));

        List<CategorySelectResponse> result = service.findAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Fiction");
    }

    @Test
    void findById_returnsCategory_whenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(category));

        CategoryEntity result = service.findById(1L);

        assertThat(result.getName()).isEqualTo("Fiction");
    }

    @Test
    void findById_throwsCategoryNotFoundException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void save_createsCategory_whenNameNotDuplicated() {
        when(repository.existsByName("Fiction")).thenReturn(false);
        when(repository.save(any(CategoryEntity.class))).thenReturn(category);

        CategoryEntity result = service.save(request);

        assertThat(result.getName()).isEqualTo("Fiction");
        verify(repository).save(any(CategoryEntity.class));
    }

    @Test
    void save_throwsIllegalStateException_whenNameAlreadyExists() {
        when(repository.existsByName("Fiction")).thenReturn(true);

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El nombre de categoría ya existe");
    }

    @Test
    void update_updatesCategory_whenNameNotTaken() {
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.existsByNameAndIdNot("Fiction", 1L)).thenReturn(false);
        when(repository.save(any(CategoryEntity.class))).thenReturn(category);

        CategoryEntity result = service.update(1L, request);

        assertThat(result).isNotNull();
        verify(repository).save(category);
    }

    @Test
    void update_throwsIllegalStateException_whenNameTakenByOther() {
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.existsByNameAndIdNot("Fiction", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El nombre de categoría ya existe");
    }

    @Test
    void activate_setsActiveTrue() {
        category.setActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.save(category)).thenReturn(category);

        CategoryEntity result = service.activate(1L);

        assertThat(result.isActive()).isTrue();
    }

    @Test
    void deactivate_setsActiveFalse() {
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.save(category)).thenReturn(category);

        CategoryEntity result = service.deactivate(1L);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void delete_callsRepositoryDelete_whenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(category));

        service.delete(1L);

        verify(repository).delete(category);
    }
}

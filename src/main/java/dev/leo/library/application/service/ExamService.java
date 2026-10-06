package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.ExamRequest;
import dev.leo.library.application.dto.response.ExamResponse;
import dev.leo.library.domain.exception.ExamNotFoundException;
import dev.leo.library.domain.port.input.ExamUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.ExamEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.ExamJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamService implements ExamUseCase {

  private final ExamJpaRepository repository;

  @Override
  public PaginatedResponse<ExamResponse> findAll(String q, Boolean active, int page, int perPage) {
    Page<ExamEntity> result = repository.search(q, active,
        PageRequest.of(page - 1, perPage, Sort.by("date").descending()));
    return PaginatedResponse.of(result.getContent().stream().map(ExamResponse::from).toList(),
        page, perPage, result.getTotalElements());
  }

  @Override
  public ExamResponse findById(Long id) {
    return ExamResponse.from(findEntityById(id));
  }

  @Override
  @Transactional
  public ExamResponse save(ExamRequest dto) {
    ExamEntity saved = repository.save(ExamEntity.builder()
        .subject(dto.subject())
        .date(dto.date())
        .classroom(dto.classroom())
        .durationMinutes(dto.durationMinutes())
        .active(true).build());
    return ExamResponse.from(saved);
  }

  @Override
  @Transactional
  public void update(Long id, ExamRequest dto) {
    ExamEntity exam = findEntityById(id);
    exam.setSubject(dto.subject());
    exam.setDate(dto.date());
    exam.setClassroom(dto.classroom());
    exam.setDurationMinutes(dto.durationMinutes());
    repository.save(exam);
  }

  @Override
  @Transactional
  public void activate(Long id) {
    ExamEntity exam = findEntityById(id);
    exam.setActive(true);
    repository.save(exam);
  }

  @Override
  @Transactional
  public void deactivate(Long id) {
    ExamEntity exam = findEntityById(id);
    exam.setActive(false);
    repository.save(exam);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    repository.delete(findEntityById(id));
  }

  private ExamEntity findEntityById(Long id) {
    return repository.findById(id).orElseThrow(() -> new ExamNotFoundException(id));
  }
}

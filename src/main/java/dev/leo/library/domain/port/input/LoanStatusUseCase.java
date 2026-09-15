package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.response.LoanStatusResponse;
import java.util.List;

public interface LoanStatusUseCase {
    List<LoanStatusResponse> findAll();
}

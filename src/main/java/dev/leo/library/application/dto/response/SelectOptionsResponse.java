package dev.leo.library.application.dto.response;

import java.util.List;

public record SelectOptionsResponse(
        List<SelectItem> users,
        List<SelectItem> authors,
        List<SelectItem> books,
        List<SelectItem> bookCopies,
        List<SelectItem> categories
) {
    public record SelectItem(Long id, String label) {}
}

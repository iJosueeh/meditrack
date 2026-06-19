package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.domain.services.search.BuscarGlobalUseCase;
import com.utp.meditrackapp.features.search.models.SearchResult;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcSearchRepository;
import java.util.List;

/**
 * Adaptador para TopbarController.
 * Puentea el GlobalSearchDAO antiguo con el nuevo BuscarGlobalUseCase.
 */
public class SearchAdapter {
    private final BuscarGlobalUseCase useCase;

    public SearchAdapter() {
        this.useCase = new BuscarGlobalUseCase(new JdbcSearchRepository());
    }

    public List<SearchResult> searchGlobal(String query) {
        return useCase.ejecutar(query);
    }
}

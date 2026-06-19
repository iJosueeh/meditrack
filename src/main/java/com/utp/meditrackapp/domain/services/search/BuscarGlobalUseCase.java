package com.utp.meditrackapp.domain.services.search;

import com.utp.meditrackapp.domain.ports.out.SearchRepository;
import com.utp.meditrackapp.features.search.models.SearchResult;
import java.util.List;

/**
 * Caso de uso para busqueda global.
 */
public class BuscarGlobalUseCase {
    private final SearchRepository searchRepository;

    public BuscarGlobalUseCase(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public List<SearchResult> ejecutar(String query) {
        return searchRepository.searchGlobal(query);
    }
}

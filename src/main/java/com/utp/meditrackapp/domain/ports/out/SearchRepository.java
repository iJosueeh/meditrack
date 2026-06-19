package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.features.search.models.SearchResult;
import java.util.List;

/**
 * Puerto de salida para busqueda global.
 */
public interface SearchRepository {
    List<SearchResult> searchGlobal(String query);
}

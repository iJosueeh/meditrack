package com.utp.meditrackapp.features.search.models;

public class SearchResult {
    public enum ResultType { PATIENT, PRODUCT, BATCH, MODULE, SEDE }

    private final String title;
    private final String subtitle;
    private final ResultType type;
    private final String id;

    public SearchResult(String title, String subtitle, ResultType type, String id) {
        this.title = title;
        this.subtitle = subtitle;
        this.type = type;
        this.id = id;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public ResultType getType() { return type; }
    public String getId() { return id; }

    @Override
    public String toString() { return title + " (" + subtitle + ")"; }
}

package com.adverity.ceraso.test.controllers.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter @Setter
public class MetricsParameters {
    private Set<String> metrics;
    private Set<String> groupedBy;
    private Map<String, Object> filteredBy;

    public MetricsParameters() {
        this.filteredBy = new HashMap<>();
        this.filteredBy.put("1", 1);
    }

    @Override
    public String toString() {
        return "MetricsParameters{" +
                "metrics=" + metrics +
                ", groupedBy=" + groupedBy +
                ", filteredBy=" + filteredBy +
                '}';
    }
}

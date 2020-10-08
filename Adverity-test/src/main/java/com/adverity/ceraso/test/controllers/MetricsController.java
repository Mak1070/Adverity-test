package com.adverity.ceraso.test.controllers;

import com.adverity.ceraso.test.controllers.requests.MetricsParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controller to answer fpr the REST calls
 * regarding the metrics in the database.
 */
@RestController
public class MetricsController {
    private static final Logger log = LoggerFactory.getLogger(MetricsController.class);

    @Autowired
    private JdbcTemplate template;

    @ResponseBody
    @PostMapping(value = "/metrics")
    public List<Map<String, Object>> metricsByParameters(@RequestBody MetricsParameters parameters) throws Exception {
        if (parameters.getMetrics() == null || parameters.getMetrics().isEmpty()) {
            throw new MissingServletRequestParameterException("metrics", "Set<String>");
        }

        StringBuilder selectBuilder = new StringBuilder();
        parameters.getMetrics().forEach((metric) -> {
            if (selectBuilder.length() > 0) {
                selectBuilder.append(",");
            }
            if (metric.equals("clicks") || metric.equals("impressions")) {
                selectBuilder.append("SUM(").append(metric).append(") AS ").append(metric);
            } else if (metric.equals("ctr")) {
                selectBuilder.append("SUM(clicks)/SUM(impressions) AS CTR");
            }
        });

        StringBuilder groupByBuilder = new StringBuilder();
        if (parameters.getGroupedBy() != null && !parameters.getGroupedBy().isEmpty()) {
            parameters.getGroupedBy().forEach((param) -> {
                if (groupByBuilder.length() > 0) {
                    groupByBuilder.append(",");
                }
                selectBuilder.append(",").append(param);
                groupByBuilder.append(param);
            });
        }

        return template.queryForList( String.format("SELECT %s FROM metric m WHERE %s GROUP BY %s",
                selectBuilder.toString(),
                turnMapIntoWhereClause(parameters.getFilteredBy()),
                groupByBuilder.length() > 0 ? groupByBuilder.toString() : "null"));
    }

    private String turnMapIntoWhereClause(Map<String, Object> whereMap) {
        StringBuilder builder = new StringBuilder();

        whereMap.forEach((param, value) -> {
            if (builder.length() > 0) {
                builder.append(" AND ");
            }

            builder.append(param).append("=");

            if (value instanceof String) {
                builder.append("'").append(value).append("'");
            } else {
                builder.append(value);
            }
        });

        return builder.toString();
    }
}

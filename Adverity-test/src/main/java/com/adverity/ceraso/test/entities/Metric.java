package com.adverity.ceraso.test.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.Objects;

/**
 * Class that represents the 'metric' table
 * in the database.
 */
@Getter @Setter
@NoArgsConstructor
public class Metric {

    private Long id;
    private String datasource;
    private String campaign;
    private Date daily;
    private Integer clicks;
    private Integer impressions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metric metric = (Metric) o;
        return datasource.equals(metric.datasource) &&
                campaign.equals(metric.campaign) &&
                daily.equals(metric.daily);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datasource, campaign, daily);
    }

    @Override
    public String toString() {
        return String.format("Metric{datasource='%s', " +
                        "campaign='%s', " +
                        "daily='%s', " +
                        "impressions=%d}",
                        datasource, campaign,
                        daily, impressions);
    }
}

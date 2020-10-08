package com.adverity.ceraso.test.configs;

import com.adverity.ceraso.test.entities.Metric;
import com.mysql.cj.jdbc.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple configuration for the db client
 * to use in the Main class.
 */
@Configuration
public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    public CommandLineRunner initDatabase(JdbcTemplate template) {
        if (template.queryForObject("SELECT count(*) from metric", Integer.class) == 0) {
            loadCSVFileIntoRepository(template);
            return args -> log.info("CSV loaded");
        }

        return args -> log.info("CSV already loaded");
    }

    @Bean
    public JdbcTemplate template() {
        SimpleDriverDataSource ds = new SimpleDriverDataSource();
        ds.setUrl("jdbc:mysql://localhost:3400/test_db");
        ds.setDriverClass(Driver.class);
        ds.setUsername("root");
        ds.setPassword("root");

        return new JdbcTemplate(ds);
    }

    private void loadCSVFileIntoRepository(JdbcTemplate template) {
        List<Metric> importedMetrics = new ArrayList<>();
        String fileURL = "http://adverity-challenge.s3-website-eu-west-1.amazonaws.com/PIxSyyrIKFORrCXfMYqZBI.csv";

        try (InputStreamReader in = new InputStreamReader(new URL(fileURL).openStream());
             BufferedReader reader = new BufferedReader(in)) {
            Map<Integer, Method> positionToSetter = getSettersMap(reader.readLine());
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    Metric newMetric = getMetricFromLine(line, positionToSetter);
                    importedMetrics.add(newMetric);
                } catch (Exception e) {
                    log.error(String.format("Error transforming line '%s':", line), e);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Can't download the CSV file:", e);
        }

        if (!importedMetrics.isEmpty()) {
            insertDataInTheDB(template, importedMetrics);
        }
    }

    private void insertDataInTheDB(JdbcTemplate template, List<Metric> data) {
        template.batchUpdate("INSERT INTO metric (datasource, campaign, daily, clicks, impressions) " +
                "VALUES (?,?,?,?,?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Metric metricToInsert = data.get(i);
                ps.setString(1, metricToInsert.getDatasource());
                ps.setString(2, metricToInsert.getCampaign());
                ps.setDate(3, metricToInsert.getDaily());
                ps.setInt(4, metricToInsert.getClicks());
                ps.setInt(5, metricToInsert.getImpressions());
            }

            @Override
            public int getBatchSize() {
                return data.size();
            }
        });
    }

    private Map<Integer, Method> getSettersMap(String headersLine) {
        Method[] methods = Metric.class.getMethods();
        String[] headers = headersLine.split(",");
        Map<Integer, Method> positionToSetter = new HashMap<>(headers.length);

        for (Integer position = 0; position < headers.length; position++) {
            for (Method method : methods) {
                if (method.getName().equals("set" + headers[position])) {
                    positionToSetter.put(position, method);
                }
            }
        }

        return positionToSetter;
    }

    private Metric getMetricFromLine(String line, Map<Integer, Method> positionToSetter) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        String[] data = line.split(",");
        Metric metric = new Metric();

        for (Integer position = 0; position < positionToSetter.size(); position++) {
            Method setter = positionToSetter.get(position);
            Class<?> valueClass = setter.getParameterTypes()[0];
            Object value;

            if (valueClass.equals(Date.class)) {
                value = Date.valueOf(LocalDate.parse(data[position], formatter));
            } else if (valueClass.equals(Integer.class)) {
                value = Integer.valueOf(data[position]);
            } else {
                value = valueClass.cast(data[position]);
            }

            setter.invoke(metric, value);
        }

        return metric;
    }
}

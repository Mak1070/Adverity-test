CREATE TABLE IF NOT EXISTS metric (
    _id             bigint AUTO_INCREMENT PRIMARY KEY,
    datasource      varchar(50) NOT NULL,
    campaign        varchar(50) NOT NULL,
    daily           date NOT NULL,
    clicks          int NOT NULL,
    impressions     int NOT NULL
);
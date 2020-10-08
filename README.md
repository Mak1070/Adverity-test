# Adverity-test

The test works very simple concepts:
1. Downloads the CSV file from URL;
2. Load it in the database;
3. Run the Rest controller.

Technologies used: Spring/Spring Boot (RestController, JdbcTemplate), Docker, Maven, Java 11

The database is a MySQL one loaded with Docker compose.

The API exposes only the POST endpoint "/metrics".
It expects in the body of the POST to have at least the "metrics" set of data.
As extra can be specified the 2 sets "groupedBy" and "filteredBy".

Metrics available: "clicks", "impressions", "CTR".

I didn't have so much time to dedicate so I implemented not the most elegant solution but it makes the job done.

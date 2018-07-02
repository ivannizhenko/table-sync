# table-sync
Synchronizer for Main and Mirror tables, assuming both have similar schema.

Uses Spring scheduler to periodically scan Main and Mirror tables row by row, compares row pairs, and updates out-of-dated ones with newer data. Internally, uses stored hash to decide which row is newer.

Please look in Synchronizer class for main logic.
Please look in `resources/application.yml` to configure synchronization schedule interval and table names.
Please look in `resources/db/migration/V1__init.sql` for initial schema definition.

Please run this app with maven: `./mvnw clean spring-boot:run`

Or you can build the JAR file with: `./mvnw clean package -DskipTests`

Then you can run the JAR file as: `java -jar target/table-sync-0.0.1-SNAPSHOT.jar`
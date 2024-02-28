(./)mvnw.cmd clean install

java -Xmx1G -jar target\TestTaskJob-1.0-SNAPSHOT.jar big-data.csv

The algorithm was created for grouping large data (over 100,000 rows) - limit 1 GB

Example:

- "1";"2";""
- "1";"2";""
- "3";"2";""
- "4";"6";"";"2";""
- "4";"3";"2"
- "";"";"2"

Output:

Group 1
- "1";"2";""
- "3";"2";""

Group 2
- "4";"6";"";"2";""
- "4";"3";"2"
- "";"";"2"

Duplicate values are removed, matching only non-empty strings.
- Group 1 - common "2" in the first and second line
- Group 2 - common "4" in the first and second line and "2" in the second and third line

# GameSales

Prerequisite tool required to run this project code 

- Java 21 JRE/JDK is required for java run time. (To support latest springboot 3 and above)
- Intellij 2024 or 2023 is required for Java 21 version 
- MySql V8.0 is used for development and testing 


Before you run the Project 

- Update application.properties for the MySql Connection url and credential for your database 
	- spring.datasource.url (please retain the rewriteBatchedStatements=true for the performance purpose)
	- spring.datasource.username
	- spring.datasource.password




During project startup, it will auto generated a csv file with 1millions record, the data are randomly generated based on the format given, may use it for your testing on the /import endpoint.

Before importing csv with the postman collection given, please update the file path for the csv to upload the file.
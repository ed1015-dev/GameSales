# GameSales

Prerequisite tool required to run this project code 

- Java 21 JRE/JDK is required for java run time. (To support latest springboot 3 and above)
- Intellij 2024 or 2023 is required for Java 21 version 
- MySql V8.0 is used for development and testing 
- Postman 2.1 (for testing purpose)

Before you run the Project 

- Update application.properties (GameSales -> src -> resources) for the MySql Connection url and credential for your database 
	- spring.datasource.url (please retain the rewriteBatchedStatements=true for the performance purpose)
	- spring.datasource.username
	- spring.datasource.password


Step to run 

1. Git Clone this via Intellij ( Menu -> New -> Project from version Control)
2. input the github link given and directory that you prefer for the project setup folder. 
3. Setup this project load as Maven project. 
4. go to file GameSalesApplication (GameSales -> src -> main -> java -> com -> gamesales).
5. Hit the run function on Intellij to start up the application
6. Once application is up and running, may import the postman collection and hit the api endpoint for testing.


During project startup, it will auto generated a csv file with 1millions record, the data are randomly generated based on the format given, may use it for your testing on the /import endpoint.

Before importing csv with the postman collection given, please update the file path for the csv to upload the file.
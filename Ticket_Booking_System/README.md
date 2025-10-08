# Ticket_Booking_System

## Setup Instructions

### 1. Install Apache Maven
Download and install Apache Maven:  
[https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip](https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip)

### 2. Extract Maven
Extract the ZIP file to:
``` C:\Program Files\Apache\Maven ```

After extraction, ensure you have this directory:
``` C:\Program Files\Apache\Maven\apache-maven-3.9.11\bin  ```


### 3. Set Environment Variables
1. Open **System Properties** → **Environment Variables**
2. Under **System variables**, find and select **Path**, then click **Edit**
3. Add a new path:


### 4. Reload VS Code
Restart Visual Studio Code to apply the updated environment variables.

### 5. Confirm Installation
Open a new **terminal** or **command prompt** and verify both Java and Maven are installed correctly:

```bash
java -version
mvn -version
```
---

## Run the Project

In your VS Code terminal, execute:
``` mvn clean javafx:run ```

Default Users
| Role  | Username | Password |
| ----- | -------- | -------- |
| User  | user1    | password |
| Admin | admin    | admin123 |


Note: This project is not fully perfect due to time constraints. It focuses on covering the 
core requirements outlined in the instructions. 
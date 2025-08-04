# Use an official Maven image to build the app
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set working directory inside the container
WORKDIR /app

# Copy only the pom.xml and install dependencies (this leverages Docker cache)
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Now copy the rest of the source code
COPY src ./src

# Build the project and create the JAR file
RUN mvn clean install -DskipTests

# --------- RUNTIME STAGE ---------
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the JAR from the previous build stage
COPY --from=build /app/target/*.jar app.jar

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]

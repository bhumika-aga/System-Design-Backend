# FROM picks the base image, the first (bottom) layer. Pin a version, never :latest
FROM eclipse-temurin:21-jdk

# WORKDIR sets the working dir for the instructions that follow (and at runtime)
WORKDIR /app

# Copy the build files FIRST and warm the dependency cache, so this layer
# survives ordinary source edits (sec 7)
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline

# Now copy the source and build the jar
COPY src src
RUN ./mvnw clean package -DskipTests

# Document the port the app listens on. Metadata only; publishing
# happens at run time (sec 8)
EXPOSE 8080

# CMD is the default command run when a container starts from this image
CMD ["java", "-jar", "target/app.jar"]

# Official Maven slim image for builder:
FROM maven:3.6-jdk-11-slim as builder

# Copy code to container image
WORKDIR /app
COPY pom.xml .
COPY swagger.json .
COPY src ./src

# Build the release artifact
RUN mvn package -DskipTests

# Use AdoptOpenJDK for the final execute image.
# This uses a 2-stage build so the final image only needs JRE, not JDK.
FROM adoptopenjdk:11-jre-hotspot

COPY --from=builder /app/target/image_captioner*.jar /image_captioner.jar

CMD ["/bin/bash", "-c", "java -jar /image_captioner.jar --server.port=$PORT"]
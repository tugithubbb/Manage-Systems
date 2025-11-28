
# Stage 1: Build
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
#skip task test
RUN gradle clean bootJar -x test --no-daemon

# Stage 2: Run
FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

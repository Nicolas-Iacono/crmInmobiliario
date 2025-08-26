FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# cache de dependencias
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

# código
COPY . .

# (defensa ante BOM/CRLF, por si tu editor metió algo raro)
RUN sed -i '1s/^\xEF\xBB\xBF//' src/main/resources/application.properties || true
RUN apt-get update && apt-get install -y dos2unix && dos2unix src/main/resources/application.properties || true

# build
RUN mvn -Dfile.encoding=UTF-8 clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Dfile.encoding=UTF-8","-jar","app.jar"]
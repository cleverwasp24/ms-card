FROM openjdk:17-oracle
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ms-card.jar
ENTRYPOINT ["java","-jar","/ms-card.jar"]
FROM openjdk:17-oracle
ADD target/ms-card-0.0.1-SNAPSHOT.jar ms-card.jar
ENTRYPOINT ["java","-jar","ms-card.jar"]
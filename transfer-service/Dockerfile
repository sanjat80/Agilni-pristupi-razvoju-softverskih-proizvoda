FROM openjdk:17
EXPOSE 8700
ADD target/transfer-service.jar transfer-service.jar
ENTRYPOINT ["java","-jar","/transfer-service.jar"]
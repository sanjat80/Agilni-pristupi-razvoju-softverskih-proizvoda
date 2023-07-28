FROM openjdk:17
EXPOSE 8770
ADD target/users-service.jar users-service.jar
ENTRYPOINT ["java","-jar","/users-service.jar"]
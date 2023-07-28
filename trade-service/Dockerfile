FROM openjdk:17
EXPOSE 8600
ADD target/trade-service.jar trade-service.jar
ENTRYPOINT ["java","-jar","/trade-service.jar"]
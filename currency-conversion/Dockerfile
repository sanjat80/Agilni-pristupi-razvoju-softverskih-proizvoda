FROM openjdk:17
EXPOSE 8100
ADD target/currency-conversion.jar currency-conversion.jar
ENTRYPOINT ["java","-jar","/currency-conversion.jar"]
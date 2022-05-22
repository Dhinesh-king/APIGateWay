FROM openjdk:8
EXPOSE 9090
ADD target/APIgateway.jar APIgateway.jar
ENTRYPOINT ["java","-jar","/APIgateway.jar"]
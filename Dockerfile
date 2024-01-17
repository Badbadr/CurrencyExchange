FROM azul/zulu-openjdk:20.0.2-20.32.11-jre
COPY target/currency-0.0.1-SNAPSHOT.jar /
ENTRYPOINT ["java", "-jar", "/currency-0.0.1-SNAPSHOT.jar"]

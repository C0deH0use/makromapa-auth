FROM openjdk:14.0.1-oraclelinux7

ARG api_version

ENV API_VERSION=${api_version}

COPY build/libs/makromapa-auth.jar /

EXPOSE 9090

ENTRYPOINT [ "java", "-XX:MaxDirectMemorySize=800M", "-jar", "/makromapa-auth.jar" ]

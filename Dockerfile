FROM openjdk:17-oraclelinux8

ARG COMMIT_SHA='0.0.0'

ENV CI_COMMIT_SHA=$COMMIT_SHA

COPY build/libs/makromapa-auth.jar /

EXPOSE 9090

ENTRYPOINT [ "java", "--illegal-access=permit", "-jar", "/makromapa-auth.jar" ]

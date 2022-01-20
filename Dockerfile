FROM openjdk:17-oraclelinux8

ARG commit_short_sha

ENV CI_COMMIT_SHA=${commit_short_sha}

COPY build/libs/makromapa-auth.jar /

EXPOSE 9090

ENTRYPOINT [ "java", "--illegal-access=permit", "-jar", "/makromapa-auth.jar" ]

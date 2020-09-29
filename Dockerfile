FROM openjdk:14.0.1-oraclelinux7

ARG commit_short_sha

ENV CI_COMMIT_SHA=${commit_short_sha}

COPY build/libs/makromapa-auth.jar /

EXPOSE 9090

ENTRYPOINT [ "java", "-jar", "/makromapa-auth.jar" ]

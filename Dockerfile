FROM gradle:jdk21-corretto AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle :automessagerbot:build --no-daemon

FROM amazoncorretto:21.0.0
COPY --from=build /home/gradle/src/automessagerbot/build/libs/automessagerbot-1.0.jar /automessagerbot/automessagerbot.jar
COPY --from=build /home/gradle/src//automessagerbot/build/libs/lib /automessagerbot/lib
WORKDIR /automessagerbot
EXPOSE 8080
ENTRYPOINT ["java","-jar","automessagerbot.jar"]
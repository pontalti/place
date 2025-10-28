FROM gradle:jdk25 AS build
USER gradle
WORKDIR /home/app
COPY --chown=gradle:gradle build.gradle* settings.gradle* gradlew /home/app/
COPY --chown=gradle:gradle gradle /home/app/gradle
COPY --chown=gradle:gradle src /home/app/src
COPY --chown=gradle:gradle src /home/app/src
COPY --chown=gradle:gradle src /home/app/src
RUN ./gradlew clean build --refresh-dependencies --no-daemon

FROM openjdk:25-jdk
LABEL Gustavo Pontalti
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000
WORKDIR /app
COPY --from=build /home/app/build/libs/place.jar place.jar
EXPOSE 8080 8000
CMD java -jar place.jar
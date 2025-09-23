# ======== STAGE 1: BUILD ========
FROM openjdk:25-jdk-slim AS build
RUN apt-get update && apt-get install -y --no-install-recommends wget unzip ca-certificates \
  && rm -rf /var/lib/apt/lists/*
ENV GRADLE_VERSION=9.1.0
ENV GRADLE_HOME=/opt/gradle
ENV PATH="${GRADLE_HOME}/bin:${PATH}"
RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip \
 && unzip -q gradle-${GRADLE_VERSION}-bin.zip \
 && mv gradle-${GRADLE_VERSION} "${GRADLE_HOME}" \
 && rm gradle-${GRADLE_VERSION}-bin.zip
RUN useradd -ms /bin/bash gradle
USER gradle
WORKDIR /home/app
COPY --chown=gradle:gradle build.gradle* settings.gradle* gradlew /home/app/
COPY --chown=gradle:gradle gradle /home/app/gradle
COPY --chown=gradle:gradle src /home/app/src
RUN gradle clean build --refresh-dependencies --no-daemon

# ======== STAGE 2: RUNTIME ========
FROM azul/zulu-openjdk:25-jre
LABEL maintainer="Gustavo Pontalti"
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000"
WORKDIR /app
COPY --from=build /home/app/build/libs/place.jar /app/place.jar
EXPOSE 8080 8000
CMD ["java", "-jar", "place.jar"]
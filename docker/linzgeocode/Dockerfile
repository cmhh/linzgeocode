FROM ghcr.io/graalvm/graalvm-ce:ol9-java11-22.3.1

COPY target/scala-2.13/linzgeocode.jar linzgeocode.jar

EXPOSE 9001

CMD ["java", "-Dconfig.file=./conf/application.conf", "-cp", "linzgeocode.jar", "org.cmhh.linzgeocode.Service"]

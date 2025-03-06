FROM openjdk:21
COPY ./target/*.jar /home/githubactions.jar
ENTRYPOINT java -jar /home/githubactions.jar
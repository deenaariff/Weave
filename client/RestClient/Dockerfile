FROM anapsix/alpine-java

COPY resources/Weave.jar /home/target/Weave.jar
COPY resources/nodes.xml /home/target/nodes.xml

EXPOSE 8080
EXPOSE 8081
EXPOSE 8082
CMD ["java","-jar","/home/target/Weave.jar","i","/home/target/nodes.xml"]
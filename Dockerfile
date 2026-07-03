FROM tomcat:11.0.21-jre21-temurin
RUN mkdir $HOME/.vip

ARG MARIADB_HOST
ARG MARIADB_DATABASE
ARG MARIADB_USER
ARG MARIADB_PASSWORD

ADD docker/tomcat/context.xml /usr/local/tomcat/conf/context.xml

ADD docker/tomcat/setenv.sh /tmp/setenv.sh
RUN /tmp/setenv.sh

CMD ["catalina.sh", "run"]

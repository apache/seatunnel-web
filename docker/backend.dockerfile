FROM openjdk:8-jre-alpine

ENV DOCKER true
ENV TZ Asia/Shanghai
ENV SEATUNNEL_WEB_HOME /opt/app/seatunnel-web

WORKDIR $SEATUNNEL_WEB_HOME

ADD ../seatunnel-server/seatunnel-app/target/seatunnel-web/ $SEATUNNEL_WEB_HOME/

EXPOSE 8080

CMD [ "/bin/sh", "/opt/app/seatunnel-web/bin/seatunnel-backend-daemon.sh", "start" ]
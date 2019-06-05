FROM maven:3.6.1-jdk-8
LABEL MAINTAINER kevin.luy@antfin.com,xiangfeng.xzc@antfin.com

ENV deploy_path /home/admin/deploy


COPY . $deploy_path/source


RUN mkdir -p /root/.m2 && \
  cd $deploy_path/source && \
  mv docker/settings-aliyun.xml /root/.m2/settings.xml && \
  mv docker/entrypoint.sh $deploy_path/entrypoint.sh && \
  chmod a+x $deploy_path/entrypoint.sh && \
  mvn clean package -Psofaark -B -e -T 1C -Dsun.jnu.encoding=UTF-8 -Dfile.encoding=UTF-8 -Dmaven.test.skip=true -am -pl boot/all-in-one-bootstrap && \
  mv $deploy_path/source/boot/all-in-one-bootstrap/target/allinone-executable.jar $deploy_path/app.jar && \
  cd ~ && \
  rm -rf /root/.m2 $deploy_path/source && \
  useradd admin && \
  chown -R admin:admin /home/admin

EXPOSE 6200 7200 9090

VOLUME /home/admin/logs /home/admin/lookout_gateway_queue_cache

USER admin
WORKDIR /home/admin

ENTRYPOINT ["/home/admin/deploy/entrypoint.sh"]

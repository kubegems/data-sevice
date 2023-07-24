#/usr/bin/env bash
docker_image_name=dataservice-common
docker build . --no-cache -t harbor.cloudminds.com/bigdata/${docker_image_name}:$1
docker push harbor.cloudminds.com/bigdata/${docker_image_name}:$1

#!/bin/bash
 
### Openshift deploy script
### Add version as parameter
sudo podman run -it --privileged --name sensor-service localhost/quarkus/rest-client-jvm

sudo podman inspect sensor-service | grep \"IPAddress\"

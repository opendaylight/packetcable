#!/bin/sh


NOW=$(date +"%Y%m%d-%H%M")

DIRECTORY="../../controller/opendaylight/distribution/opendaylight/target/distribution.opendaylight-0.1.1-SNAPSHOT-osgipackage/opendaylight/plugins/"
if [ -d "$DIRECTORY" ]; then
  echo "Copied plugin to  $DIRECTORY"
  cp target/protocol_plugins.packetcable-0.5.0-SNAPSHOT.jar $DIRECTORY
fi
DIRECTORY="../../distribution/opendaylight/target/distribution.opendaylight-osgipackage/opendaylight/plugins/"
if [ -d "$DIRECTORY" ]; then
  echo "Copied plugin to  $DIRECTORY"
  cp target/protocol_plugins.packetcable-0.5.0-SNAPSHOT.jar $DIRECTORY
fi
DIRECTORY="../../plugin.archive/protocol_plugins.packetcable-0.5.0-SNAPSHOT.$NOW.jar"
if [ -d "$DIRECTORY" ]; then
  echo "Copied plugin to  $DIRECTORY"
  cp target/protocol_plugins.packetcable-0.5.0-SNAPSHOT.jar $DIRECTORY
fi


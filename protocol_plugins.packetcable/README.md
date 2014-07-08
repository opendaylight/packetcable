### Checkout the Controller and Plugin


    sudo apt-get install maven
    git clone https://git.opendaylight.org/gerrit/p/controller.git

### XXX - CHANGE THIS

    git clone  https://github.com/xsited/packetcable.git

### Directory Organization

- in the parent is pom.xml for the entire packetcable projects.
- this will build a working controller distribution
  based on the controller + packetcable modules
  packetcable/target/protocol_plugins.packetcable-0.5.0-SNAPSHOT.jar
- md-sal experimental
- docs for PoC documentation and notes and PCMM specification version I05
 

### Replacing OpenJDK on Ubuntu

    apt-get purge -y openjdk*
    add-apt-repository ppa:webupd8team/java
    apt-get update
    apt-get install -y oracle-java7-installer
    apt-get --no-install-recommends install -y maven


### How to Build 


JDK 1.7+ and Maven 3+ are required:


From the toplevel issue the following instructions to build the controller:

    cd controller/opendaylight/distribution/opendaylight

    export JAVA_HOME=/usr
    mvn clean install


or if you want to avoid SNAPSHOT checking use: 

    mvn clean install -nsu
    // mvn clean install -DskipTests 


From the toplevel issue the following instructions to build the packetcable SB plugin:

    cd packetcable
    mvn clean install

From the toplevel issue the following instructions to build the packetcable model:

    cd packetcable/md-sal 
    mvn clean install


### How to Run


Upon successful completion of a build install and run from the toplevel:

    ./copy.sh
    export JAVA_HOME=/usr
    ./run.sh

Wait for the osgi console to startup and then point a browser at 

    http://localhost:8080/


From the osgi console, verify the plugin is active

    osgi> ss | grep packetcable
    110	ACTIVE      org.opendaylight.controller.protocol_plugins.packetcable_0.4.0.SNAPSHOT true

    osgi> dm 110
    [110] org.opendaylight.controller.protocol_plugins.packetcable
      org.opendaylight.controller.sal.flowprogrammer.IPluginInFlowProgrammerService(protocolPluginType=PC) registered
      org.opendaylight.controller.sal.utils.INodeFactory(protocolName=PC,protocolPluginType=PC) registered
      org.opendaylight.controller.sal.utils.INodeConnectorFactory(protocolName=PC,protocolPluginType=PC) registered
      org.opendaylight.controller.sal.inventory.IPluginInInventoryService(scope=Global,protocolPluginType=PC) registered
      org.opendaylight.controller.sal.inventory.IPluginOutInventoryService (scope=Global) service required available
      org.opendaylight.controller.sal.reader.IPluginInReadService(protocolPluginType=PC,containerName=default) registered
      org.opendaylight.controller.sal.inventory.IPluginInInventoryService(protocolPluginType=PC,containerName=default) registered


    osgi> s | grep packetcable
    110	file:/home/mininet/controller/opendaylight/distribution/opendaylight/target/distribution.opendaylight-0.1.1-SNAPSHOT-osgipackage/opendaylight/plugins/protocol_plugins.packetcable-0.4.0-SNAPSHOT.jar
      ACTIVE      org.opendaylight.controller.protocol_plugins.packetcable_0.4.0.SNAPSHOT [110] true
      
    osgi> 


### Acknowledgements and Contributions


The project development lead from
Thomas Kee (xsited@yahoo.com)

The fantastic Java stylings and support from
Riadh HAJ AMOR (rhadjamor@gmail.com)

The COPS for Java foundation from
Copyright (c) 2004 University of Murcia.  All rights reserved.
For more information, please see <http://www.umu.euro6ix.org/>.
Félix Jesús García Clemente  (fgarcia@dif.um.es)


COMP3100 Peter Wu 44890826

Usage
To run MyClient you will need to:

Have java and git installed and use Ubuntu

Clone ds-sim
git clone https://github.com/distsys-MQ/ds-sim.git

Clone MyClient
git clone https://github.com/PeterWuMQ/comp3100Project.git

Download test suite
https://ilearn.mq.edu.au/pluginfile.php/7733545/mod_resource/content/20/S1Demo.tar

Move the test suite to a new folder named S1Demo and unpack it. You can unpack it with the following script:
tar -xvf S1Demo.tar

Copy ds-server and ds-client from ./ds-sim/src/pre-compiled into ./S1Demo

In the folder comp3100Project open a terminal and compile MyClient:
javac MyClient.java

Move MyClient.class and MyClient$Server.class from ./comp3100Project into ./S1Demo

In ./S1Demo open a terminal and execute the following script:
./demoS1.sh -n MyClient.class

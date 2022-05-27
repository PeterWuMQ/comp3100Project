COMP3100 Peter Wu 44890826

Usage
To run MyClient you will need to:

Have java and git installed and use Ubuntu

Clone ds-sim
git clone https://github.com/distsys-MQ/ds-sim.git

Clone MyClient
git clone https://github.com/PeterWuMQ/comp3100Project.git

Download test suite
https://ilearn.mq.edu.au/pluginfile.php/7733550/mod_resource/content/9/stage2-test.zip 

Move the test suite to a new folder named S2Demo and unzip it. You can unzip it with the following script:
tar -unzip stage2-test.zip 

Copy ds-server from ./ds-sim/src/pre-compiled into ./S2Demo

In the folder comp3100Project open a terminal and compile MyClient:
javac javac stage2/*.java

Copy stage2 folder from ./comp3100Project into ./S2Demo

In ./S2Demo open a terminal and execute the following scripts:
chmod u+rwx stage2-test-x86 
./stage2-test-x86 "java stage2.MyClient -a baf" -o tt -n

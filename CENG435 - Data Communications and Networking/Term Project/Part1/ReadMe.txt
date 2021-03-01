
Name: Orçun Başşimşek
ID: 2098804

Name: Murat Doğan
ID: 2188498

Initial Note: To run each script, Java Compiler and Java Virtual Machine must be installed on nodes.

Part 1-) Running Discovery Scripts:

There are five discovery scripts. Firstly compile scripts with the following commands:

On node D:
javac UDPServerNodeD.java

On node R3:
javac UDPServerNodeR3.java

On node R2:
javac UDPServerNodeR2.java

On node R1:
javac UDPServerNodeR1.java

On node S:
javac UDPServerNodeS.java


Then, please run the scripts in this order:

Firstly, on Node D, execute UDPServerNodeD script with: 
java UDPServerNodeD

Secondly, on Node R3, execute UDPServerNodeR3 script with: 
java UDPServerNodeR3

Thirdly, on Node R2, execute UDPServerNodeR2 script with: 
java UDPServerNodeR2

Fourthly, on Node R1, execute UDPServerNodeR1 script with: 
java UDPServerNodeR1

And lastly, on Node S, execute UDPServerNodeS script with:
java UDPServerNodeS


After executing last script on node S, messaging process is started.
You will see informative prints on the screen while messaging is continuing. 
At the end, average of RTT times printed on the screen.
You can also reach this information from generated "link_cost(X-Y).txt" files after running the scripts on the corresponding nodes.

-----------------------------------------------------------

Part 2-) Running Experiment Scripts: 

In our implementation scenario to calculate end-to-end delays from node S to node D, node S saves sendTime information itself and gets receiveTime information that is saved in node D for each message among the (S-R3-D) shortest path.
Then, in node S, we simply calculate end-to-end delay as receiveTime-sendTime.
We used System.currentTimeMillis() method of Java to calculate times.
Thus, system times of node S and node D must be synchronized firstly. 
For this reason, we followed the guide in this link "https://www.tecmint.com/install-ntp-server-and-client-on-ubuntu/". 
According to this guide, we created NTP Server on node S, and NTP Client on node D. 
And, system time of Node D will be synchronized with system time of Node S at the end. 
Therefore, we could use System.currentTimeMillis() method thanks to these configurations for our scenario.

There are three experiment scripts. 
Since we again used 1000 messages approach for experiment, it may take some time to wait the normal termination of each scripts. 
Again you will see informative prints on the screen about the end-to-end delay informations of each messages.
At the end, node S prints the average of end-to-end delays on the screen. 
By the way, we did not discard packet losses for both RTT calculations at discover part, and end-to-end calculations of this experiment part.

Note: If you do not want to wait the normal termination of scripts for each step, you can use CTRL-Z command of course.
However, if you want to execute the scripts again, you can get the error because open UDP sockets that were not closed normally in our programs.
To get rid of this error, you can check these open sockets with "netstat -tulpn" command and kill them with "kill -9 PID" command. 

Firstly compile scripts with the following commands:

On node D:
javac UDPNodeD.java

On node R3:
javac UDPNodeR3.java

On node S:
javac UDPNodeS.java


Experiment STEP 1:

Please run this command on each of node S, R3 and D:
> sudo tc qdisc add dev eth0 root netem delay 20ms 5ms distribution normal

Then, please run the scripts in this order:

Firstly, on Node D, execute UDPNodeD script with: 
java UDPNodeD

Secondly, on Node R3, execute UDPNodeR3 script with: 
java UDPNodeR3

And lastly, on Node S, execute UDPNodeS script with: 
java UDPNodeS


Experiment STEP 2:

Please run these commands on each of node S, R3 and D:
> sudo tc qdisc del dev eth0
> sudo tc qdisc add dev eth0 root netem delay 40ms 5ms distribution normal

Then, please run the scripts in this order:

Firstly, on Node D, execute UDPNodeD script with: 
java UDPNodeD

Secondly, on Node R3, execute UDPNodeR3 script with: 
java UDPNodeR3

And lastly, on Node S, execute UDPNodeS script with: 
java UDPNodeS


Experiment STEP 3:

Please use this command on each of node S, R3 and D:
> sudo tc qdisc del dev eth0
> sudo tc qdisc add dev eth0 root netem delay 50ms 5ms distribution normal

Then, please run the scripts in this order:

Firstly, on Node D, execute UDPNodeD script with: 
java UDPNodeD

Secondly, on Node R3, execute UDPNodeR3 script with: 
java UDPNodeR3

And lastly, on Node S, execute UDPNodeS script with: 
java UDPNodeS













 

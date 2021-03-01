
Initial Note: In my scripts, I use hostname string of the nodes as it is to send packet to the nodes.
For example, to send packet to R1 node, I use "r1.61tp.ch-geni-net.instageni.cenic.net" hostname string which is taken from my GENI slice.
As you see, my slice name "61tp" is included in this string. Thus, my scripts are strictly dependent to my 61tp slice.
If you try to run my scripts on different slice, even if they will compile, they will not work unfortunately.

Part 1-)

You need to use the scripts in the exp1 folder for this part.

On node S: You need to put MainSenderNodeS.java, CheckSum.java and Packet.java scripts to this node.
Then, compile scripts with the followind command:
javac *.java

On node R3: You need to put NodeR3.java and Packet.java scripts to this node.
Then, compile scripts with the followind command:
javac *.java 

On node D: You need to put MainReceiverNodeD.java, CheckSum.java and Packet.java scripts to this node.
Then, compile scripts with the followind command:
javac *.java


Then, please run the scripts in this order:

Firstly, on node D, execute MainReceiverNodeD script with:
java MainReceiverNodeD

Secondly, on node R3, execute NodeR3 script with:
java NodeR3

And finally, on node S, execute MainSenderNodeS script with:
java MainSenderNodeS  

After executing last script on node S, transmission of file is started.
You will see informative prints on the screen while transmission is continuing.
At the end, you can check the transmitted file ("output1.txt") at receiver node D.


---------------------------------------------------------------------------------------------

EXPERIMENT:

Step 1:
Please use following commands in order for nodes S,R3 and D:

> sudo tc qdisc del dev eth0 root
> sudo tc qdisc add dev eth0 root netem delay 3ms
> sudo tc qdisc change dev eth0 root netem loss 5% delay 3ms

Then, you can run the scripts as explained above.


Step 2:
Please use following commands in order for nodes S,R3 and D:

> sudo tc qdisc del dev eth0 root
> sudo tc qdisc add dev eth0 root netem delay 3ms
> sudo tc qdisc change dev eth0 root netem loss 15% delay 3ms

Then, you can run the scripts as explained above.


Step 3:
Please use following commands in order for nodes S,R3 and D:

> sudo tc qdisc del dev eth0 root
> sudo tc qdisc add dev eth0 root netem delay 3ms
> sudo tc qdisc change dev eth0 root netem loss 38% delay 3ms

Then, you can run the scripts as explained above.



---------------------------------------------------------------------------------------------

Important Node for part 2: My part 2 scripts which contain multi-homing and link-failure detection do not work as intended.
The code starts to working as intended, but at some point it congests. Probably, I could not implement correct multi-threading,
or I could not catch just small errors, or my approach is totally wrong. 

Part 2-)

You need to use the scripts in the exp2 folder for this part.

On node S: You need to put MainSenderNodeS.java, CheckSum.java and Packet.java scripts to this node.
Then, compile scripts with the followind command:
javac *.java

On node R1: You need to put NodeR1.java and Packet.java scripts to this node.
Then, compile scripts with the followind command:
javac *.java

On node R2: You need to put NodeR2.java and Packet.java scripts to this node.
Then, compile scripts with the followind command:
javac *.java 

On node D: You need to put MainReceiverNodeD.java, CheckSum.java and Packet.java scripts to this node.
Then, compile scripts with the followind command:
javac *.java


Then, please run the scripts in this order:

Firstly, on node D, execute MainReceiverNodeD script with:
java MainReceiverNodeD

Secondly, on node R2, execute NodeR2 script with:
java NodeR2

Thirdly, on node R1, execute NodeR1 script with:
java NodeR1

And finally, on node S, execute MainSenderNodeS script with:
java MainSenderNodeS  

After executing last script on node S, transmission of file is started.
You will see informative prints on the screen while transmission is continuing.
However, unfortunately, code will congest after some point and will not terminate properly.
For this reason, I could not conduct any experiment for part 2.






 

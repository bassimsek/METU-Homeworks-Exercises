#ifndef __RECEIVER_H
#define __RECEIVER_H

#include "monitor.h"




class Receiver {
	int receiverID;
	int speedOfReceiver;
	int assignedHubIDOfReceiver;

	

public:

	
    Receiver(int ID, int sR, int hR) {
    	receiverID = ID;
    	speedOfReceiver = sR;
    	assignedHubIDOfReceiver = hR;

    }




    int getReceiverId() {
    	//__synchronized__;
    	return receiverID;
    }


    int getassignedHubIdOfReceiver() {
    	//__synchronized__;
    	return assignedHubIDOfReceiver;
    }

    int getSpeedOfReceiver() {
    	//__synchronized__
    	return speedOfReceiver;
    }




   
};

#endif
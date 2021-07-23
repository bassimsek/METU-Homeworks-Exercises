#ifndef __SENDER_H
#define __SENDER_H

#include "monitor.h"




class Sender: public Monitor {
	int senderID;
	int speedOfSender;
	int assignedHubIDOfSender;
	int totalPackageOfSender;

    Condition cv1;
	
	bool isProducing;


    

public:

	
    Sender(int ID, int sS, int hS, int tS) : cv1(this) {
    	senderID = ID;
    	speedOfSender = sS;
    	assignedHubIDOfSender = hS;
    	totalPackageOfSender = tS;
    	isProducing = true;


        
    }


    void waitUntilPackageDeposited() {
        __synchronized__;
        cv1.wait();
    }

    void notifyForDeposit() {
        __synchronized__;
        cv1.notify();
    }


    int getSenderId() {
        __synchronized__;
        return senderID;
    }


    int getSpeedOfSender() {
        __synchronized__;
        return speedOfSender;
    }

    int getassignedHubIDOfSender() {
        __synchronized__;
        return assignedHubIDOfSender;
    }

    int getTotalPackageOfSender() {
    	__synchronized__;
    	return totalPackageOfSender;
    }


    void stopProducing() {
        __synchronized__;
        isProducing = false;
    }

    void notifyForSenderQuit() {
        __synchronized__;
        cv1.notify();
    }

   


    bool getIsProducing() {
    	__synchronized__;
    	return isProducing;
    }
};

#endif

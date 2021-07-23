#ifndef __DRONE_H
#define __DRONE_H

#include "monitor.h"




class Drone: public Monitor {
	int droneID;
	int speedOfDrone;
	int startingHubIdOfDrone;
	int maximumRangeOfDrone;

	
	Condition cv1, cv2;

	bool isRequested;
	bool isAssignedFromCurrentHub;

	PackageInfo currentPackage;
	int comingRequestHubId;

	int currentRangeOfDrone;
	int currentHubIdOfDrone;


   

public:


	
    Drone(int ID, int sD, int hD, int rD) : cv1(this), cv2(this) {
    	droneID = ID;
    	speedOfDrone = sD;
    	startingHubIdOfDrone = hD;
    	maximumRangeOfDrone = rD;

    	isRequested = false;
    	isAssignedFromCurrentHub = false;

    	currentRangeOfDrone = maximumRangeOfDrone;
    	currentHubIdOfDrone = startingHubIdOfDrone;

        
    }


    // drone waits
    void waitTheSignal() {
    	__synchronized__;
    	cv2.wait();
    }


    



    // by drone and hub
    bool getIsRequested() {
    	__synchronized__;
    	return isRequested;
    }


    // by drone and hub
    bool getIsAssignedFromCurrentHub() {
    	__synchronized__;
    	return isAssignedFromCurrentHub;
    }

	

    // hub requests
    void droneRequest(int hubId) {
    	__synchronized__;
    	comingRequestHubId = hubId;
    	isRequested = true;
    	cv2.notify();
    }


    // hub notifies
    void assignDrone(PackageInfo package) {
    	__synchronized__;
    	currentPackage = package;
    	isAssignedFromCurrentHub = true;
    	cv2.notify();
    }


    // by drone
    void clearPackageAssignment() {
    	__synchronized__;
    	isAssignedFromCurrentHub = false;
        
    }


    // by drone
    void clearRequestFromAnotherHub() {
    	__synchronized__;
    	isRequested = false;
    }
    

    void notifyTheHubQuit() {
        __synchronized__;
        cv2.notify();
    }






    int getDroneId() {
		__synchronized__;
		return droneID;
	}


	int getSpeedOfDrone() {
		__synchronized__;
		return speedOfDrone;
	}


	int getStartingHubIdOfDrone() {
		__synchronized__;
		return startingHubIdOfDrone;
	}

	int getMaximumRangeOfDrone() {
		__synchronized__;
		return maximumRangeOfDrone;
	}


	// BY DRONE
	PackageInfo getCurrentPackage() {
		__synchronized__;
		return currentPackage;
	}

	int getComingRequestHubId() {
		__synchronized__;
		return comingRequestHubId;
	}

	int getCurrentRange() {
		__synchronized__;
		return currentRangeOfDrone;
	}

	int getCurrentHubId() {
		__synchronized__;
		return currentHubIdOfDrone;
	}


	void setCurrentRange(int currentRange) {
		__synchronized__;
		currentRangeOfDrone = currentRange;
	}

	void setCurrentHubId(int currentHubId) {
		__synchronized__;
		currentHubIdOfDrone = currentHubId;
	}



    
};

#endif

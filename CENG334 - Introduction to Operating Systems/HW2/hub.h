#ifndef __HUB_H
#define __HUB_H

#include "monitor.h"




class Hub: public Monitor  {
    int hubID;
    int inPackStorageOfHub;
    int outPackStorageOfHub;
    int chargeSpaceOfHub;
    std::vector<int> distancesToOtherHubs;

    
    Condition cv2;

    bool isActive;
    

    std::vector<PackageInfo> inStorage;
    std::vector<PackageInfo> outStorage;
    
    std::vector<DroneInfo> currentDrones; 


   

public:

    Hub(int ID, int iH, int oH, int cH, std::vector<int> distances) : cv2(this) {

        hubID = ID;
        inPackStorageOfHub = iH;
        outPackStorageOfHub = oH;
        chargeSpaceOfHub = cH;
        distancesToOtherHubs = distances;
        isActive = true;

        

    }


    
   


    // by sender
    void putPackageOnOutgoingStorage(PackageInfo packageInfo) {
        __synchronized__;

        outStorage.emplace_back(packageInfo);
       
    }


    

    
    bool getIsActive() {
        __synchronized__;
        return isActive;
    }



    // by receiver
    PackageInfo getPackageFromIncomingStorage(int receiverId) {
        __synchronized__;
        PackageInfo package;

        package.sender_id = inStorage.back().sender_id;
        package.sending_hub_id = inStorage.back().sending_hub_id;
        package.receiver_id = inStorage.back().receiver_id;
        package.receiving_hub_id = inStorage.back().receiving_hub_id;


        //package = inStorage.back();


        inStorage.pop_back();

        return package;
        
        
    }




   


    // by drone and hub
    std::vector<int> getDistancesToOtherHubs() {
        __synchronized__;
        return distancesToOtherHubs;
    }


  



   

    // by drone
    void removeDroneFromHub(int droneId) {
        __synchronized__;
        
        for(int i=0;i<currentDrones.size();i++) {
            if (currentDrones[i].id == droneId) {
                currentDrones.erase(currentDrones.begin()+i);
                 
                break;
            }
        }
        
    }



    // by drone
    void putPackageOnIncomingStorage(PackageInfo packageInfo) {
        __synchronized__;
        inStorage.emplace_back(packageInfo);
        
    }




     void addDroneToNewHub(DroneInfo drone) {
        __synchronized__;
        currentDrones.push_back(drone);
       
    }




    void notifyTheArrival() {
        __synchronized__;
        cv2.notify();
    }

   
    


   


    bool isIncomingStorageEmpty() {
        __synchronized__;
        if (inStorage.size() <= 0) {
            return true;
        } else {
            return false;
        }
    }


    bool isOutgoingStorageEmpty() {
        __synchronized__;
        if (outStorage.size() <= 0) {
            return true;
        } else {
            return false;
        }
    }




   


    int getDronesSize() {
        __synchronized__;
        return currentDrones.size();
    }


    int selectDroneWithHighestCurrentRange() {
        __synchronized__;
        int highestRangedDroneId = -1;
        int highestRange = -1;
        for(int i=0;i<currentDrones.size();i++) {
            if ((currentDrones[i].current_range) > highestRange) {
                highestRange = currentDrones[i].current_range;
                highestRangedDroneId = currentDrones[i].id;
            }
        }
        return highestRangedDroneId;
    }



    // by hub
    PackageInfo getPackageFromOutgoingStorage() {
        __synchronized__;
        PackageInfo package;
        
        package.sender_id = outStorage.back().sender_id;
        package.sending_hub_id = outStorage.back().sending_hub_id;
        package.receiver_id = outStorage.back().receiver_id;
        package.receiving_hub_id = outStorage.back().receiving_hub_id;
        outStorage.pop_back();


        return package;
        
    }



    std::vector<int> getCurrentDroneIds() {
        __synchronized__;
        std::vector<int> droneIds;
        for(int i=0;i<currentDrones.size();i++) {
            droneIds.emplace_back(currentDrones[i].id);
        
        }
        
        return droneIds;
    }


    void waitForTheDroneArrival() {
        __synchronized__;
        cv2.wait();
    }

    

    




    void deactivate() {
        __synchronized__;
        isActive = false;
    }



    int getHubId() {
        __synchronized__;
        return hubID;
    }

    

    int getinPackStorageOfHub() {
        __synchronized__;
        return inPackStorageOfHub;
    }

    int getoutPackStorageOfHub() {
        __synchronized__;
        return outPackStorageOfHub;
    }


    
};

#endif

#include "writeOutput.h"
#include "helper.h"
#include "monitor.h"

#include "hub.h"
#include "sender.h"
#include "receiver.h"
#include "drone.h"

#include <semaphore.h>
#include <stdlib.h>
#include <algorithm>






std::vector<Hub*> hubs;
std::vector<Sender*> senders;
std::vector<Receiver*> receivers;
std::vector<Drone*> drones;

pthread_mutex_t hubMutex =  PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t senderMutex =  PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t receiverMutex =  PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t droneMutex =  PTHREAD_MUTEX_INITIALIZER;




// HUB SEMs
std::vector<sem_t> semInStorageSpaceForHubs;
std::vector<sem_t> semOutStorageSpaceForHubs;
std::vector<sem_t> semChargeSpaceForHubs;
std::vector<sem_t> semChargeOccupiedSpaceForHubs;



// SENDER SEMs
std::vector<sem_t> semGetPackageFromSender;



// RECEIVER SEMs
std::vector<sem_t> semGivePackageToReceiver;


// DRONE SEMs
std::vector<sem_t> semAssignmentFromCurrentHub;
std::vector<sem_t> semRequestFromNearbyHub;
std::vector<sem_t> semCurrentDronePackage;



// HUB MUTEXes
std::vector<pthread_mutex_t> inStorageMutexes;
std::vector<pthread_mutex_t> outStorageMutexes;
std::vector<pthread_mutex_t> chargeStorageMutexes;
std::vector<pthread_mutex_t> distancesMutexes;


// DRONE MUTEXes
std::vector<pthread_mutex_t> droneRequestFromDifferentHubMutexes;
std::vector<pthread_mutex_t> droneAssignmentMutexes;
std::vector<pthread_mutex_t> dronePackageMutexes;
std::vector<pthread_mutex_t> droneComingRequestsMutexes;



// SENDER MUTEXes
std::vector<pthread_mutex_t> senderActiveMutexes;









void waitDestinationSpace(int hubId) {
	sem_wait(&semInStorageSpaceForHubs[hubId-1]);
	sem_wait(&semChargeSpaceForHubs[hubId-1]); 
}

void waitAndReserveChargingSpace(int hubId) {
	sem_wait(&semChargeSpaceForHubs[hubId-1]);
}



bool isAllHubsAreActive() {
	pthread_mutex_lock(&hubMutex);

	
	for(int i=0;i<hubs.size();i++) {
		
		if(hubs[i]->getIsActive()) {
			pthread_mutex_unlock(&hubMutex);
			return true;
		}
	}
	pthread_mutex_unlock(&hubMutex);
	return false;
}


void *DroneRoutine(void *arg) {
	Drone *drone = (Drone *) arg;
	int droneId = drone->getDroneId();
	int speed = drone->getSpeedOfDrone();
	int currentHubId = drone->getStartingHubIdOfDrone();

	long long startTime;
	long long endTime;
	

	int currentRange = drone->getMaximumRangeOfDrone();

	DroneInfo droneInfo;
	FillDroneInfo(&droneInfo, droneId, currentHubId ,currentRange, NULL, 0);
	WriteOutput(NULL, NULL, &droneInfo, NULL, DRONE_CREATED);

	

	while(true) {
		
		startTime = timeInMilliseconds();

		

		bool areThereActiveHubs = false;
		pthread_mutex_lock(&hubMutex);

		
		for(int i=0;i<hubs.size();i++) {
			
			if(hubs[i]->getIsActive()) {

				areThereActiveHubs = true;
				break;
			}
		}
		

		if(!areThereActiveHubs) {
			pthread_mutex_unlock(&hubMutex);
			break;
		}

		pthread_mutex_unlock(&hubMutex);

		

		pthread_mutex_lock(&droneRequestFromDifferentHubMutexes[droneId-1]);
		pthread_mutex_lock(&droneAssignmentMutexes[droneId-1]);


		

		while(!drone->getIsRequested() && !drone->getIsAssignedFromCurrentHub() && isAllHubsAreActive()) {

			
			pthread_mutex_unlock(&droneAssignmentMutexes[droneId-1]);
			pthread_mutex_unlock(&droneRequestFromDifferentHubMutexes[droneId-1]);
			
			drone->waitTheSignal();
			
			pthread_mutex_lock(&droneRequestFromDifferentHubMutexes[droneId-1]);
			pthread_mutex_lock(&droneAssignmentMutexes[droneId-1]);
		}

		


		
		if (drone->getIsAssignedFromCurrentHub()) {

			pthread_mutex_unlock(&droneAssignmentMutexes[droneId-1]);
			pthread_mutex_unlock(&droneRequestFromDifferentHubMutexes[droneId-1]);

			
			pthread_mutex_lock(&dronePackageMutexes[droneId-1]);
			PackageInfo currentPackage = drone->getCurrentPackage();
			pthread_mutex_unlock(&dronePackageMutexes[droneId-1]);

			if (currentPackage.receiving_hub_id == 0) {
				sem_post(&semChargeOccupiedSpaceForHubs[currentHubId-1]);
				sem_post(&semCurrentDronePackage[droneId-1]);

				pthread_mutex_lock(&droneAssignmentMutexes[droneId-1]);

				drone->clearPackageAssignment();

				pthread_mutex_unlock(&droneAssignmentMutexes[droneId-1]);
				continue;
			}

			waitDestinationSpace(currentPackage.receiving_hub_id);

			

			endTime = timeInMilliseconds();
			

			pthread_mutex_lock(&distancesMutexes[currentHubId-1]);

			std::vector<int> distancesOfDestinationHub = hubs[currentPackage.receiving_hub_id-1]->getDistancesToOtherHubs();
			int travellingDistance = distancesOfDestinationHub[currentHubId-1];

			pthread_mutex_unlock(&distancesMutexes[currentHubId-1]);
			
			currentRange = std::min(calculate_drone_charge(endTime-startTime, currentRange, drone->getMaximumRangeOfDrone()) + currentRange, drone->getMaximumRangeOfDrone());

			int rangeDecrease = range_decrease(travellingDistance, speed);


			int remainingRange = currentRange - rangeDecrease;
			while (remainingRange < 0) {
				startTime = timeInMilliseconds();
				wait(abs(remainingRange) * UNIT_TIME);
				endTime = timeInMilliseconds();
				currentRange = std::min(calculate_drone_charge(endTime-startTime, currentRange, drone->getMaximumRangeOfDrone()) + currentRange, drone->getMaximumRangeOfDrone());
				remainingRange = currentRange - rangeDecrease;
			}

			
			pthread_mutex_lock(&chargeStorageMutexes[currentHubId-1]);

			hubs[currentHubId-1]->removeDroneFromHub(droneId);

			pthread_mutex_unlock(&chargeStorageMutexes[currentHubId-1]);
			

			
			sem_post(&semOutStorageSpaceForHubs[currentHubId-1]);
			sem_post(&semChargeSpaceForHubs[currentHubId-1]);

			
			


			PackageInfo package;
			FillPacketInfo(&package, currentPackage.sender_id, currentPackage.sending_hub_id, currentPackage.receiver_id, currentPackage.receiving_hub_id);
			DroneInfo droneInfo2;
			FillDroneInfo(&droneInfo2, droneId, currentHubId, currentRange, &package, 0);
			WriteOutput(NULL, NULL, &droneInfo2, NULL, DRONE_PICKUP);

			

			travel(travellingDistance, speed);

			

			currentRange = currentRange - rangeDecrease;
			currentHubId = package.receiving_hub_id;

			
			drone->setCurrentRange(currentRange);
			
			drone->setCurrentHubId(currentHubId);

			

			pthread_mutex_lock(&inStorageMutexes[currentHubId-1]);

			hubs[currentHubId-1]->putPackageOnIncomingStorage(package);

			pthread_mutex_unlock(&inStorageMutexes[currentHubId-1]);

			
			sem_post(&semGivePackageToReceiver[package.receiver_id-1]);

			

			FillPacketInfo(&package, currentPackage.sender_id, currentPackage.sending_hub_id, currentPackage.receiver_id ,currentPackage.receiving_hub_id);
			DroneInfo droneInfo3;
			FillDroneInfo(&droneInfo3, droneId, currentHubId, currentRange, &package, 0);
			WriteOutput(NULL, NULL, &droneInfo3, NULL, DRONE_DEPOSITED);

			
			pthread_mutex_lock(&chargeStorageMutexes[currentHubId-1]);

			hubs[currentHubId-1]->addDroneToNewHub(droneInfo3);

			pthread_mutex_unlock(&chargeStorageMutexes[currentHubId-1]);
			
			pthread_mutex_lock(&droneAssignmentMutexes[droneId-1]);

			drone->clearPackageAssignment();

			pthread_mutex_unlock(&droneAssignmentMutexes[droneId-1]);


			sem_post(&semChargeOccupiedSpaceForHubs[currentHubId-1]);

			sem_post(&semCurrentDronePackage[droneId-1]);

			
			
		}
		else if (drone->getIsRequested()) {

			pthread_mutex_unlock(&droneAssignmentMutexes[droneId-1]);
			pthread_mutex_unlock(&droneRequestFromDifferentHubMutexes[droneId-1]);

			

			pthread_mutex_lock(&droneComingRequestsMutexes[droneId-1]);
			
			
			int comingRequestHubId = drone->getComingRequestHubId();

			pthread_mutex_unlock(&droneComingRequestsMutexes[droneId-1]);

			

			waitAndReserveChargingSpace(comingRequestHubId);
			

			endTime = timeInMilliseconds();
			

			pthread_mutex_lock(&distancesMutexes[currentHubId-1]);

			std::vector<int> distancesOfDestinationHub = hubs[comingRequestHubId-1]->getDistancesToOtherHubs();
			int travellingDistance = distancesOfDestinationHub[currentHubId-1];

			pthread_mutex_unlock(&distancesMutexes[currentHubId-1]);

			
			
			currentRange = std::min(calculate_drone_charge(endTime-startTime, currentRange, drone->getMaximumRangeOfDrone()) + currentRange, drone->getMaximumRangeOfDrone());

			int rangeDecrease = range_decrease(travellingDistance, speed);


			int remainingRange = currentRange - rangeDecrease;
			while (remainingRange < 0) {
				startTime = timeInMilliseconds();
				wait(abs(remainingRange) * UNIT_TIME);
				endTime = timeInMilliseconds();
				currentRange = std::min(calculate_drone_charge(endTime-startTime, currentRange, drone->getMaximumRangeOfDrone()) + currentRange, drone->getMaximumRangeOfDrone());
				remainingRange = currentRange - rangeDecrease;
			}

			DroneInfo droneInfo2;
			FillDroneInfo(&droneInfo2, droneId, currentHubId, currentRange, NULL, comingRequestHubId);
			WriteOutput(NULL, NULL, &droneInfo2, NULL, DRONE_GOING);

			pthread_mutex_lock(&chargeStorageMutexes[currentHubId-1]);

			hubs[currentHubId-1]->removeDroneFromHub(droneId);

			pthread_mutex_unlock(&chargeStorageMutexes[currentHubId-1]);

			sem_post(&semChargeSpaceForHubs[currentHubId-1]);


			

			travel(travellingDistance, speed);

		

			currentRange = currentRange - rangeDecrease;
			currentHubId = comingRequestHubId;

			
			drone->setCurrentRange(currentRange);
			

			drone->setCurrentHubId(currentHubId);

			DroneInfo droneInfo3;
			FillDroneInfo(&droneInfo3, droneId, currentHubId, currentRange, NULL, 0);
			WriteOutput(NULL, NULL, &droneInfo3, NULL, DRONE_ARRIVED);

			pthread_mutex_lock(&chargeStorageMutexes[currentHubId-1]);

			hubs[currentHubId-1]->addDroneToNewHub(droneInfo3);

			pthread_mutex_unlock(&chargeStorageMutexes[currentHubId-1]);

			hubs[currentHubId-1]->notifyTheArrival();


			pthread_mutex_lock(&droneRequestFromDifferentHubMutexes[droneId-1]);

			// clear isRequested flag
			drone->clearRequestFromAnotherHub();

			pthread_mutex_unlock(&droneRequestFromDifferentHubMutexes[droneId-1]);


			sem_post(&semChargeOccupiedSpaceForHubs[currentHubId-1]);

			

		}
		else { // can quit
			pthread_mutex_unlock(&droneAssignmentMutexes[droneId-1]);
			pthread_mutex_unlock(&droneRequestFromDifferentHubMutexes[droneId-1]);
			break;
		}
		

	}

	DroneInfo droneInfo4;
	FillDroneInfo(&droneInfo4, droneId, currentHubId, currentRange, NULL, 0);
	WriteOutput(NULL, NULL, &droneInfo4, NULL, DRONE_STOPPED);



	return NULL;
}






void receiverPickUp(int receiverId) {
	sem_wait(&semGivePackageToReceiver[receiverId-1]);
}


bool isThereAnyComingPackage(int receiverId) {

	int sval;

	sem_getvalue(&semGivePackageToReceiver[receiverId-1], &sval);

	if (sval > 0) {
		return true;
	} else {
		return false;
	}

	
}



void *ReceiverRoutine(void *arg) {
	Receiver *receiver = (Receiver *) arg;
	int receiverId = receiver->getReceiverId();
	int speedOfReceiver = receiver->getSpeedOfReceiver();
	int waitTime = speedOfReceiver * UNIT_TIME;
	int currentHubId = receiver->getassignedHubIdOfReceiver();

	ReceiverInfo receiverInfo;
	FillReceiverInfo(&receiverInfo, receiverId, currentHubId, NULL);
	WriteOutput(NULL, &receiverInfo, NULL, NULL, RECEIVER_CREATED);

	PackageInfo packageInfo;

	

	while (true) {
		

		if (!hubs[currentHubId-1]->getIsActive()) {
			break;
		}

		

		if (isThereAnyComingPackage(receiverId)) {
			receiverPickUp(receiverId);

			pthread_mutex_lock(&inStorageMutexes[currentHubId-1]);

			PackageInfo package = hubs[currentHubId-1]->getPackageFromIncomingStorage(receiverId);

			pthread_mutex_unlock(&inStorageMutexes[currentHubId-1]);

			
		

			if (package.sender_id != 0) {
				//package is coming
				
				sem_post(&semInStorageSpaceForHubs[currentHubId-1]);
				FillPacketInfo(&packageInfo, package.sender_id, package.sending_hub_id, package.receiver_id, package.receiving_hub_id);
				FillReceiverInfo(&receiverInfo, receiverId, currentHubId, &packageInfo);
				WriteOutput(NULL, &receiverInfo, NULL, NULL, RECEIVER_PICKUP);
				wait(waitTime);
			}
		}
		

		

		
	}

	FillReceiverInfo(&receiverInfo, receiverId, currentHubId, NULL);
	WriteOutput(NULL, &receiverInfo, NULL, NULL, RECEIVER_STOPPED);



	return NULL;
}









void waitCanDeposit(int hubId) {
	sem_wait(&semOutStorageSpaceForHubs[hubId-1]);
}

void senderDeposit(int senderId) {
	sem_post(&semGetPackageFromSender[senderId-1]);
}






void *SenderRoutine(void *arg) {
	Sender *sender = (Sender *) arg;
	int senderId = sender->getSenderId();
	int speedOfSender = sender->getSpeedOfSender();
	int waitTime = speedOfSender * UNIT_TIME;
	int currentHubId = sender->getassignedHubIDOfSender();
	int remainingPackages = sender->getTotalPackageOfSender();


	SenderInfo senderInfo;
	FillSenderInfo(&senderInfo, sender->getSenderId(), currentHubId, remainingPackages, NULL);
	WriteOutput(&senderInfo, NULL, NULL, NULL, SENDER_CREATED);

	

	while(remainingPackages > 0) {
		
		int noOfHubs = hubs.size();

		int randomIndex1;


  		do {
  			/* generate random number from 0 to (noOfHubs-1): */
  			randomIndex1 = rand() % noOfHubs;
  		} 
  		while((currentHubId-1) == randomIndex1);

  		int receiverId;

  		pthread_mutex_lock(&receiverMutex);

  		for(int i=0;i<receivers.size();i++) {
  			if(receivers[i]->getassignedHubIdOfReceiver() == randomIndex1+1) {
  				receiverId = receivers[i]->getReceiverId();
  				break;
  			}
  		}
  
		pthread_mutex_unlock(&receiverMutex);

		

		waitCanDeposit(currentHubId);

		

		PackageInfo packageInfo;
		FillPacketInfo(&packageInfo, senderId, currentHubId, receiverId, randomIndex1+1);
		FillSenderInfo(&senderInfo, senderId, currentHubId, remainingPackages, &packageInfo);

		
		pthread_mutex_lock(&outStorageMutexes[currentHubId-1]);

		hubs[currentHubId-1]->putPackageOnOutgoingStorage(packageInfo);

		pthread_mutex_unlock(&outStorageMutexes[currentHubId-1]);
		

		senderDeposit(senderId);

		sender->notifyForDeposit();
		
		

		WriteOutput(&senderInfo, NULL, NULL, NULL, SENDER_DEPOSITED);

		

		remainingPackages -= 1; 
		

		wait(waitTime);
   
	}

	



	
	

	pthread_mutex_lock(&senderActiveMutexes[senderId-1]);

	sender->stopProducing();

	pthread_mutex_unlock(&senderActiveMutexes[senderId-1]);
	

	

    

    FillSenderInfo(&senderInfo, senderId, currentHubId, remainingPackages, NULL);
	WriteOutput(&senderInfo, NULL, NULL, NULL, SENDER_STOPPED);

    
	int activeSenderCount = 0;
	for(int i=0;i<senders.size();i++) {
		
		if (senders[i]->getIsProducing() == true) {
			activeSenderCount += 1;
		}
		
	}

	if (activeSenderCount == 0) {
		for(int i=0;i<senders.size();i++) {
			senders[i]->notifyForSenderQuit();
		}
	}

	


	return NULL;
}










bool isThereAnyDroneInTheHub(int hubId) {
	int sval;

	sem_getvalue(&semChargeOccupiedSpaceForHubs[hubId-1], &sval);

	if (sval > 0) {
		return true;
	} else {
		return false;
	}
}





void *HubRoutine(void *arg) {
	Hub *hub = (Hub *) arg;
	int hubId = hub->getHubId();
	int inPackStorageOfHub = hub->getinPackStorageOfHub();
	int outPackStrogeOfHub = hub->getoutPackStorageOfHub();

	

	for(int i=0;i<drones.size();i++) {
		if (drones[i]->getStartingHubIdOfDrone() == hubId) {
			DroneInfo newDrone;
			newDrone.id = drones[i]->getDroneId();
			
			newDrone.current_hub_id = hubId;
			newDrone.current_range = drones[i]->getCurrentRange();
			
			sem_wait(&semChargeSpaceForHubs[hubId-1]);
			hub->addDroneToNewHub(newDrone);
			sem_post(&semChargeOccupiedSpaceForHubs[hubId-1]);
		
 		}
	}

	

	HubInfo hubInfo;
	FillHubInfo(&hubInfo, hubId);
	WriteOutput(NULL, NULL, NULL, &hubInfo, HUB_CREATED);

	int assignedSenderId;

	
	for(int i=0;i<senders.size();i++) {
		if(senders[i]->getassignedHubIDOfSender() == hubId) {
			assignedSenderId = senders[i]->getSenderId();
			break;
		}
	}
	

    bool isThereAnyActiveSender = false;

    for (int i=0;i<senders.size();i++) {

   		if (senders[i]->getIsProducing()) {
   			isThereAnyActiveSender = true;
   			break;
   		}
    }

   

    pthread_mutex_lock(&inStorageMutexes[hubId-1]);
    pthread_mutex_lock(&outStorageMutexes[hubId-1]);


    while (isThereAnyActiveSender || !hub->isIncomingStorageEmpty() || !hub->isOutgoingStorageEmpty()) {

    	

    	pthread_mutex_unlock(&outStorageMutexes[hubId-1]);
   		pthread_mutex_unlock(&inStorageMutexes[hubId-1]);
   		

   		




   		pthread_mutex_lock(&senderActiveMutexes[assignedSenderId-1]);
   		pthread_mutex_lock(&outStorageMutexes[hubId-1]);

   		while (senders[assignedSenderId-1]->getIsProducing() && hub->isOutgoingStorageEmpty()) {

   			pthread_mutex_unlock(&outStorageMutexes[hubId-1]);
   			pthread_mutex_unlock(&senderActiveMutexes[assignedSenderId-1]);

   			senders[assignedSenderId-1]->waitUntilPackageDeposited();

   			pthread_mutex_lock(&senderActiveMutexes[assignedSenderId-1]);
   			pthread_mutex_lock(&outStorageMutexes[hubId-1]);
   		}

   		pthread_mutex_unlock(&outStorageMutexes[hubId-1]);
		pthread_mutex_unlock(&senderActiveMutexes[assignedSenderId-1]);



   	    bool getThePackageFromOutgoing = false;

   	    pthread_mutex_lock(&outStorageMutexes[hubId-1]);

   	    // gonderilecek paket var
   	    if (!hub->isOutgoingStorageEmpty()) {
   	    	getThePackageFromOutgoing = true;
   	    } 

   	    pthread_mutex_unlock(&outStorageMutexes[hubId-1]);
		
		

	    isThereAnyActiveSender = false;

	    for (int i=0;i<senders.size();i++) {


	   		if (senders[i]->getIsProducing()) {
	   			isThereAnyActiveSender = true;
	   			break;
	   		}
	    }

	    

		

		

		pthread_mutex_lock(&outStorageMutexes[hubId-1]);

		if (getThePackageFromOutgoing == false && hub->isOutgoingStorageEmpty()) { // ONLY incoming packets are waited
			pthread_mutex_unlock(&outStorageMutexes[hubId-1]);

			pthread_mutex_lock(&inStorageMutexes[hubId-1]);
    		pthread_mutex_lock(&outStorageMutexes[hubId-1]);

			
			continue;
		}

		pthread_mutex_unlock(&outStorageMutexes[hubId-1]);


		


		do {



			pthread_mutex_lock(&hubMutex);
			pthread_mutex_lock(&chargeStorageMutexes[hubId-1]);
			
			

			int droneSize = hub->getDronesSize();

			
			if (isThereAnyDroneInTheHub(hubId) || droneSize > 0) {

				pthread_mutex_unlock(&chargeStorageMutexes[hubId-1]);

				pthread_mutex_unlock(&hubMutex);
				sem_wait(&semChargeOccupiedSpaceForHubs[hubId-1]);
				

				pthread_mutex_lock(&chargeStorageMutexes[hubId-1]);

				

				int assignedDroneId = hub->selectDroneWithHighestCurrentRange();

				
				pthread_mutex_unlock(&chargeStorageMutexes[hubId-1]);

				

				pthread_mutex_lock(&outStorageMutexes[hubId-1]);


				
				PackageInfo package;
				package = hub->getPackageFromOutgoingStorage();

				pthread_mutex_unlock(&outStorageMutexes[hubId-1]);
				

				if (assignedDroneId > 0) {
					
					
					sem_wait(&semCurrentDronePackage[assignedDroneId-1]);



					pthread_mutex_lock(&dronePackageMutexes[assignedDroneId-1]);
					pthread_mutex_lock(&droneAssignmentMutexes[assignedDroneId-1]);

					drones[assignedDroneId-1]->assignDrone(package);

					pthread_mutex_unlock(&droneAssignmentMutexes[assignedDroneId-1]);
					pthread_mutex_unlock(&dronePackageMutexes[assignedDroneId-1]);

					
				}

				getThePackageFromOutgoing = false;


			}
			else {
				
				pthread_mutex_unlock(&chargeStorageMutexes[hubId-1]);

				pthread_mutex_unlock(&hubMutex);

				bool isDroneCalled = false;

				
				

				pthread_mutex_lock(&droneMutex);

				pthread_mutex_lock(&distancesMutexes[hubId-1]);

				std::vector<int> distancesToOtherHubs = hub->getDistancesToOtherHubs();
				std::vector<int> ascendingDistancesToOtherHubs = hub->getDistancesToOtherHubs();
				std::sort(ascendingDistancesToOtherHubs.begin(), ascendingDistancesToOtherHubs.end()); 

				pthread_mutex_unlock(&distancesMutexes[hubId-1]);

				

				for(int i=1;i<ascendingDistancesToOtherHubs.size() && !isDroneCalled;i++) {
					int currentMinDistance = ascendingDistancesToOtherHubs[i];
					
					for(int j=0;j<distancesToOtherHubs.size() && !isDroneCalled;j++) {
						if(distancesToOtherHubs[j] == currentMinDistance) {
							//j+1 = hubId
							

							if (isThereAnyDroneInTheHub(j+1)) { // drone is available on far hub
								sem_wait(&semChargeOccupiedSpaceForHubs[j]);

								pthread_mutex_lock(&chargeStorageMutexes[j]);

								std::vector<int> currentDroneIds = hubs[j]->getCurrentDroneIds();
								
								for(int k=0;k<currentDroneIds.size() && !isDroneCalled;k++) {
									

									

									pthread_mutex_lock(&droneRequestFromDifferentHubMutexes[currentDroneIds[k]-1]);
									pthread_mutex_lock(&droneAssignmentMutexes[currentDroneIds[k]-1]);


									if (!drones[currentDroneIds[k]-1]->getIsRequested() && !drones[currentDroneIds[k]-1]->getIsAssignedFromCurrentHub()) {
										// call the drone
										
										drones[currentDroneIds[k]-1]->droneRequest(hubId);
										isDroneCalled = true;
										
									}

									
									pthread_mutex_unlock(&droneAssignmentMutexes[currentDroneIds[k]-1]);
									pthread_mutex_unlock(&droneRequestFromDifferentHubMutexes[currentDroneIds[k]-1]);
								}

								pthread_mutex_unlock(&chargeStorageMutexes[j]);

								if (isDroneCalled != true) {
									sem_post(&semChargeOccupiedSpaceForHubs[j]);
								}


							} 
							

							
						}
					}
				}

				
				// wait for arriving of called drone
				if (isDroneCalled == true) {
					hub->waitForTheDroneArrival();
					getThePackageFromOutgoing = false;
				} 
				if (isDroneCalled == false) { // wait for specific time
					wait(UNIT_TIME);
					
				}

				pthread_mutex_unlock(&droneMutex);

			}

			

			
		} while(getThePackageFromOutgoing);

	

	    pthread_mutex_lock(&inStorageMutexes[hubId-1]);
    	pthread_mutex_lock(&outStorageMutexes[hubId-1]);
			
   }


	
	

	hub->deactivate();

	FillHubInfo(&hubInfo, hubId);
	WriteOutput(NULL, NULL, NULL, &hubInfo, HUB_STOPPED);

	pthread_mutex_lock(&chargeStorageMutexes[hubId-1]);

	std::vector<int> currentDroneIDs = hub->getCurrentDroneIds();

	for(int i=0;i<currentDroneIDs.size();i++) {
		drones[currentDroneIDs[i]-1]->notifyTheHubQuit();
	}

	pthread_mutex_unlock(&chargeStorageMutexes[hubId-1]);

	// if this is the last hub:
	int activeHubCount = 0;
	for(int i=0;i<hubs.size();i++) {
		if (hub->getIsActive() == true) {
			activeHubCount += 1;
		}
	}

	if (activeHubCount == 0) {
		for(int i=0;i<drones.size();i++) {
			drones[i]->notifyTheHubQuit();
		}
	}


	bool remainingActiveSenders = false;

    for (int i=0;i<senders.size();i++) {


   		if (senders[i]->getIsProducing()) {
   			remainingActiveSenders = true;
   			break;
   		}
    }

    if (remainingActiveSenders == false) {
    	for (int i=0;i<senders.size();i++) {
    		senders[i]->notifyForSenderQuit();
    	}
    }

	
	pthread_mutex_unlock(&outStorageMutexes[hubId-1]);
	pthread_mutex_unlock(&inStorageMutexes[hubId-1]);



	return NULL;
}






int main(int argc, const char* argv[]) {



// numberOfHubs = Nt

// --- Hub inputs ----
int numberOfHubs;
int inPackStorageOfHub, outPackStorageOfHub, chargeSpaceOfHub;

std::cin >> numberOfHubs;


pthread_t *hubThreads, *senderThreads, *receiverThreads, *droneThreads;


InitWriteOutput();




hubThreads = new pthread_t[numberOfHubs];
for(int i=0; i< numberOfHubs; i++) {
	std::cin >> inPackStorageOfHub >> outPackStorageOfHub >> chargeSpaceOfHub;
	int distance;
	std::vector<int> distancesToOtherHubs;
	for(int j=0; j<numberOfHubs;j++) {
		std::cin >> distance;
		distancesToOtherHubs.emplace_back(distance);
	}

	
	sem_t semInStorage;
	sem_init(&semInStorage, 0, inPackStorageOfHub);

	sem_t semOutStorage;
	sem_init(&semOutStorage, 0, outPackStorageOfHub);

	sem_t semChargeStorage;
	sem_init(&semChargeStorage, 0, chargeSpaceOfHub);

	sem_t semChargeOccupiedStorage;
	sem_init(&semChargeOccupiedStorage, 0, 0);

	semInStorageSpaceForHubs.emplace_back(semInStorage);
	semOutStorageSpaceForHubs.emplace_back(semOutStorage);
	semChargeSpaceForHubs.emplace_back(semChargeStorage);
	semChargeOccupiedSpaceForHubs.emplace_back(semChargeOccupiedStorage);
	

	
	Hub* hub = new Hub(i+1, inPackStorageOfHub, outPackStorageOfHub, chargeSpaceOfHub, distancesToOtherHubs);
	hubs.emplace_back(hub);
	
}






for(int i=0;i<numberOfHubs;i++) {
	pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
	inStorageMutexes.emplace_back(mutex);
}

for(int i=0;i<numberOfHubs;i++) {
	pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
	outStorageMutexes.emplace_back(mutex);
}

for(int i=0;i<numberOfHubs;i++) {
	pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
	chargeStorageMutexes.emplace_back(mutex);
}

for(int i=0;i<numberOfHubs;i++) {
	pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
	distancesMutexes.emplace_back(mutex);
}



// --- Sender inputs ------
int speedOfSender, assignedHubIDOfSender, totalPackageOfSender;


senderThreads = new pthread_t[numberOfHubs];
for (int i = 0; i < numberOfHubs; i++) {
    std::cin >> speedOfSender >> assignedHubIDOfSender >> totalPackageOfSender;

    sem_t semGetPackage;
    sem_init(&semGetPackage, 0, 0);

   
    semGetPackageFromSender.emplace_back(semGetPackage);
   

    Sender* sender = new Sender(i+1, speedOfSender, assignedHubIDOfSender, totalPackageOfSender);
    senders.emplace_back(sender);
    
}


for(int i=0;i<numberOfHubs;i++) {
	pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
	senderActiveMutexes.emplace_back(mutex);
}





// ----- Receiver Inputs -----
int speedOfReceiver, assignedHubIDOfReceiver;

receiverThreads = new pthread_t[numberOfHubs];
for (int i = 0; i < numberOfHubs; i++) {
    std::cin >> speedOfReceiver >> assignedHubIDOfReceiver;

    //semGivePackageToReceiver;

    sem_t semGivePackage;
    sem_init(&semGivePackage, 0 , 0);

    semGivePackageToReceiver.emplace_back(semGivePackage);

    Receiver* receiver = new Receiver(i+1, speedOfReceiver, assignedHubIDOfReceiver);
    receivers.emplace_back(receiver);
   
}






// ------- Drone inputs ------
int numberOfDrones;
std::cin >> numberOfDrones;

int speedOfDrone, startingHubIdOfDrone, maximumRangeOfDrone;

droneThreads = new pthread_t[numberOfDrones];
for(int i=0;i<numberOfDrones;i++) {
	std::cin >> speedOfDrone >> startingHubIdOfDrone >> maximumRangeOfDrone;

	sem_t semDronePackage;
	sem_init(&semDronePackage, 0, 1);

	sem_t semAssignment;
	sem_init(&semAssignment, 0, 0);

	sem_t semRequest;
	sem_init(&semRequest, 0, 0);

	semCurrentDronePackage.emplace_back(semDronePackage);
	semAssignmentFromCurrentHub.emplace_back(semAssignment);
	semRequestFromNearbyHub.emplace_back(semRequest);



	Drone* drone = new Drone(i+1, speedOfDrone, startingHubIdOfDrone, maximumRangeOfDrone);
	drones.emplace_back(drone);
	
}





for(int i=0;i<numberOfDrones;i++) {
	pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
	droneRequestFromDifferentHubMutexes.emplace_back(mutex);
}

for(int i=0;i<numberOfDrones;i++) {
	pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
	droneAssignmentMutexes.emplace_back(mutex);
}

for(int i=0;i<numberOfDrones;i++) {
	pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
	dronePackageMutexes.emplace_back(mutex);
}

for(int i=0;i<numberOfDrones;i++) {
	pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
	droneComingRequestsMutexes.emplace_back(mutex);
}











for(int i=0;i< numberOfHubs; i++) {
	pthread_create(&hubThreads[i], NULL, HubRoutine, (void *) hubs[i]);
}

for(int i=0;i< numberOfHubs; i++) {
	pthread_create(&senderThreads[i], NULL, SenderRoutine, (void *) senders[i]);
}

for(int i=0;i< numberOfHubs; i++) {
	pthread_create(&receiverThreads[i], NULL, ReceiverRoutine, (void *) receivers[i]);
}

for(int i=0;i< numberOfDrones; i++) {
	pthread_create(&droneThreads[i], NULL, DroneRoutine, (void *) drones[i]);
}







/* wait for the termination of threads */
for (int i=0;i<numberOfHubs; i++) {
	pthread_join(hubThreads[i], NULL);
}

for (int i=0;i<numberOfHubs; i++) {
	pthread_join(senderThreads[i], NULL);
}

for (int i=0;i<numberOfHubs; i++) {
	pthread_join(receiverThreads[i], NULL);
}

for (int i=0;i<numberOfDrones; i++) {
	pthread_join(droneThreads[i], NULL);
}





delete [] hubThreads;
delete [] senderThreads;
delete [] receiverThreads;
delete [] droneThreads;


/* free the agents */
for (int i=0; i<hubs.size(); i++) {

    delete hubs[i];
}

for (int i=0; i<senders.size(); i++) {

    delete senders[i];
}

for (int i=0; i<receivers.size(); i++) {

    delete receivers[i];
}

for (int i=0; i<drones.size(); i++) {

    delete drones[i];
}



return 0;

    
}
#include "message.h"

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>


#define MAX(a,b) (((a)>(b))?(a):(b))


int manhattanDistance(int xPlayer, int xMonster, int yPlayer, int yMonster) {
	int xDiff = xPlayer - xMonster;
	int yDiff = yPlayer - yMonster;
	int distance = abs(xDiff) + abs(yDiff);
	return distance;
}

coordinate calculateMove(int xPlayer, int xMonster, int yPlayer, int yMonster) {
	int distances[8];
	int xS[8];
	int yS[8];
	int i;
	int minDistance;
	int minIndex;


	for(i=0;i<8;i++) {
		if (i==0) { // when go up-left
			xS[0] = xMonster-1;
			yS[0] = yMonster-1;
			distances[i] = manhattanDistance(xPlayer, xS[0], yPlayer, yS[0]);
		}
		if (i==1) { // when go left
			xS[1] = xMonster-1;
			yS[1] = yMonster;
			distances[i] = manhattanDistance(xPlayer, xS[1], yPlayer, yS[1]);
		}
		if (i==2) { // when go bottom-left
			xS[2] = xMonster-1;
			yS[2] = yMonster+1;
			distances[i] = manhattanDistance(xPlayer, xS[2], yPlayer, yS[2]);
		}
		if (i==3) { // when go bottom
			xS[3] = xMonster;
			yS[3] = yMonster+1;
			distances[i] = manhattanDistance(xPlayer, xS[3], yPlayer, yS[3]);
		}
		if (i==4) { // when go bottom-right
			xS[4] = xMonster+1;
			yS[4] = yMonster+1;
			distances[i] = manhattanDistance(xPlayer, xS[4], yPlayer, yS[4]);
		}
		if (i==5) { // when go right
			xS[5] = xMonster+1;
			yS[5] = yMonster;
			distances[i] = manhattanDistance(xPlayer, xS[5], yPlayer, yS[5]);
		}
		if (i==6) { // when go up-right
			xS[6] = xMonster+1;
			yS[6] = yMonster-1;
			distances[i] = manhattanDistance(xPlayer, xS[6], yPlayer, yS[6]);
		}
		if (i==7) { // when go up
			xS[7] = xMonster;
			yS[7] = yMonster-1;
			distances[i] = manhattanDistance(xPlayer, xS[7], yPlayer, yS[7]);
		}
	}

	minDistance = distances[0];
	minIndex = 0;

	for(i=0;i<8;i++) {
		if (minDistance >= distances[i]) {
			minDistance = distances[i];
			minIndex = i;
		}
	}

	coordinate minCoord;
	minCoord.x = xS[minIndex];
	minCoord.y = yS[minIndex];

	
	return minCoord;
	
}


int main(int argc, const char* argv[]) {




	int health = atoi(argv[1]);
	int damage = atoi(argv[2]);
	int defence = atoi(argv[3]);
	int range = atoi(argv[4]);



	monster_response readyResponse;
	

	readyResponse.mr_type = mr_ready;
	

	write(1, &readyResponse, sizeof(struct monster_response));


	while (1) {
		
		monster_message monsterMessage;
		monster_response monsterResponse;
		int xPlayer;
		int yPlayer;

		read(0, &monsterMessage, sizeof(struct monster_message));

		health = health - MAX(0, monsterMessage.damage - defence);

		if(monsterMessage.game_over) {
			exit(0);
		}

		if (health <= 0) {
			monsterResponse.mr_type = mr_dead;
			write(1, &monsterResponse, sizeof(struct monster_response));
			break;
		}

		else if (manhattanDistance(monsterMessage.player_coordinate.x, monsterMessage.new_position.x, monsterMessage.player_coordinate.y, monsterMessage.new_position.y) <= range) {
			monsterResponse.mr_type = mr_attack;
			monsterResponse.mr_content.attack = damage;
		} else {
			
			monsterResponse.mr_type = mr_move;
			coordinate newCoords = calculateMove(monsterMessage.player_coordinate.x, monsterMessage.new_position.x, monsterMessage.player_coordinate.y, monsterMessage.new_position.y);
			monsterResponse.mr_content.move_to = newCoords;
		}

		write(1, &monsterResponse, sizeof(struct monster_response));

	}

	


	return 0;
}













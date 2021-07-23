#include "logging.h"

#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <unistd.h>

#define PIPE(fd) socketpair(AF_UNIX, SOCK_STREAM, PF_UNIX, fd)


#define BUFFSIZE 100


typedef struct monster_executable {
	char executable[BUFFSIZE];
	char symbol;
	int x;
	int y;
	int arg1;
	int arg2;
	int arg3;
	int arg4;
} monster_executable;


typedef struct active_monster {
	int x;
	int y;
	int pid;
	monster_message monsterMessage;
	monster_response monsterResponse;
	int pipe[2];
	char symbol;
} active_monster;




int manhattanDistanceToZero(int x, int y) {
	return (x + y);
}

void sortMonsters(struct active_monster activeMonsters[MONSTER_LIMIT], int numberOfMonsters) {
	int i, j;
    struct active_monster temp;
    
    for (i = 0; i < numberOfMonsters - 1; i++)
    {
        for (j = 0; j < (numberOfMonsters - 1-i); j++)
        {
            if (activeMonsters[j].x > activeMonsters[j+1].x)
            {
                temp = activeMonsters[j];
                activeMonsters[j] = activeMonsters[j + 1];
                activeMonsters[j + 1] = temp;
            } else if (activeMonsters[j].x == activeMonsters[j+1].x) {
            	if (activeMonsters[j].y > activeMonsters[j+1].y) {
            		temp = activeMonsters[j];
                	activeMonsters[j] = activeMonsters[j + 1];
                	activeMonsters[j + 1] = temp;
            	}
            } 
        }
    }
}


void processMonsterResponse(struct monster_response response, struct player_message *playerMessage, struct monster_message *monsterMessage, int order);
bool isMonsterDead(struct monster_response response);
void processPlayerResponse(struct player_response response, struct player_message *playerMessage, struct active_monster *activeMonsters);
bool isGameOver(struct player_response response);
void playerLeft();
void printCurrentStateOfTheMap();
void stopLoop(int pidPlayer);




int width_of_the_room;
int height_of_the_room;
int xDoor;
int yDoor;
int xPlayer;
int yPlayer;

char player[BUFFSIZE];
int arg3,arg4,arg5;

int numberOfMonsters;
struct monster_executable* monsters;
struct active_monster* activeMonsters;

int pipeForPlayer[2];
int pidPlayer; 


int main() {


	// ----- GETTING INPUT -----

	int i,j;


	scanf("%d %d\n",&width_of_the_room, &height_of_the_room);
	scanf("%d %d\n", &xDoor, &yDoor);
	scanf("%d %d\n", &xPlayer, &yPlayer);

	scanf("%s %d %d %d\n", player, &arg3, &arg4, &arg5);

	scanf("%d\n", &numberOfMonsters);

	monsters = malloc(numberOfMonsters * sizeof(struct monster_executable));

	
	for (i=0; i<numberOfMonsters ;i++) {
		monster_executable monster;
		if ((i+1) != numberOfMonsters) {
			scanf("%s %c %d %d %d %d %d %d\n", monster.executable, &monster.symbol, &monster.x, &monster.y, &monster.arg1, &monster.arg2, &monster.arg3, &monster.arg4);
		} else {
			scanf("%s %c %d %d %d %d %d %d", monster.executable, &monster.symbol, &monster.x, &monster.y, &monster.arg1, &monster.arg2, &monster.arg3, &monster.arg4);

		}
		monsters[i] = monster;
	}

	activeMonsters = malloc(numberOfMonsters * sizeof(struct active_monster));

	for (i=0;i<numberOfMonsters;i++) {
		active_monster monster;
		monster.x = monsters[i].x;
		monster.y = monsters[i].y;
		monster.symbol = monsters[i].symbol;
		activeMonsters[i] = monster;
	}




	

	PIPE(pipeForPlayer);

	

	for (i=0;i<numberOfMonsters; i++) {
		PIPE(activeMonsters[i].pipe);
	}


	if (!fork()) { // CHILD [PLAYER]

		pidPlayer = getpid();
		
		
		close(pipeForPlayer[0]);	
		dup2(pipeForPlayer[1], 0);
		dup2(pipeForPlayer[1], 1);
		close(pipeForPlayer[1]);


		char pl_arg1[50];
		char pl_arg2[50];
		char pl_arg3[50];
		char pl_arg4[50];
		char pl_arg5[50];

		sprintf(pl_arg1, "%d", xDoor);
		sprintf(pl_arg2, "%d", yDoor);
		sprintf(pl_arg3, "%d", arg3);
		sprintf(pl_arg4, "%d", arg4);
		sprintf(pl_arg5, "%d", arg5);

		execl(player, player, pl_arg1, pl_arg2, pl_arg3, pl_arg4, pl_arg5, (char *) 0);
		

		
	} else { // continues as PARENT

		
		close(pipeForPlayer[1]);
		
		
	}

	


	for(i=0;i<numberOfMonsters;i++) {

		if(!fork()) { // [CHILD]: MONSTER

			activeMonsters[i].pid = getpid();

			close(activeMonsters[i].pipe[0]);	
			dup2(activeMonsters[i].pipe[1], 0);
			dup2(activeMonsters[i].pipe[1], 1);
			close(activeMonsters[i].pipe[1]);

			
		
			 char arg1[50];
			 char arg2[50];
			 char arg3[50];
			 char arg4[50];

			 sprintf(arg1, "%d", monsters[i].arg1);
			 sprintf(arg2, "%d", monsters[i].arg2);
			 sprintf(arg3, "%d", monsters[i].arg3);
			 sprintf(arg4, "%d", monsters[i].arg4);
    		
			execl(monsters[i].executable, monsters[i].executable ,arg1 ,arg2, arg3,arg4,  (char *) 0);


		} else { // PARENT continues from here

			close(activeMonsters[i].pipe[1]);
    		
		}
	}




	while (1) {

		for(i=0;i<numberOfMonsters;) {
			monster_response mrResponse;
			read(activeMonsters[i].pipe[0], &mrResponse, sizeof(struct monster_response));

			if(mrResponse.mr_type == mr_ready) {
				
				i++;
			} 
		}

		while (1) {
			player_response prResponse;
		    read(pipeForPlayer[0], &prResponse, sizeof(struct player_response));

		    if (prResponse.pr_type == pr_ready) {
		    	break;
		    }
		}

		



		printCurrentStateOfTheMap();
		



		break;
	}



	

    char c;
	int turn = 1;
	ssize_t nbytes;


	int alive_monster_count = numberOfMonsters;
	game_over_status gameOverStatus;

	player_message playerMessage;
	player_response playerResponse;

	while (1) {



		if(turn == 1) {
			playerMessage.new_position.x = xPlayer;
			playerMessage.new_position.y = yPlayer;
			playerMessage.total_damage = 0;
			playerMessage.alive_monster_count = alive_monster_count;

			for(i =0;i<alive_monster_count;i++) {
				coordinate coord;
				coord.x = activeMonsters[i].x;
				coord.y = activeMonsters[i].y;
				playerMessage.monster_coordinates[i] = coord;
			}
			playerMessage.game_over = false; // check again


			
			for(i=0;i<numberOfMonsters;i++) {
				activeMonsters[i].monsterMessage.new_position.x = activeMonsters[i].x;
				activeMonsters[i].monsterMessage.new_position.y = activeMonsters[i].y;
				activeMonsters[i].monsterMessage.damage = 0;
				activeMonsters[i].monsterMessage.player_coordinate.x = xPlayer;
				activeMonsters[i].monsterMessage.player_coordinate.y = yPlayer;
				activeMonsters[i].monsterMessage.game_over = false;
			}

		}

		write(pipeForPlayer[0], &playerMessage, sizeof(struct player_message));

		if ((nbytes = read(pipeForPlayer[0], &playerResponse, sizeof(struct player_response))) <= 0) {
			playerLeft();
			for(int i=0;i<numberOfMonsters;i++) {
				activeMonsters[i].monsterMessage.game_over = true;
				write(activeMonsters[i].pipe[0], &activeMonsters[i].monsterMessage, sizeof(struct monster_message));
			}
			stopLoop(pidPlayer);
			break;
		}

		processPlayerResponse(playerResponse, &playerMessage, activeMonsters);

		if (isGameOver(playerResponse)) {
			playerMessage.game_over = true;
			write(pipeForPlayer[0], &playerMessage, sizeof(struct player_message));
			for(i=0;i<numberOfMonsters;i++) {
				activeMonsters[i].monsterMessage.game_over = true;
				write(activeMonsters[i].pipe[0], &activeMonsters[i].monsterMessage, sizeof(struct monster_message));
			}
			stopLoop(pidPlayer);
			break;
		}

		// 3.Send turn message to the every monster
		for(i=0;i<numberOfMonsters;i++) {
			write(activeMonsters[i].pipe[0], &activeMonsters[i].monsterMessage, sizeof(struct monster_message));

		}

		//4. Collect monster responses in a loop, checking for any of the pipe ends for a new responses and reading them.
		for(i=0;i<numberOfMonsters;i++) {

			while (1) {

				ssize_t result = read(activeMonsters[i].pipe[0], &activeMonsters[i].monsterResponse, sizeof(struct monster_response));

            if (-1 == result) 
		           {
		              perror("read() failed");
		            }
		            else if (0 == result)
		            {
		              fprintf(stderr, "read() returned without having read anything.\n");
		            }
		            else
		            {
		            	break;
		            }
			}

		}

		

		playerMessage.total_damage = 0;
		int deadMonsters = 0;
		for(i=0;i<numberOfMonsters;i++) {
			processMonsterResponse(activeMonsters[i].monsterResponse, &playerMessage, &activeMonsters[i].monsterMessage, i);
			if (isMonsterDead(activeMonsters[i].monsterResponse)) {

				close(activeMonsters[i].pipe[0]);

				for(j=i;j<numberOfMonsters-1;j++) {
					activeMonsters[j] = activeMonsters[j+1];
				}


				deadMonsters += 1;

				
			}
		}

		numberOfMonsters -= deadMonsters;
		playerMessage.alive_monster_count = numberOfMonsters;
		if(numberOfMonsters == 0) {
			playerMessage.game_over = true;
		}


		if (isGameOver(playerResponse)) {
			playerMessage.game_over = true;
			write(pipeForPlayer[0], &playerMessage, sizeof(struct player_message));
			for(i=0;i<numberOfMonsters;i++) {
				activeMonsters[i].monsterMessage.game_over = true;
				write(activeMonsters[i].pipe[0], &activeMonsters[i].monsterMessage, sizeof(struct monster_message));
			}
			stopLoop(pidPlayer);
			break;
		}

		printCurrentStateOfTheMap();

		for(i =0;i<numberOfMonsters;i++) {
			coordinate coord;
			coord.x = activeMonsters[i].x;
			coord.y = activeMonsters[i].y;
			playerMessage.monster_coordinates[i] = coord;
		}



		turn++;

	}




	free(monsters);
	free(activeMonsters);

    return 0;
	
	
}




void processMonsterResponse(struct monster_response response, struct player_message *playerMessage, struct monster_message *monsterMessage, int order) {
	int i,j;
	bool validRequest = true;
	int x,y;
	if (response.mr_type == mr_move) {

		x = response.mr_content.move_to.x;
		y = response.mr_content.move_to.y;

		//upper wall case
		if ((x>=0) && (x <= (width_of_the_room-1)) && (y == 0)) { 
			validRequest = false;
		}
		// bottom wall case 
		if ((x>=0) && (x <= (width_of_the_room-1)) && (y == (height_of_the_room-1))) {
			validRequest = false;
		}
		// left wall case
		if ((y>=0) && (y <= (height_of_the_room-1)) && (x == 0)) {
			validRequest = false;
		}
		// right wall case
		if ((y>=0) && (y <= (height_of_the_room-1)) && (x == (width_of_the_room-1))) {
			validRequest = false;
		}
		// upper bound case
		if(y < 0) {
			validRequest = false;
		}
		// lower bound case
		if(y >= height_of_the_room) {
			validRequest = false;
		}
		// left bound case
		if(x < 0) {
			validRequest = false;
		}
		// right bound case
		if(x >= width_of_the_room) {
			validRequest = false;
		}
		// door case
		if (x == xDoor && y == yDoor) {
			validRequest = false;
		}
		// another entity case
		int i;
		for (i = 0; i<numberOfMonsters;i++) {
			if (activeMonsters[i].x == x && activeMonsters[i].y == y) {
				validRequest = false;
			}
		}
		// player occupied cell case
		if (x == xPlayer && y == yPlayer) {
			validRequest = false;
		}

		if(validRequest == true) {
			
			monsterMessage->new_position.x = x;
			monsterMessage->new_position.y = y;
			for(i=0;i<numberOfMonsters;i++) {
				if (i == order) {
					activeMonsters[i].x = x;
					activeMonsters[i].y = y;
				}
			}
			
			return;
		} else {
			return;
		}

	}
	else if (response.mr_type == mr_attack) {

		playerMessage->total_damage += response.mr_content.attack;

		return;
	}
	
	else if (response.mr_type == mr_dead) {
		
		/*for(i=order;i<numberOfMonsters-1;i++) {
			activeMonsters[i] = activeMonsters[i+1];
		}*/

		return;
	}
}



bool isMonsterDead(struct monster_response response) {
	if (response.mr_type == mr_dead) {
		return true;
	}
	return false;
}





void processPlayerResponse(struct player_response response, struct player_message *playerMessage, struct active_monster *activeMonsters) {
	int i,j;
	bool validRequest = true;
	int x,y;
	if (response.pr_type == pr_move) {
		x = response.pr_content.move_to.x;
		y = response.pr_content.move_to.y;

		//upper wall case
		if ((x>=0) && (x <= (width_of_the_room-1)) && (y == 0)) { 
			validRequest = false;
		}
		// bottom wall case 
		if ((x>=0) && (x <= (width_of_the_room-1)) && (y == (height_of_the_room-1))) {
			validRequest = false;
		}
		// left wall case
		if ((y>=0) && (y <= (height_of_the_room-1)) && (x == 0)) {
			validRequest = false;
		}
		// right wall case
		if ((y>=0) && (y <= (height_of_the_room-1)) && (x == (width_of_the_room-1))) {
			validRequest = false;
		}
		// upper bound case
		if(y < 0) {
			validRequest = false;
		}
		// lower bound case
		if(y >= height_of_the_room) {
			validRequest = false;
		}
		// left bound case
		if(x < 0) {
			validRequest = false;
		}
		// right bound case
		if(x >= width_of_the_room) {
			validRequest = false;
		}
		// door case
		if (x == xDoor && y == yDoor) {
			validRequest = true;
		}
		// another entity case
		int i;
		for (i = 0; i<numberOfMonsters;i++) {
			if (monsters[i].x == x && activeMonsters[i].y == y) {
				validRequest = false;
			}
		}

		if(validRequest == true) {
			xPlayer = x;
			yPlayer = y;
			playerMessage->new_position.x = x;
			playerMessage->new_position.y = y;
			for(i=0;i<numberOfMonsters;i++) {
				activeMonsters[i].monsterMessage.player_coordinate.x = x;
				activeMonsters[i].monsterMessage.player_coordinate.y = y;
			}
			return;
		} else {
			return;
		}

	}
	else if (response.pr_type == pr_attack) {

		for(i=0;i<numberOfMonsters; i++) {
			if (response.pr_content.attacked[i] > 0) {
				activeMonsters[i].monsterMessage.damage = response.pr_content.attacked[i];
			}
		}
		return;
	}
	else if (response.pr_type == pr_dead) {
		return;
	}
}




bool isGameOver(struct player_response response) {
	game_over_status goStatus;
	if (response.pr_type == pr_dead) {
		printCurrentStateOfTheMap();
		goStatus = go_died;
		print_game_over(goStatus);
		return true;
	}
	if (numberOfMonsters <= 0) {
		printCurrentStateOfTheMap();
		goStatus = go_survived;
		print_game_over(goStatus);
		return true;
	}
	if (xPlayer == xDoor && yPlayer == yDoor) {
		printCurrentStateOfTheMap();
		goStatus = go_reached;
		print_game_over(goStatus);
		return true;
	}

	return false;
}


void playerLeft() {
	game_over_status goStatus;
	printCurrentStateOfTheMap();
	goStatus = go_left;
	print_game_over(goStatus);
}


void printCurrentStateOfTheMap() {

	int i;

	map_info initialMap;
	initialMap.map_width = width_of_the_room;
	initialMap.map_height = height_of_the_room;
	initialMap.door.x = xDoor;
	initialMap.door.y = yDoor;
	initialMap.player.x = xPlayer;
	initialMap.player.y = yPlayer;
	initialMap.alive_monster_count = numberOfMonsters;

	sortMonsters(activeMonsters, numberOfMonsters);


	
	for(i =0;i<numberOfMonsters;i++) {
		initialMap.monster_types[i] = activeMonsters[i].symbol;
	}


	
	for(i =0;i<numberOfMonsters;i++) {
		coordinate coord;
		coord.x = activeMonsters[i].x;
		coord.y = activeMonsters[i].y;
		initialMap.monster_coordinates[i] = coord;
	}

	

	print_map(&initialMap);
}


void stopLoop(int pidPlayer) {
//inform all alive child processes and wait for them to terminate before exiting.
	int i;
	char c;

	close(pipeForPlayer[0]);
	for (i = 0; i < numberOfMonsters; ++i) {
		close(activeMonsters[i].pipe[0]);
	}

	/*kill(pidPlayer, SIGINT);
	for(i=0;i<numberOfMonsters;i++) {
		kill(activeMonsters[i].pid, SIGINT);
	}*/


	int returnStatus;
	waitpid(pidPlayer, &returnStatus, 0);

	for(i=0;i<numberOfMonsters;i++) {
		int returnStatus2;
		waitpid(activeMonsters[i].pid, &returnStatus2,0);
	}

}
#include "logging.h"

#include <stdio.h>
#include <stdlib.h>

struct monster_sorting_info {
  map_info *mi;
  int index;
};

int cmp_monster_info(const void *a, const void *b) {
  struct monster_sorting_info *aa = (struct monster_sorting_info *)a;
  struct monster_sorting_info *bb = (struct monster_sorting_info *)b;
  coordinate aaa = aa->mi->monster_coordinates[aa->index];
  coordinate bbb = bb->mi->monster_coordinates[bb->index];
  int diff = aaa.x - bbb.x;
  if (!diff) {
    return aaa.y - bbb.y;
  }
  return diff;
}

void print_map(map_info *mi) {
  static struct monster_sorting_info sort_keys[MONSTER_LIMIT];
  for (int i = 0; i < mi->alive_monster_count; i++) {
    sort_keys[i].index = i;
    sort_keys[i].mi = mi;
  }
  bool unordered = false;
  for (int i = 1; i < mi->alive_monster_count; i++) {
    coordinate a = mi->monster_coordinates[i - 1];
    coordinate b = mi->monster_coordinates[i];
    if ((a.x > b.x) || (a.x == b.x && a.y > b.y)) {
      unordered = true;
      break;
    }
  }
  if (unordered) {
    if (getenv("check_sorted")) {
      printf("Unsorted input to print_map\n");
    }

    qsort(sort_keys, mi->alive_monster_count,
          sizeof(struct monster_sorting_info), cmp_monster_info);
  }

  printf("Map Size:%dx%d\n", mi->map_width, mi->map_height);
  printf("Door at (%d,%d)\n", mi->door.x, mi->door.y);
  printf("Player at (%d,%d)\n", mi->player.x, mi->player.y);
  printf("Alive Monster Count: %d\n", mi->alive_monster_count);
  for (int i = 0; i < mi->alive_monster_count; i++) {
    int index = sort_keys[i].index;
    printf("Monster %c at (%d,%d)\n", mi->monster_types[index],
           mi->monster_coordinates[index].x, mi->monster_coordinates[index].y);
  }
  printf("______\n");
};

void print_game_over(game_over_status go) {
  printf("Game Over\nPlayer ");
  switch (go) {
  case go_reached:
    printf("won by reaching the door\n");
    break;
  case go_survived:
    printf("won by surviving all monsters\n");
    break;
  case go_left:
    printf("lost by leaving the game\n");
    break;
  case go_died:
    printf("lost by dying\n");
    break;
  }
}

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <limits.h>
#include <stdbool.h>

#define DIFF 32 // 'a' - 'A'

typedef struct position {
    int row;
    int col;
} pos;

typedef struct order_info {
    pos player;
    pos boxpos;
    char box;
    struct order_info *next;
} ordi;

// Lines 23 - 46 stand for the implementation of stack, which will store upcoming
// instructions given at stdin.
void stack_init(ordi **h) {
    *h = NULL;
}

void stack_push(ordi **h, pos player, pos boxpos, char box) {
    ordi *new_ord = malloc((size_t) sizeof(struct order_info));
    new_ord->player = player;
    new_ord->boxpos = boxpos;
    new_ord->box = box;
    new_ord->next = *h;
    *h = new_ord;
}

void stack_pop(ordi **h) {
    ordi* temp = *h;
    *h = (*h)->next;
    free(temp);
}

bool stack_empty(ordi *h) {
    if (h == NULL)
        return true;
    return false;
}

void board_print(char **b, int *size, int lrow) {
    for (int i = 0; i <= lrow; i++) {
        for (int k = 0; k < size[i]; k++)
            printf("%c",b[i][k]);
        printf("\n");
    }
}

// Find position of first index of 2D array b, which contains char x or char y.
// If there is no x or y in the array, find_pos returns position {-1,-1}.  
pos find_pos(char **b, int *size, int lrow, char x, char y) {
    pos res;
    for (int i = 0; i <= lrow; i++) 
        for (int k = 0; k < size[i]; k++) 
            if (b[i][k] == x || b[i][k] == y) {
                res.row = i;
                res.col = k;
                return res;
            }
    res.row = -1;
    res.col = -1;
    return res;
}

// Helper function for bool path_exists. Returns true if there is at leats one path
// from b[a][b] to b[c][d], where {a,b} and {c,d} are values of variables stored in
// pos player and pos target respectively.
bool check_path(char **b, int *size, int lrow, bool **vis, pos player, pos target) {
        if (player.row == -1 || player.col == -1)
            return false;
        if (player.row == target.row && player.col == target.col) {
	          return true;
        }
        else {
            vis[player.row][player.col] = true;
            bool up = false , down = false , right = false, left = false;
            if (player.row - 1 >= 0 && size[player.row - 1] > player.col)
	              if ((!vis[player.row - 1][player.col]) &&
                    ((b[player.row - 1][player.col] == '+') ||
                     (b[player.row - 1][player.col] == '-'))) {
                    pos newpos;
                    newpos.row = player.row - 1;
                    newpos.col = player.col;
                    up = check_path(b, size, lrow, vis, newpos, target);
                }
            if (player.row + 1 <= lrow && size[player.row + 1] > player.col)
	              if ((!vis[player.row + 1][player.col]) &&
                    ((b[player.row + 1][player.col] == '+') ||
                     (b[player.row + 1][player.col] == '-'))) {
                    pos newpos;
                    newpos.row = player.row + 1;
                    newpos.col = player.col;
                    down = check_path(b, size, lrow, vis, newpos, target);
                }
            if (player.col + 1 < size[player.row])
	              if ((!vis[player.row][player.col + 1]) &&
                    ((b[player.row][player.col + 1] == '+') ||
                     (b[player.row][player.col + 1] == '-'))) {
                    pos newpos;
                    newpos.row = player.row;
                    newpos.col = player.col + 1;
                    right = check_path(b, size, lrow, vis, newpos, target);
                }
            if (player.col - 1 >= 0)
	              if ((!vis[player.row][player.col - 1]) &&
                    ((b[player.row][player.col - 1] == '+') ||
                     (b[player.row][player.col - 1] == '-'))) {
                    pos newpos;
                    newpos.row = player.row;
                    newpos.col = player.col - 1;
                    left = check_path(b, size, lrow, vis, newpos, target);
                }
	        return up || down || right || left;
        }
    }

// Function allocates a new array of bools, which is crucial in order to call
// bool check_path and, when the check_path is done, it frees all additional memory.
bool path_exists(char **b, int *size, int lrow, pos player, pos target) {
    bool **vis = NULL; // vis array stores information, if a square had been visited.
    vis = malloc((size_t) (lrow + 1) * sizeof(bool*));
    vis[0] = NULL;
    for (int i = 0; i <= lrow; i++) {
        vis[i] = malloc((size_t) size[i] * sizeof(bool));
        for (int j = 0; j < size[i]; j++) {
            vis[i][j] = false;
        }
    }
    bool res = check_path(b, size, lrow, vis, player, target);
    for (int i = 0; i <= lrow; i++)
	      free(vis[i]);
    free(vis);
    return res;
}

// Returns true if the box was succesfully moved in given direction.
bool move_box(char **b, int *size, int lrow, char box, pos player, char direction) {
    pos boxpos = find_pos(b, size, lrow, box, (char) (box - DIFF));
    pos nboxpos; // New box position (after making a move).
    pos nplayer; // Position player needs to take in order to move the box.
    bool possible = false;
    bool succeed = false;
    switch (direction) { // Check if the new position for the box exists
        case '2':        // and is able to be taken.
            if (boxpos.row + 1 <= lrow && size[boxpos.row + 1] > boxpos.col)
                if (b[boxpos.row + 1][boxpos.col] == '-' ||
                    b[boxpos.row + 1][boxpos.col] == '+' ||
                    b[boxpos.row + 1][boxpos.col] == '@' || 
                    b[boxpos.row + 1][boxpos.col] == '*') {
                    possible = true;
                    nboxpos.row = boxpos.row + 1;
                    nboxpos.col = boxpos.col;
                    nplayer.row = boxpos.row - 1;
                    nplayer.col = boxpos.col;
                }
            break;
        case '4':
            if (boxpos.col - 1 >= 0)
                if (b[boxpos.row][boxpos.col - 1] == '-' ||
                    b[boxpos.row][boxpos.col - 1] == '+' ||
                    b[boxpos.row][boxpos.col - 1] == '@' ||
                    b[boxpos.row][boxpos.col - 1] == '*') {
                    possible = true;
                    nboxpos.row = boxpos.row;
                    nboxpos.col = boxpos.col - 1;
                    nplayer.row = boxpos.row;
                    nplayer.col = boxpos.col + 1;
                }
            break;
        case '6':
            if (boxpos.col + 1 < size[boxpos.row])
                if (b[boxpos.row][boxpos.col + 1] == '-' ||
                    b[boxpos.row][boxpos.col + 1] == '+' ||
                    b[boxpos.row][boxpos.col + 1] == '@' ||
                    b[boxpos.row][boxpos.col + 1] == '*') {
                    possible = true;
                    nboxpos.row = boxpos.row;
                    nboxpos.col = boxpos.col + 1;
                    nplayer.row = boxpos.row;
                    nplayer.col = boxpos.col - 1;
                }
            break;
        case '8':
            if (boxpos.row - 1 >= 0 && size[boxpos.row - 1] > boxpos.col)
                if (b[boxpos.row - 1][boxpos.col] == '-' ||
                    b[boxpos.row - 1][boxpos.col] == '+' ||
                    b[boxpos.row - 1][boxpos.col] == '@' ||
                    b[boxpos.row - 1][boxpos.col] == '*') {
                    possible = true;
                    nboxpos.row = boxpos.row - 1;
                    nboxpos.col = boxpos.col;
                    nplayer.row = boxpos.row + 1;
                    nplayer.col = boxpos.col;
                }
            break;
    }
    if (possible && path_exists(b, size, lrow, player, nplayer)) {
        if (!(nboxpos.col == player.col && nboxpos.row == player.row)) { // Move.
            b[nboxpos.row][nboxpos.col] = b[nboxpos.row][nboxpos.col] == '-' ? box : (char) (box - DIFF);
            b[boxpos.row][boxpos.col] = b[boxpos.row][boxpos.col] == box ? '@' : '*';
            b[player.row][player.col] = b[player.row][player.col] == '@' ? '-' : '+';
            succeed = true;
        }
        else {  // Move if new pos of the box is equal to old player pos.
            b[nboxpos.row][nboxpos.col] = b[nboxpos.row][nboxpos.col] == '@' ? box : (char) (box - DIFF);
            b[boxpos.row][boxpos.col] = b[boxpos.row][boxpos.col] == box ? '@' : '*';
            succeed = true;
        }
    }
    return succeed;
}

// Undos last successful box move. Player and the box returns to their earlier positions. 
void move_undo(char **b, int *size, int lrow, ordi info) {
    pos curplayer = find_pos(b, size, lrow, '@', '*');
    pos curbox = find_pos(b, size, lrow, info.box, (char) (info.box - DIFF));
    if (!(curbox.row == info.player.row && curbox.col == info.player.col)) {
        b[info.player.row][info.player.col] = b[info.player.row][info.player.col] == '-' ? '@' : '*';
        b[curplayer.row][curplayer.col] = b[curplayer.row][curplayer.col] == '@' ? info.box : (char) (info.box - DIFF);
        b[curbox.row][curbox.col] = b[curbox.row][curbox.col] == info.box ? '-' : '+';
    }
    else { // curbox pos == player pos.
        b[curplayer.row][curplayer.col] = b[curplayer.row][curplayer.col] == '@' ? info.box : (char) (info.box - DIFF);
        b[curbox.row][curbox.col] = b[curbox.row][curbox.col] == info.box ? '@' : '*';
    }
}

int main() {
    char **p = NULL; // Pointer to a 2D array, where the playing board is kept.
    int *size = NULL; // Pointer to an array, which on i-th index holds i-th row size.
    int mul = 1; // Multiplier for reallocations.
    int lrow = 0; // The last row index.
    char x;
    while (true) { // Read the board and save it in the memory allocated at p pointer.
        int sizel = 0; // Size of current line.
        if (lrow + 1 >= mul) {
            mul *= 2; // For effective reallocation, double the memory.
            p = realloc(p, (size_t) mul * sizeof(char*));
            size = realloc(size, (size_t) mul * sizeof(int));
        }
        size[lrow] = 0;
	      p[lrow] = NULL;
        for (int i = 0; (x = (char) getchar()) != '\n' && x != EOF; i++) { // Read a line.
            if (i == sizel) {
                if (sizel == 0)
                    sizel += 1;
                sizel *= 2;
                p[lrow] = realloc(p[lrow], (size_t) sizel * sizeof(char));
            }
            p[lrow][i] = x;
            size[lrow]++;
        }
        if (x == '\n') { // Finish when an empty line appears.
            x = (char) getchar();
            if (x == '\n') {
                ungetc(x, stdin);
                break;
            }
            else {
                ungetc(x, stdin);
            }
        }
        lrow++;
    }
    ordi *h;
    stack_init(&h);
    while (x != '.') { // Read the instructions.
        x = (char) getchar();
        if (x == EOF)
            break;
        if (x == '\n') { // Print the board instruction.
            board_print(p, size, lrow);
        }
        else if (x >= 'a' && x <= 'z') { // Move the box instruction.
            char box = x;
            x = (char) getchar();
            pos player = find_pos(p, size, lrow, '@', '*');
            pos boxpos = find_pos(p, size, lrow, box, (char) (box - DIFF));
            if (move_box(p, size, lrow, box, player, x)) // If move appears, save to stack.
                stack_push(&h, player, boxpos, box);
        }
        else {  // x == '0' - undo instruction.
            if (!stack_empty(h)) {
                move_undo(p, size, lrow, *h);
                stack_pop(&h);
            }
        }
    }
    while (h != NULL) { // Free the ordi stack.
	    ordi *temp = h;
	    h = h->next;
	    free(temp);
    }
    for (int i = 0; i <= lrow; i++) // 311 - 314: Free the board and the size array.
        free(p[i]);
    free(p);
	  free(size);
    return 0;
}

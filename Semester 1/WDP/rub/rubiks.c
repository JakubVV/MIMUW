#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#define F 0     // FRONT (cube's frontal wall).
#define B 1     // BACK  (cube's back wall).
#define L 2     // LEFT  (cube's left wall).
#define R 3     // RIGHT (cube's right wall).
#define U 4     // UP (cube's upper wall).
#define D 5     // DOWN  (cube's bottom wall).
#define W 6     // number of walls in a cube.
// cube size 
#ifndef N       
#define N 5
#endif

typedef struct instruction {
    int layers;
    int wall;
    char angle;
} instr;

// Function rotates given matrix by given angle.
void rotateWall(char A[N][N],  char angle) {
    char temp[N][N];
    switch (angle) {
        case 'r':   // Right angle.
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++) {
                    temp[i][j] = A[N - 1 - j][i];
                }
        break;
        case 's':   // Straight angle.
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++) {
                    temp[i][j] = A[N - 1 - i][N - 1 - j];
                }
        break;
        case 'm':   // -90 degree angle.
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++) {
                    temp[i][j] = A[j][N - 1 - i];
                }
        break;
    }
    for (int i = 0; i < N; i++)
        for (int j = 0; j < N; j++) {
            A[i][j] = temp[i][j];
        }
}
// UPPER / DOWN walls rotations function for angles (90, -90, 180). 
void rotateUD(char A[W][N][N], int lay, char angle, int wall) { 
    char temp;
    int order[W - 2] = {0, 0, 0, 0};
    if (wall == D) {    // Bottom wall.
        if (angle == 'r') { // Right angle.
            order[0] = L;
            order[1] = F;
            order[2] = R;
            order[3] = B;
            rotateWall(A[D], 'r');
            if (lay == N)
                rotateWall(A[U], 'm');
        }
        else if (angle == 'm') {    // -90 degree angle.   
            order[0] = B;
            order[1] = R;
            order[2] = F;
            order[3] = L;
            rotateWall(A[D], 'm');
            if (lay == N)
                rotateWall(A[U], 'r');
        }
        if (angle != 's')
            for (int k = N - 1; k > N - 1 - lay; k--)
                for (int i = 1; i < W - 2; i++) {
                    for (int j = 0; j < N; j++) {
                        temp = A[order[0]][k][j];
                        A[order[0]][k][j] = A[order[i]][k][j];
                        A[order[i]][k][j] = temp;
                    }
                }
        else {  // Straight angle.
            for (int k = N - 1; k > N - 1 - lay; k--)
                for (int j = 0; j < N; j++) {
                    temp = A[F][k][j];
                    A[F][k][j] = A[B][k][j];
                    A[B][k][j] = temp;
                    temp = A[L][k][j];
                    A[L][k][j] = A[R][k][j];
                    A[R][k][j] = temp;
                } 
            rotateWall(A[D], 's');
            if (lay == N)
                rotateWall(A[U], 's');
        }
    }
    else {  // Upper wall.
        if (angle == 'r') { // Right angle.
            order[0] = B;
            order[1] = R;
            order[2] = F;
            order[3] = L;
            rotateWall(A[U], 'r');
            if (lay == N)
                rotateWall(A[D], 'm');
        }
        else if (angle == 'm') { // -90 degree angle.   
            order[0] = L;
            order[1] = F;
            order[2] = R;
            order[3] = B;
            rotateWall(A[U], 'm');
            if (lay == N)
                rotateWall(A[D], 'r');
        }
        if (angle != 's')
            for (int k = 0; k < lay; k++)
                for (int i = 1; i < W - 2; i++) {
                    for (int j = 0; j < N; j++) {
                        temp = A[order[0]][k][j];
                        A[order[0]][k][j] = A[order[i]][k][j];
                        A[order[i]][k][j] = temp;
                    }
                }
        else {  // Straight angle.
            for (int k = 0; k < lay; k++)
                for (int j = 0; j < N; j++) {
                    temp = A[F][k][j];
                    A[F][k][j] = A[B][k][j];
                    A[B][k][j] = temp;
                    temp = A[L][k][j];
                    A[L][k][j] = A[R][k][j];
                    A[R][k][j] = temp;
                } 
            rotateWall(A[U], 's');
            if (lay == N)
                rotateWall(A[D], 's');
        }
    }
 }
// LEFT / RIGHT walls rotations function for angles (90, -90, 180). 
void rotateLR(char A[W][N][N], int lay, char angle, int wall) {
    char temp;
    int order[W - 2] = {0,0,0,0};
    if (wall == L) {    // Left wall.
        if (angle == 'r') { // Right angle.
            order[0] = U;
            order[1] = F;
            order[2] = D;
            order[3] = B;
            rotateWall(A[L], 'r');
            if (lay == N)
                rotateWall(A[R], 'm');
        }
        else if (angle == 'm') {  // -90 degree angle.   
            order[0] = B;
            order[1] = D;
            order[2] = F;
            order[3] = U;
            rotateWall(A[L], 'm');
            if (lay == N)
                rotateWall(A[R], 'r');
        }
        if (angle != 's')
            for (int k = 0; k < lay; k++)
                for (int i = 1; i < W - 2; i++) {
                    for (int j = 0; j < N; j++) {
                        if (order[i] != B && order[0] != B) {
                            temp = A[order[0]][j][k];
                            A[order[0]][j][k] = A[order[i]][j][k];
                            A[order[i]][j][k] = temp;
                        }
                        else if (order[i] == B) {
                            temp = A[order[0]][j][k];                         
                            A[order[0]][j][k] = A[order[i]][N - 1 - j][N - 1 - k];
                            A[order[i]][N - 1 - j][N - 1 - k] = temp;
                        }
                        else {
                            temp = A[order[0]][N - 1 - j][N - 1 - k];
                            A[order[0]][N - 1 - j][N - 1 - k] = A[order[i]][j][k];
                            A[order[i]][j][k] = temp;
                        }
                    }
                }
        else {  // Straight angle.
            for (int k = 0; k < lay; k++)
                for (int j = 0; j < N; j++) {
                    temp = A[U][j][k];
                    A[U][j][k] = A[D][j][k];
                    A[D][j][k] = temp;
                    temp = A[F][j][k];
                    A[F][j][k] = A[B][N - 1 - j][N - 1 - k];
                    A[B][N - 1 - j][N - 1 - k] = temp;
                } 
            rotateWall(A[L], 's');
            if (lay == N)
                rotateWall(A[R], 's');
        }
    }
    else {  // RIGHT WALL.
        if (angle == 'r') { // Right angle.
            order[0] = B;
            order[1] = D;
            order[2] = F;
            order[3] = U;
            rotateWall(A[R], 'r');
            if (lay == N)
                rotateWall(A[L], 'm');
        }
        else if (angle == 'm') {  // -90 degree angle.  
            order[0] = U;
            order[1] = F;
            order[2] = D;
            order[3] = B;
            rotateWall(A[R], 'm');
            if (lay == N)
                rotateWall(A[L], 'r');
        }
        if (angle != 's')
            for (int k = 0; k < lay; k++)
                for (int i = 1; i < W - 2; i++) {
                    for (int j = 0; j < N; j++) {
                        if (order[i] != B && order[0] != B) {
                            temp = A[order[0]][j][N - 1 - k];
                            A[order[0]][j][N - 1 - k] = A[order[i]][j][N - 1 - k];
                            A[order[i]][j][N - 1 - k] = temp;
                        }
                        else if (order[i] == B) {
                            temp = A[order[0]][j][N - 1 - k];                         
                            A[order[0]][j][N - 1 - k] = A[order[i]][N - 1 - j][k];
                            A[order[i]][N - 1 - j][k] = temp;
                        }
                        else {
                            temp = A[order[0]][N - 1 - j][k];
                            A[order[0]][N - 1 - j][k] = A[order[i]][j][N - 1 - k];
                            A[order[i]][j][N - 1 - k] = temp;
                        }
                    }
                }
        else {  // Straight angle.
            for (int k = 0; k < lay; k++)
                for (int j = 0; j < N; j++) {
                    temp = A[U][j][N - 1 - k];
                    A[U][j][N - 1 - k] = A[D][j][N - 1 - k];
                    A[D][j][N - 1 - k] = temp;
                    temp = A[F][j][N - 1 - k];
                    A[F][j][N - 1 - k] = A[B][N - 1 - j][k];
                    A[B][N - 1 - j][k] = temp;
                } 
            rotateWall(A[R], 's');
            if (lay == N)
                rotateWall(A[L], 's');
        }
    }    
}
// FRONT / BACK walls rotations function for angles (90, -90, 180).
void rotateFB(char A[W][N][N], int lay, char angle, int wall) { 
    char temp;
    if (wall == F) {  // FRONTAL WALL.
        if (angle == 'r') {  // Right angle.
            for (int k = 0; k < lay; k++)
                for (int j = 0; j < N; j++) {
                    temp = A[R][j][k];
                    A[R][j][k] = A[D][k][N - 1 - j];
                    A[D][k][N - 1 - j] = temp;
                    temp = A[R][j][k];
                    A[R][j][k] = A[L][N - 1 - j][N - 1 - k];
                    A[L][N - 1 - j][N - 1 - k] = temp;
                    temp = A[R][j][k];
                    A[R][j][k] = A[U][N - 1 - k][j];
                    A[U][N - 1 - k][j] = temp;
                }
            rotateWall(A[F], angle);
            if (lay == N)
                rotateWall(A[B], 'm');
        }
        else if (angle == 'm') {  // -90 degree angle.
            for (int k = 0; k < lay; k++)
                for (int j = 0; j < N; j++) {
                    temp = A[R][j][k];
                    A[R][j][k] = A[U][N - 1 - k][j];
                    A[U][N - 1 - k][j] = temp;
                    temp = A[R][j][k];
                    A[R][j][k] = A[L][N - 1 - j][N - 1 - k];
                    A[L][N - 1 - j][N - 1 - k] = temp;
                    temp = A[R][j][k];
                    A[R][j][k] = A[D][k][N - 1 - j];
                    A[D][k][N - 1 - j] = temp;
                }
            rotateWall(A[F], angle);
            if (lay == N)
                rotateWall(A[B], 'r');
        }
        else {  // Straight angle.
            for (int k = 0; k < lay; k++)
                for (int j = 0; j < N; j++) {
                    temp = A[R][j][k];
                    A[R][j][k] = A[L][N - 1 - j][N - 1 - k];
                    A[L][N - 1 - j][N - 1 - k] = temp;
                    temp = A[U][N - 1 - k][j];
                    A[U][N - 1 - k][j] = A[D][k][N - 1 - j];
                    A[D][k][N - 1 - j] = temp;
                }
            rotateWall(A[F], angle);
            if (lay == N)
                rotateWall(A[B], angle);  
        }
    }
    else {  // BACK WALL.
        if (angle == 'r') {  // Right angle.
            for (int k = 0; k < lay; k++)
                for (int j = 0; j < N; j++) {
                    temp = A[U][k][j];
                    A[U][k][j] = A[L][N - 1 - j][k];
                    A[L][N - 1 - j][k] = temp;
                    temp = A[U][k][j];
                    A[U][k][j] = A[D][N - 1 - k][N - 1 - j];
                    A[D][N - 1 - k][N - 1 - j] = temp;
                    temp = A[U][k][j];
                    A[U][k][j] = A[R][j][N - 1 - k];
                    A[R][j][N - 1 - k] = temp;
                }
            rotateWall(A[B], angle);
            if (lay == N)
                rotateWall(A[F], 'm');
        }
        else if (angle == 'm') {  // -90 degree angle.  
            for (int k = 0; k < lay; k++)
                for (int j = 0; j < N; j++) {
                    temp = A[U][k][j];
                    A[U][k][j] = A[R][j][N-1-k];
                    A[R][j][N - 1 - k] = temp;
                    temp = A[U][k][j];
                    A[U][k][j] = A[D][N - 1 - k][N - 1 - j];
                    A[D][N - 1 - k][N - 1 - j] = temp;
                    temp = A[U][k][j];
                    A[U][k][j] = A[L][N - 1 - j][k];
                    A[L][N - 1 - j][k] = temp;
                }
            rotateWall(A[B], angle);
            if (lay == N)
                rotateWall(A[F], 'r');
        }
        else {  // Straight angle.
            for (int k = 0; k < lay; k++)
                for (int j = 0; j < N; j++) {
                    temp = A[U][k][j];
                    A[U][k][j] = A[D][N - 1 - k][N - 1 - j];
                    A[D][N - 1 - k][N - 1 - j] = temp;
                    temp = A[L][j][k];
                    A[L][j][k] = A[R][N - 1 - j][N - 1 - k];
                    A[R][N - 1 - j][N - 1 - k] = temp;
                } 
            rotateWall(A[B], angle);
            if (lay == N)
                rotateWall(A[F],angle);
        }
    }
}
// 'printCube' prints an entire cube according to given specifications.
void printCube(char A[W][N][N]) {
    for (int i = 0; i < N; i++) {
        printf("\n");
        for (int j = 0; j < N + 1; j++)
            printf(" ");
        for (int j = 0; j < N; j++)
            printf("%c", A[U][i][j]);
    }
    printf("\n");
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++)
            printf("%c", A[L][i][j]);
        printf("|");
        for (int j = 0; j < N; j++)
            printf("%c", A[F][i][j]);
        printf("|");
        for (int j = 0; j < N; j++)
            printf("%c", A[R][i][j]);
        printf("|");
        for (int j = 0; j < N; j++)
            printf("%c", A[B][i][j]);
        printf("\n");
    }
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N + 1; j++)
            printf(" ");
        for (int j = 0; j < N; j++)
            printf("%c", A[D][i][j]);
        printf("\n");
    }
}



int main() {
    char CUBE[W][N][N];
    char x; // 'x' variable holds currently analysed character.
    for (int i = 0; i < N; i++) // Filling the cube with starting values as given. 
        for (int j = 0; j < N; j++) {
            CUBE[U][i][j] = '0';
            CUBE[L][i][j] = '1';
            CUBE[F][i][j] = '2';
            CUBE[R][i][j] = '3';
            CUBE[B][i][j] = '4';
            CUBE[D][i][j] = '5';
        }
    /*  Every while-loop iteration takes one instruction given in std-in
        and executes it. */
    while (true) {
        instr i;    // Initiate one new instruction.
        x = (char) getchar();
        if (x == '\n')
            printCube(CUBE);
        else if (x == '.')
            break; 
        else {
            if (x >= '0' && x <= '9') { // Number of layers.
                ungetc(x, stdin);
                scanf("%d", &i.layers);
            }
            else {
                i.layers = 1;
                ungetc(x, stdin);
            }
            x = (char) getchar();
            switch (x) {    // Wall number.
                case 'u':
                    i.wall = U;
                break;
                case 'l':
                    i.wall = L;
                break;
                case 'f':
                    i.wall = F;
                break;
                case 'r':
                    i.wall = R;
                break;
                case 'b':
                    i.wall = B;
                break;
                case 'd':
                    i.wall = D;
                break;
            }
            x = (char) getchar();
            switch (x) {    // Angle.
                case 39:    // Apostrophe given as ASCII for better readability.
                    i.angle = 'm';
                break;
                case '"':
                    i.angle = 's';
                break;
                default:
                    i.angle = 'r';
                    ungetc(x, stdin);
                break;
            } 
            if (i.wall == U || i.wall == D) // Execute the instruction.
                rotateUD(CUBE, i.layers, i.angle, i.wall);
            else if (i.wall == L || i.wall == R)
                rotateLR(CUBE, i.layers, i.angle, i.wall);
            else 
                rotateFB(CUBE, i.layers, i.angle, i.wall);
        }
    }
    return 0;
}

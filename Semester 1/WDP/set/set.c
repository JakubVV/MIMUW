#include <stdio.h>
#include <stdbool.h>

#define N 81    // Max number of cards in the deck.
#define S 12    // Default number of cards on the table.
#define L 4     // Number of attributes of each card.

static bool isset(int a, int b, int c) { 
    const int d = 10;                    
    if (a == 0 || b == 0 || c == 0)
    return false;
    for (int i = 0; i < L; i++) {
        int x = a % d;
        int y = b % d;
        int z = c % d;
        if (!((x == y && x == z) ||           
              (x != y && x != z && y != z)))  
            return false;
        a /= d;
        b /= d;
        c /= d;
    }
    return true;
}

static void reiterate(int A[]) { 
    int zer, i, c;               
    zer = i = 0;
    while (i < N) {
        while (zer < N && A[zer] != 0)
            zer++;
        i = zer + 1;
        while (i < N && A[i] == 0)
            i++;
        if (i < N && zer < N) {
		        c = A[i];
            A[i] = A[zer];
        	  A[zer] = c;
	      }
    }
}

int main() {
    int TAL[N];
    int il = 0;     
    bool stop = true;
    for (int i = 0; i < N; i++)
        TAL[i] = 0;
    while (scanf("%d", &TAL[il]) != EOF)
        il++;
    if (il > S)
        il = S;
    printf("=");
    for (int i = 0; i < il; i++)
        printf(" %d", TAL[i]);
    printf("\n");
    while (true) {
        stop = true;                              
        for (int i = 0; i < il - 2 && stop; i++) 
            for (int j = i + 1; j < il - 1 && stop; j++)
                for (int k = j + 1; k < il && stop; k++)
                    if (isset(TAL[i], TAL[j], TAL[k])) {
                        printf("- %d %d %d\n", TAL[i], TAL[j], TAL[k]);
                        TAL[i] = TAL[j] = TAL[k] = 0;
                        stop = false;
                        reiterate(TAL);
                        if (TAL[il - 1] == 0 || il > 12)  
                            il -= 3;                     
                    }
        if (stop && TAL[il] != 0) {    
            il += 3;                   
            printf("+\n");
        }
        else if (stop && TAL[il] == 0) {    
            break;                          
        }
        printf("=");
        for (int i = 0; i < il; i++)    
            printf(" %d", TAL[i]);      
        printf("\n");
    }
    return 0;
}

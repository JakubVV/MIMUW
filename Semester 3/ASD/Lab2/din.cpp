#include <iostream>
#include <limits.h>

int mini(int a, int b, int c) {
    if (a <= b && a <= c)
        return a;
    if (b <= a && b <= c)
        return b;
    return c;
}

int mod(int a, int b) {
    int r = a % b;
    if (r < 0)
        return r + b;
    return r;
}

using namespace std;

int main() {
    int n, m;
    cin >> n >> m;
    int dp[20] = {0};
    int traps[m + 1];
    int counter = 1;
    int currentTrap = 0;
    for (int i = 0; i < m; i++)
        cin >> traps[i];
    traps[m] = n + 1;
    int prevTrap = -1;
    while (counter <= n) {
        if ( (currentTrap < m) && traps[currentTrap] == counter) {
            dp[mod(counter, 20)] = -1;
            currentTrap++;
            prevTrap = currentTrap - 1;
        }
        else {
            int one = INT_MAX;
            int five = INT_MAX;
            int ten = INT_MAX;
            if (counter - 1 >= 0) {
                if (dp[mod(counter - 1, 20)] != -1)
                    one = dp[mod(counter - 1, 20)] + 1;
            }
            if (counter - 5 >= 0) {
                if (dp[mod(counter - 5, 20)] != -1)
                    five = dp[mod(counter - 5, 20)] + 10;
            }

            if (counter - 10 >= 0) {
                if (dp[mod(counter - 10, 20)] != -1)
                    ten = dp[mod(counter - 10, 20)] + 100;
            }
            dp[mod(counter, 20)] = mini(one, five, ten);
            if (dp[mod(counter, 20)] == INT_MAX)
                dp[mod(counter, 20)] = -1;
            
            if (prevTrap >= 0 && currentTrap < m && counter - traps[prevTrap] > 20 && traps[currentTrap] - counter > 50) {
                int diff = traps[currentTrap] - counter - 10;
                diff -= diff % 20;
                int c = dp[mod(counter, 20)] + diff;
                for (int i = mod(counter, 20); i != mod(counter + 1, 20); i = mod(i - 1, 20)) {
                    dp[i] = c;
                    c--;
                }
                counter += diff;
            }
        }
        counter++;
    }
    cout << dp[mod(n, 20)];
}

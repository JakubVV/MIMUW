#include <bits/stdc++.h>

using namespace std;

int MOD = 1000000007;

long long mathMod(long long a, long long b) {
    long long r = a % b;
    if (r < 0)
        return r + b;
    return r;
}

int main() {
    int n;
    
    cin >> n;
  
    vector<long long> arr(n);
    vector<long long> suf(n, 0);
  
    for (int i = 0; i < n; i++)
        cin >> arr[i];
  
    suf[n - 1] = arr[n - 1];
  
    for (int i = n - 2; i >= 0; i--) {
        suf[i] = (suf[i + 1] + arr[i]) % MOD;
    }
  
    long long squares = 0;
    for (int i = 0; i < arr.size(); i++) {
        long long newSquare = (n - 1) * (arr[i] * arr[i]) % MOD;
        squares = (squares + newSquare) % MOD;
    }
  
    long long minuses = 0;
    for (int i = 0; i < n - 1; i++) {
        long long newMinus = (arr[i] * suf[i + 1]) % MOD;
        minuses = (minuses + newMinus) % MOD;
    }
  
    minuses = (minuses * (2)) % MOD;
    minuses = mathMod((minuses * (-1)), MOD);
    long long result = mathMod(squares + minuses, MOD);
    
    cout << result << endl;

    return 0;
}

#include<iostream>
#include<vector>
#include<limits.h>

using namespace std;

int main() {
    int n, K;
    cin >> n >> K;
    vector<long long> a(n);
    for (int i = 0; i < n; i++) 
        cin >> a[i];
    const long long NaN = LLONG_MIN / 5;
    vector<vector<long long>> dp_prev(2, vector<long long>(K + 1, NaN));
    dp_prev[0][0] = 0;
    for (int i = 0; i < n; i++) {
        long long x = a[i];
        vector<vector<long long>> dp_curr(2, vector<long long>(K + 1, NaN));
        for (int used = 0; used <= 1; used++) {
            for (int cur = 0; cur <= K; cur++) {
                long long val = dp_prev[used][cur];
                if (val <= NaN / 2) 
                    continue;
                dp_curr[used][0] = max(dp_curr[used][0], val);
                if (cur < K) {
                    int new_cur = cur + 1;
                    if (new_cur == K) {
                        if (used == 0) {
                            dp_curr[1][K] = max(dp_curr[1][K], val + x);
                        }
                    } 
                    else {
                        dp_curr[used][new_cur] = max(dp_curr[used][new_cur], val + x);
                    }
                }
            }
        }
        dp_prev.swap(dp_curr);
    }
    long long res = 0;
    for (int used = 0; used <= 1; used++)
        for (int cur = 0; cur <= K; cur++)
            res = max(res, dp_prev[used][cur]);
    cout << res << "\n";
    return 0;
}

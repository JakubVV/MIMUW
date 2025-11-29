#include <iostream>
#include <vector>
#include <algorithm>

using namespace std;

int main() {
    const long long MOD = 1000000000;
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int n, m, k, g;
    if (!(cin >> n >> m >> k >> g))
        return 0;

    vector<vector<char>> hasMush(n + 1, vector<char>(m + 1, 0));
    for (int i = 0; i < g; ++i) {
        int a, b; 
        cin >> a >> b;
        if (a >= 1 && a <= n && b >= 1 && b <= m) 
            hasMush[a][b] = 1;
    }

    vector<vector<long long>> dp_prev(n + 1, vector<long long>(k + 1, 0));
    vector<vector<long long>> dp_curr(n + 1, vector<long long>(k + 1, 0));

    dp_prev[1][0] = 1;

    long long result = 0;

    for (int col = 1; col <= m; ++col) {
        for (int i = 1; i <= n; ++i) 
            fill(dp_curr[i].begin(), dp_curr[i].end(), 0);

        vector<int> pref(n + 1, 0);
        for (int r = 1; r <= n; ++r) 
            pref[r] = pref[r - 1] + (hasMush[r][col] ? 1 : 0);

        for (int j = 0; j <= k; ++j) {
            vector<long long> S(n + 1, 0);
            int ps_ptr = 0;
            long long ps_sum = 0;
            for (int v = 1; v <= n; ++v) {
                int add_idx = pref[v - 1];
                if (dp_prev[v][j]) {
                    long long val = dp_prev[v][j];
                    S[add_idx] = (S[add_idx] + val) % MOD;
                    if (add_idx < ps_ptr) ps_sum = (ps_sum + val) % MOD; 
                }
        
                for (int delta = 0; delta < k; ++delta) {
                    int p = pref[v] - delta;
                    if (p < 0) break;
                    long long sum = S[p];
                    if (sum) {
                        int nj = min(k, j + delta);
                        dp_curr[v][nj] = (dp_curr[v][nj] + sum) % MOD;
                    }
                }
                int thr = pref[v] - k;
                if (thr >= 0) {
                    while (ps_ptr <= thr) {
                        ps_sum = (ps_sum + S[ps_ptr]) % MOD;
                        ++ps_ptr;
                    }
                    if (ps_sum) dp_curr[v][k] = (dp_curr[v][k] + ps_sum) % MOD;
                }
            }

            vector<long long> R(n + 1, 0);
            int ss_ptr = n + 1;
            long long ss_sum = 0;
            for (int v = n; v >= 1; --v) {
                if (v < n) {
                    int add_idx = pref[v + 1];
                    if (dp_prev[v + 1][j]) {
                        R[add_idx] = (R[add_idx] + dp_prev[v + 1][j]) % MOD;
                        if (add_idx >= ss_ptr) {
                            ss_sum = (ss_sum + dp_prev[v + 1][j]) % MOD;
                        }
                    }
                }
                for (int delta = 0; delta < k; ++delta) {
                    int p = pref[v - 1] + delta;
                    if (p > n) break;
                    long long sum = R[p];
                    if (sum) {
                        int nj = min(k, j + delta);
                        dp_curr[v][nj] = (dp_curr[v][nj] + sum) % MOD;
                    }
                }
                int th = pref[v - 1] + k;
                if (th <= n) {
                    while (ss_ptr - 1 >= th) {
                        --ss_ptr;
                        ss_sum = (ss_sum + R[ss_ptr]) % MOD;
                    }
                    if (ss_sum) 
                        dp_curr[v][k] = (dp_curr[v][k] + ss_sum) % MOD;
                }
            }
        }

        if (col == m) {
            long long sum = 0;
            for (int t = 0; t <= k; ++t) {
                if (t >= k) sum = (sum + dp_curr[n][t]) % MOD;
            }
            cout << (sum % MOD) << '\n';
            return 0;
        }

        for (int i = 1; i <= n; ++i) {
            for (int t = 0; t <= k; ++t) 
                dp_prev[i][t] = dp_curr[i][t];
        }
    }

    cout << 0 << endl;
    return 0;
}

#include <iostream>
#include <vector>
#include <algorithm>

using namespace std;

static bool cmp(const pair<int,int>& a, const pair<int,int>& b) {
    if (a.first != b.first) return a.first < b.first;
    return a.second < b.second;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    int n;
    long long m;
    cin >> n >> m;

    vector<long long> R(n), T(n), pos;
    pos.reserve(2*n);
    for (int i = 0; i < n; ++i) { 
        cin >> R[i];
        pos.push_back(R[i]); 
    }
    for (int i = 0; i < n; ++i) { 
        cin >> T[i]; 
        pos.push_back(T[i]); 
    }

    sort(pos.begin(), pos.end());
    pos.erase(unique(pos.begin(), pos.end()), pos.end());
    int k = (int)pos.size();

    vector<long long> diff(k, 0);
    for (auto x : R) {
        int idx = int(lower_bound(pos.begin(), pos.end(), x) - pos.begin());
        diff[idx]--; // cele - roboty
    }
    for (auto x : T) {
        int idx = int(lower_bound(pos.begin(), pos.end(), x) - pos.begin());
        diff[idx]++;
    }

    vector<long long> w(k);
    for (int i = 0; i + 1 < k; ++i) 
        w[i] = pos[i + 1] - pos[i];
    w[k - 1] = pos[0] + m - pos[k - 1];

    vector<long long> A(k);
    long long s = 0;
    for (int i = 0; i < k; ++i) {
        s += diff[i];
        A[i] = s;
    }
    
    vector<pair<long long,long long>> pts;
    pts.reserve(k);
    long long totalW = 0;
    for (int i = 0; i < k; ++i) {
        pts.emplace_back(-A[i], w[i]);
        totalW += w[i];
    }
    sort(pts.begin(), pts.end(), cmp);
    long long half = (totalW + 1) / 2;
    long long C = 0, acc = 0;
    for (auto [x, ww] : pts) {
        acc += ww;
        if (acc >= half) {
            C = x; break; 
        }
    }
    long long res = 0;
    for (int i = 0; i < k; ++i) {
        long long f = A[i] + C;
        res += w[i] * abs(f);
    }
    cout << res << "\n";
    return 0;
}

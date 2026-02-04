#include <bits/stdc++.h>
using namespace std;

using ull = unsigned long long;
using ll = long long;

const ull B1 = 911382323;
const ull B2 = 972663749;
const ull M1 = 1000000007ULL;
const ull M2 = 1000000009ULL;

vector<ull> h1, h2, p1, p2;
string s;
int n;

pair<ull, ull> get_hash(int l, int r) {
    ull x1 = (h1[r] + M1 - (h1[l-1] * p1[r-l+1] % M1)) % M1;
    ull x2 = (h2[r] + M2 - (h2[l-1] * p2[r-l+1] % M2)) % M2;
    return {x1, x2};
}

int LCP(int a, int b, int c, int d) {
    int L = min(b - a + 1, d - c + 1);
    int lo = 0, hi = L;
    while (lo < hi) {
        int mid = (lo + hi + 1) / 2;
        if (get_hash(a, a + mid - 1) == get_hash(c, c + mid - 1))
            lo = mid;
        else
            hi = mid - 1;
    }
    return lo;
}

int LCS(int a, int b, int c, int d) {
    int L = min(b - a + 1, d - c + 1);
    int lo = 0, hi = L;
    while (lo < hi) {
        int mid = (lo + hi + 1) / 2;
        if (get_hash(b - mid + 1, b) == get_hash(d - mid + 1, d))
            lo = mid;
        else
            hi = mid - 1;
    }
    return lo;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int m;
    cin >> n >> m;
    cin >> s;
    s = " " + s; 

    h1.resize(n+1);
    h2.resize(n+1);
    p1.resize(n+1);
    p2.resize(n+1);

    p1[0] = p2[0] = 1;
    for (int i = 1; i <= n; i++) {
        p1[i] = (p1[i-1] * B1) % M1;
        p2[i] = (p2[i-1] * B2) % M2;
        h1[i] = (h1[i-1] * B1 + (s[i] - 'a' + 1)) % M1;
        h2[i] = (h2[i-1] * B2 + (s[i] - 'a' + 1)) % M2;
    }

    while (m--) {
        int a,b,c,d;
        cin >> a >> b >> c >> d;

        int L1 = b - a + 1;
        int L2 = d - c + 1;

        if (abs(L1 - L2) >= 2) {
            cout << 0 << "\n";
            continue;
        }

        if (L1 == L2) {
            int lcp = LCP(a, b, c, d);
            if (lcp == L1) {
                cout << 1 << "\n";
                continue;
            }
            int lcs = LCS(a, b, c, d);
            if (lcp + lcs >= L1 - 1)
                cout << 1 << "\n";
            else
                cout << 0 << "\n";
            continue;
        }

        if (L1 < L2) {
            swap(a, c);
            swap(b, d);
            swap(L1, L2);
        }

        int lcp = LCP(a, b, c, d);
        if (lcp == L2) {
            cout << 1 << "\n";
            continue;
        }
        int lcs = LCS(a, b, c, d);

        if (lcp + lcs >= L2)
            cout << 1 << "\n";
        else
            cout << 0 << "\n";
    }

    return 0;
}

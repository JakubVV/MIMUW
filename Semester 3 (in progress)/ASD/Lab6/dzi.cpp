#include <iostream>
#include <vector>
#include <algorithm>

using namespace std;

struct Fenwick {
    int n;
    vector<int> bit;
    Fenwick(int n) : n(n), bit(n + 1, 0) {}

    void add(int i, int v) {
        for (; i <= n; i += i & -i)
            bit[i] += v;
    }

    int sum(int i) {
        int s = 0;
        for (; i > 0; i -= i & -i)
            s += bit[i];
        return s;
    }

    int range_sum(int l, int r) {
        if (r < l) return 0;
        return sum(r) - sum(l - 1);
    }
};

struct Event {
    long long x;
    int yb, yt, sign;
    int qi;
};

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int n, m;
    cin >> n >> m;

    vector<pair<long long,long long>> pts(n);
    vector<long long> ys;

    for(int i = 0; i < n; i++) {
        cin >> pts[i].first >> pts[i].second;
        ys.push_back(pts[i].second);
    }

    struct Query { long long xl, xr, yb, yt; };
    vector<Query> queries(m);

    for(int i = 0; i < m; i++) {
        cin >> queries[i].xl >> queries[i].xr >> queries[i].yb >> queries[i].yt;
        ys.push_back(queries[i].yb);
        ys.push_back(queries[i].yt);
    }

    sort(ys.begin(), ys.end());
    ys.erase(unique(ys.begin(), ys.end()), ys.end());

    auto getY = [&](long long y) {
        return int(lower_bound(ys.begin(), ys.end(), y) - ys.begin()) + 1;
    };

    sort(pts.begin(), pts.end());

    vector<Event> ev;
    ev.reserve(2*m);

    for(int i = 0; i < m; i++) {
        int yb = getY(queries[i].yb);
        int yt = getY(queries[i].yt);

        ev.push_back({queries[i].xr, yb, yt, +1, i});
        ev.push_back({queries[i].xl - 1, yb, yt, -1, i});
    }

    sort(ev.begin(), ev.end(), [](auto &a, auto &b) {
        return a.x < b.x;
    });

    Fenwick bit(ys.size());
    vector<long long> res(m, 0);

    int p = 0;

    for (auto &e : ev) {
        while (p < n && pts[p].first <= e.x) {
            bit.add(getY(pts[p].second), 1);
            p++;
        }
        long long cnt = bit.range_sum(e.yb, e.yt);
        res[e.qi] += e.sign * cnt;
    }

    for (int i = 0; i < m; i++)
        cout << res[i] << "\n";
}

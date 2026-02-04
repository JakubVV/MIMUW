#include <bits/stdc++.h>
using namespace std;

const int MAXN = 200000;
vector<int> g[MAXN + 5];
int parent[20][MAXN + 5];
int depth[MAXN + 5];
int n, q;

void dfs(int v, int p) {
    parent[0][v] = p;
    for (int u : g[v]) {
        if (u == p) continue;
        depth[u] = depth[v] + 1;
        dfs(u, v);
    }
}

int lca(int a, int b) {
    if (depth[a] < depth[b]) swap(a, b);
    int diff = depth[a] - depth[b];

    for (int i = 0; i < 20; i++)
        if (diff & (1 << i))
            a = parent[i][a];

    if (a == b) return a;

    for (int i = 19; i >= 0; i--) {
        if (parent[i][a] != parent[i][b]) {
            a = parent[i][a];
            b = parent[i][b];
        }
    }
    return parent[0][a];
}

inline int dist(int a, int b) {
    int c = lca(a, b);
    return depth[a] + depth[b] - 2*depth[c];
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    cin >> n >> q;

    for (int i = 1; i <= n; i++) g[i].clear();

    for (int i = 0; i < n - 1; i++) {
        int u, v;
        cin >> u >> v;
        g[u].push_back(v);
        g[v].push_back(u);
    }

    depth[1] = 0;
    dfs(1, 0);

    for (int i = 1; i < 20; i++)
        for (int v = 1; v <= n; v++)
            parent[i][v] = parent[i-1][ parent[i-1][v] ];

    while (q--) {
        int a, b, c;
        cin >> a >> b >> c;

        long long dab = dist(a, b);
        long long dbc = dist(b, c);
        long long dca = dist(c, a);

        long long result = (dab + dbc + dca) / 2;

        cout << result << "\n";
    }

    return 0;
}

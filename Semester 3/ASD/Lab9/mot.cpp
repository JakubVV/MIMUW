#include <bits/stdc++.h>
using namespace std;

const int INF = 1e9;

int main() {
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);
    
    int n, m;
    cin >> n >> m;
    
    vector<vector<pair<int, int>>> graph(n), reverse_graph(n);
    
    for (int i = 0; i < m; i++) {
        int u, v, w;
        cin >> u >> v >> w;
        graph[u].push_back({v, w});
        reverse_graph[v].push_back({u, w});
    }
    
    auto dijkstra = [&](int start, const vector<vector<pair<int, int>>>& g) -> vector<int> {
        vector<int> dist(n, INF);
        priority_queue<pair<int, int>, vector<pair<int, int>>, greater<pair<int, int>>> pq;
        
        dist[start] = 0;
        pq.push({0, start});
        
        while (!pq.empty()) {
            auto [d, u] = pq.top();
            pq.pop();
            
            if (d > dist[u]) continue;
            
            for (auto [v, w] : g[u]) {
                if (dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    pq.push({dist[v], v});
                }
            }
        }
        
        return dist;
    };
    
    vector<int> dist_from_0 = dijkstra(0, graph);
    
    vector<int> dist_to_end = dijkstra(n - 1, reverse_graph);
    
    int answer = INT_MAX;
    
    for (int i = 0; i < n; i++) {
        int dist1 = dist_from_0[i];      
        int dist2 = dist_to_end[i];    
        
        if (dist1 < INF && dist2 < INF) {
            int max_dist = max(dist1, dist2);
            answer = min(answer, max_dist);
        }
    }
    
    cout << answer << "\n";
    
    return 0;
}

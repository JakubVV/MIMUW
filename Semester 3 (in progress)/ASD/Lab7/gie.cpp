#include <bits/stdc++.h>
using namespace std;

struct MergeSortTree {
    int n;
    vector<vector<int>> tree;

    MergeSortTree(const vector<int> &a) {
        n = a.size();
        tree.resize(4 * n);
        build(a, 1, 0, n - 1);
    }

    void build(const vector<int> &a, int node, int l, int r) {
        if (l == r) {
            tree[node] = {a[l]};
        } else {
            int mid = (l + r) / 2;
            build(a, 2 * node, l, mid);
            build(a, 2 * node + 1, mid + 1, r);
            merge(tree[2 * node].begin(), tree[2 * node].end(),
                  tree[2 * node + 1].begin(), tree[2 * node + 1].end(),
                  back_inserter(tree[node]));
        }
    }

    int count(int node, int l, int r, int ql, int qr, int lval, int rval) {
        if (r < ql || l > qr) return 0;
        if (ql <= l && r <= qr) {
            auto left = lower_bound(tree[node].begin(), tree[node].end(), lval);
            auto right = upper_bound(tree[node].begin(), tree[node].end(), rval);
            return right - left;
        }
        int mid = (l + r) / 2;
        return count(2 * node, l, mid, ql, qr, lval, rval) +
               count(2 * node + 1, mid + 1, r, ql, qr, lval, rval);
    }

    int queryMinIndex(int node, int l, int r, int ql, int qr, int lval, int rval) {
        if (r < ql || l > qr) return -1;
        if (tree[node].back() < lval || tree[node].front() > rval) return -1;
        if (l == r) return l;
        int mid = (l + r) / 2;
        int leftRes = queryMinIndex(2 * node, l, mid, ql, qr, lval, rval);
        if (leftRes != -1) return leftRes;
        return queryMinIndex(2 * node + 1, mid + 1, r, ql, qr, lval, rval);
    }
};

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int n, m;
    cin >> n >> m;
    vector<int> a(n);
    for (int i = 0; i < n; i++) cin >> a[i];

    MergeSortTree mst(a);

    while (m--) {
        int s, e, l, h;
        cin >> s >> e >> l >> h;
        s--; e--;  

        int minIndex = mst.queryMinIndex(1, 0, n - 1, s, e, l, h);
        int countVal = mst.count(1, 0, n - 1, s, e, l, h);
        if (minIndex != -1) minIndex++; 
        cout << minIndex << " " << countVal << "\n";
    }

    return 0;
}

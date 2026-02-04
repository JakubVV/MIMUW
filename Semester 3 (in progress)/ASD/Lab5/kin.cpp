#include <iostream>
#include <vector>

using namespace std;

const long long MOD = 1000000000;

struct Segment_tree {
    int n;
    vector<long long> tree;

    Segment_tree(int a) : n(a), tree(4 * n, 0) {}

    void update(int index, long long value) {
        update(index, value, 1, 1, n);
    }

    void update(int index, long long value, int cur, int l, int r) {
        if (l == r) {
            tree[cur] = (tree[cur] + value) % MOD;
            return;
        }
        int mid = (l + r) / 2;
        if (index <= mid)
            update(index, value, cur * 2, l, mid);
        else
            update(index, value, cur * 2 + 1, mid + 1, r);
        tree[cur] = (tree[cur * 2] + tree[cur * 2 + 1]) % MOD;
    }

    long long getSum(int l, int r, int cur, int leftBound, int rightBound) {
        if (l > r)
            return 0;
        if (l == leftBound && r == rightBound)
            return tree[cur];
        int mid = (leftBound + rightBound) / 2;
        return (getSum(l, min(r, mid), cur * 2, leftBound, mid) +
                getSum(max(mid + 1, l), r, cur * 2 + 1, mid + 1, rightBound)) % MOD;
    }

    long long getSum(int l, int r) {
        if (l > r)
            return 0;
        return getSum(l, r, 1, 1, n);
    }

};

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int n, k;
    cin >> n >> k;
    vector<long long> p(n);
    for (int i = 0; i < n; i++)
        cin >> p[i];

    vector<long long> dp_prev(n, 1);
    vector<long long> dp_cur(n, 0);

    for (int c = 2; c <= k; c++) {
        Segment_tree seg(n);
        for (int i = 0; i < n; i++) {
            long long sum = seg.getSum(p[i] + 1, n);
            dp_cur[i] = sum;
            seg.update(p[i], dp_prev[i]);
        }
        dp_prev = dp_cur;
        fill(dp_cur.begin(), dp_cur.end(), 0);
    }
    long long result = 0;
    
    for (auto el : dp_prev)
        result = (result + el) % MOD;

    cout << result << endl;

    return 0;
}

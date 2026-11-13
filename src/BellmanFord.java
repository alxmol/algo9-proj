import java.util.List;

/**
 * Bellman-Ford: a slow but simple shortest-path algorithm we use as the
 * "answer key" to check every Dijkstra version (Proposal section 4).
 *
 * It's O(V * E), so we only use it on small graphs (V <= 300).
 */
public class BellmanFord {

    public static Result run(Graph g, int source) {
        int vertexCount = g.vertexCount();
        Result result = Result.start(vertexCount, source);
        long[] dist = result.dist();
        int[] parent = result.parent();

        // Go over every edge up to V-1 times. Each pass can only make
        // distances shorter. If a pass changes nothing, we're done early.
        for (int pass = 0; pass < vertexCount - 1; pass++) {
            boolean changed = false;

            for (int u = 0; u < vertexCount; u++) {
                if (dist[u] == Result.INFINITY) {
                    continue;   // can't reach u yet, so its edges don't help
                }
                for (Edge e : g.edgesFrom(u)) {
                    long newDist = dist[u] + e.weight();
                    if (newDist < dist[e.to()]) {
                        dist[e.to()] = newDist;
                        parent[e.to()] = u;
                        changed = true;
                    }
                }
            }

            if (!changed) {
                break;
            }
        }
        return result;
    }

    /**
     * Checks a Dijkstra version's answer against the correct one.
     *
     * Returns null if everything matches, or a message describing the first
     * problem found. Two things are checked:
     *   1. every distance matches
     *   2. every parent makes sense: there's an edge parent[v] -> v, and
     *      dist[parent[v]] + that edge's weight == dist[v]
     *
     * We don't require the exact same parents as Bellman-Ford, because when two
     * paths tie, either one is a correct answer.
     */
    public static String compare(Graph g, int source, Result expected, Result actual) {
        long[] want = expected.dist();
        long[] got = actual.dist();
        int[] parent = actual.parent();

        if (got.length != want.length) {
            return "wrong array length: expected " + want.length + ", got " + got.length;
        }

        for (int v = 0; v < want.length; v++) {
            if (got[v] != want[v]) {
                return "vertex " + v + ": expected distance " + show(want[v]) + ", got " + show(got[v]);
            }
        }

        for (int v = 0; v < want.length; v++) {
            if (v == source || got[v] == Result.INFINITY) {
                if (parent[v] != -1) {
                    return "vertex " + v + ": parent should be -1, got " + parent[v];
                }
                continue;
            }
            int p = parent[v];
            if (p < 0 || p >= want.length || p == v) {
                return "vertex " + v + ": parent " + p + " is not a valid parent";
            }
            if (!hasEdgeWithDistance(g.edgesFrom(p), v, got[v] - got[p])) {
                return "vertex " + v + ": no edge " + p + " -> " + v + " gives distance " + got[v];
            }
        }

        return null;
    }

    /** True if one of these edges goes to "to" with exactly this weight. */
    private static boolean hasEdgeWithDistance(List<Edge> edges, int to, long weight) {
        for (Edge e : edges) {
            if (e.to() == to && e.weight() == weight) {
                return true;
            }
        }
        return false;
    }

    private static String show(long d) {
        return d == Result.INFINITY ? "INFINITY" : Long.toString(d);
    }
}

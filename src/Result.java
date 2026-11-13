import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What every shortest-path version returns.
 *
 * dist[v]   = shortest distance from the source to v (INFINITY if v can't be reached)
 * parent[v] = the vertex right before v on that shortest path (-1 for the source
 *             and for unreachable vertices)
 *
 * The parent array lets us print the actual path, not just its length.
 */
public record Result(long[] dist, int[] parent) {

    /** Distance used for vertices we can't reach. */
    public static final long INFINITY = Long.MAX_VALUE;

    /**
     * Makes a fresh Result for a graph with V vertices: every distance starts
     * at INFINITY and every parent at -1, except the source, which is 0 away.
     */
    public static Result start(int vertexCount, int source) {
        long[] dist = new long[vertexCount];
        int[] parent = new int[vertexCount];
        for (int v = 0; v < vertexCount; v++) {
            dist[v] = INFINITY;
            parent[v] = -1;
        }
        dist[source] = 0;
        return new Result(dist, parent);
    }

    /**
     * The path from the source to target, e.g. [0, 4, 2, 7].
     * Empty list if target can't be reached.
     */
    public List<Integer> pathTo(int target) {
        List<Integer> path = new ArrayList<>();
        if (dist[target] == INFINITY) {
            return path;
        }
        // Walk backwards using parent[], then flip it around.
        for (int v = target; v != -1; v = parent[v]) {
            path.add(v);
        }
        Collections.reverse(path);
        return path;
    }
}

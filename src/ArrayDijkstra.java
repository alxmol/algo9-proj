/**
 * Dijkstra with an array scan. Owner: Alsean.
 *
 * Each round, look through every unvisited vertex and pick the one with the
 * smallest distance. No queue at all. O(V^2 + E) time.
 *
 * Counters to use: countArrayScan() for each vertex looked at during the
 * scan, countRelaxation() for each edge checked.
 */
public class ArrayDijkstra implements ShortestPathAlgorithm {

    @Override
    public String name() {
        return "array";
    }

    @Override
    public Result run(Graph g, int source, Counters counters) {
        // TODO (Alsean):
        // 1. Result result = Result.start(g.vertexCount(), source);
        //    boolean[] visited = new boolean[g.vertexCount()];
        // 2. Repeat V times:
        //    a. scan all unvisited vertices for the smallest dist (countArrayScan each one)
        //    b. if the smallest is INFINITY, stop: the rest can't be reached
        //    c. mark it visited
        //    d. for each edge out of it: countRelaxation(), and if going through
        //       it is shorter, update dist[] and parent[]
        // 3. return result;
        throw new UnsupportedOperationException("not done yet");
    }
}

/**
 * Dijkstra with Java's PriorityQueue and lazy deletion. Owner: Anthony.
 *
 * PriorityQueue can't do decrease-key, so whenever a distance gets better we
 * just add another entry. When an entry comes out of the queue that's
 * outdated (the vertex was already finished), we skip it.
 * O(E log V) time, up to O(E) entries in the queue.
 *
 * Counters to use: countInsert(), countExtractMin(), countStaleSkipped(),
 * countRelaxation(), recordQueueSize().
 */
public class LazyHeapDijkstra implements ShortestPathAlgorithm {

    @Override
    public String name() {
        return "lazy-heap";
    }

    @Override
    public Result run(Graph g, int source, Counters counters) {
        // TODO (Anthony):
        // 1. Result result = Result.start(g.vertexCount(), source);
        //    boolean[] done = new boolean[g.vertexCount()];
        // 2. PriorityQueue of {distance, vertex} pairs, smallest distance first.
        //    Add {0, source}.
        // 3. While the queue isn't empty:
        //    a. poll() the smallest (countExtractMin)
        //    b. if that vertex is already done, it's outdated: countStaleSkipped(), continue
        //    c. mark it done
        //    d. for each edge out of it: countRelaxation(), and if going through
        //       it is shorter, update dist[] and parent[], then add a new entry
        //       (countInsert, recordQueueSize)
        // 4. return result;
        throw new UnsupportedOperationException("not done yet");
    }
}

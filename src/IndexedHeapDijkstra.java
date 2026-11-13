/**
 * Dijkstra with an indexed binary heap (decrease-key). Owner: Alex.
 *
 * The heap holds vertex ids ordered by distance, at most one entry per
 * vertex. A position array tells us where each vertex is in the heap, so
 * decrease-key can find it in O(1) and fix the heap in O(log V).
 * O((V + E) log V) time, O(V) queue space.
 *
 * Counters to use: countInsert(), countDecreaseKey(), countExtractMin(),
 * countRelaxation(), recordQueueSize().
 */
public class IndexedHeapDijkstra implements ShortestPathAlgorithm {

    @Override
    public String name() {
        return "indexed-heap";
    }

    @Override
    public Result run(Graph g, int source, Counters counters) {
        // TODO (Alex, Fri Nov 13)
        throw new UnsupportedOperationException("not done yet");
    }
}

/**
 * Dijkstra with an indexed binary heap (decrease-key). Owner: Alex.
 *
 * The heap holds the vertices we've found a path to but haven't finished yet,
 * ordered by distance. Each round we take out the closest one (extractMin),
 * which is now final, and try to improve its neighbors through it.
 *
 * When a neighbor's distance gets better:
 *   - if it's already in the heap, we lower its key (decreaseKey)
 *   - if it isn't, we insert it
 * So each vertex is in the heap at most once: O(V) queue space.
 *
 * Time: O((V + E) log V). Every vertex is inserted and extracted at most once
 * (V log V), and every edge can cause at most one decreaseKey (E log V).
 * See Cormen et al. [3] for the proof.
 *
 * The heap itself is in IndexedMinHeap.java.
 */
public class IndexedHeapDijkstra implements ShortestPathAlgorithm {

    @Override
    public String name() {
        return "indexed-heap";
    }

    @Override
    public Result run(Graph g, int source, Counters counters) {
        int vertexCount = g.vertexCount();
        Result result = Result.start(vertexCount, source);
        long[] dist = result.dist();
        int[] parent = result.parent();

        IndexedMinHeap heap = new IndexedMinHeap(vertexCount);
        heap.insert(source, 0);
        counters.countInsert();
        counters.recordQueueSize(heap.size());

        while (!heap.isEmpty()) {
            // The closest unfinished vertex. Its distance is now final,
            // because every other path to it would be at least this long
            // (no negative weights).
            int u = heap.extractMin();
            counters.countExtractMin();

            // Try to get to each neighbor faster by going through u.
            for (Edge e : g.edgesFrom(u)) {
                counters.countRelaxation();
                int v = e.to();
                long newDist = dist[u] + e.weight();

                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    parent[v] = u;

                    if (heap.contains(v)) {
                        heap.decreaseKey(v, newDist);
                        counters.countDecreaseKey();
                    } else {
                        // First time we've found a path to v.
                        // (A finished vertex never lands here: its distance is
                        // already the shortest, so newDist can't beat it.)
                        heap.insert(v, newDist);
                        counters.countInsert();
                        counters.recordQueueSize(heap.size());
                    }
                }
            }
        }

        return result;
    }
}

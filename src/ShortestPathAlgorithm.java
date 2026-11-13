/**
 * Every Dijkstra version implements this, so the tests, the benchmark and the
 * demo can run any of them the same way.
 */
public interface ShortestPathAlgorithm {

    /** Short name used in output and CSV files, e.g. "array". */
    String name();

    /**
     * Finds shortest paths from source to every vertex.
     *
     * @param g        the graph (weights are never negative)
     * @param source   the starting vertex
     * @param counters where to count operations; use Counters.off() for timing runs
     */
    Result run(Graph g, int source, Counters counters);
}

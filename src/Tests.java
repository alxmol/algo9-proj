import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Arrays;

/**
 * Checks that everything works. Run it after any change:
 *
 *   javac -d out src/*.java
 *   java -cp out Tests
 *
 * Plain Java, no test library, so there's nothing to install.
 * Versions that aren't written yet show up as "not done yet" instead of failing.
 */
public class Tests {

    private static int passed = 0;
    private static int failed = 0;

    /** All the Dijkstra versions. Every one gets checked against Bellman-Ford. */
    static final List<ShortestPathAlgorithm> VERSIONS = List.of(
            new ArrayDijkstra(),
            new IndexedHeapDijkstra(),
            new LazyHeapDijkstra()
    );

    public static void main(String[] args) throws Exception {
        System.out.println("== Graph ==");
        testGraphSaveAndLoad();

        System.out.println("\n== Generator ==");
        testGeneratorSameSeedSameGraph();
        testGeneratorEdgeCounts();
        testGeneratorNoSelfLoopsOrDuplicates();
        testGeneratorEverythingReachable();

        System.out.println("\n== Bellman-Ford ==");
        testBellmanFordSmallGraph();
        testCompareCatchesWrongAnswers();

        System.out.println("\n== Dijkstra versions ==");
        testVersions();

        System.out.println("\n== Large heap agreement ==");
        testHeapAgreementOnLargeGraphs();

        System.out.println("\n== Counters ==");
        testCounters();

        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------
    // Graph
    // ------------------------------------------------------------------

    static void testGraphSaveAndLoad() throws Exception {
        Graph g = smallGraph();
        File file = File.createTempFile("graph", ".txt");
        file.deleteOnExit();

        g.save(file.getPath());
        Graph loaded = Graph.load(file.getPath());

        check(sameGraph(g, loaded), "graph saved to a file and loaded back is identical");
    }

    // ------------------------------------------------------------------
    // Generator
    // ------------------------------------------------------------------

    static void testGeneratorSameSeedSameGraph() {
        Graph a = GraphGenerator.generate(300, GraphGenerator.Density.MEDIUM, 42);
        Graph b = GraphGenerator.generate(300, GraphGenerator.Density.MEDIUM, 42);
        Graph c = GraphGenerator.generate(300, GraphGenerator.Density.MEDIUM, 43);
        check(sameGraph(a, b), "same seed gives the same graph");
        check(!sameGraph(a, c), "different seed gives a different graph");
    }

    static void testGeneratorEdgeCounts() {
        for (int v : new int[] {1, 2, 5, 100, 1000}) {
            for (GraphGenerator.Density d : GraphGenerator.Density.values()) {
                Graph g = GraphGenerator.generate(v, d, 1);
                long want = d.targetEdges(v);
                check(g.edgeCount() == want,
                        "V=" + v + " " + d + " has " + want + " edges (got " + g.edgeCount() + ")");
            }
        }
    }

    static void testGeneratorNoSelfLoopsOrDuplicates() {
        Graph g = GraphGenerator.generate(300, GraphGenerator.Density.DENSE, 7);
        boolean ok = true;
        for (int from = 0; from < g.vertexCount(); from++) {
            boolean[] seen = new boolean[g.vertexCount()];
            for (Edge e : g.edgesFrom(from)) {
                if (e.to() == from || seen[e.to()]) {
                    ok = false;
                }
                seen[e.to()] = true;
            }
        }
        check(ok, "no self-loops or duplicate edges");
    }

    static void testGeneratorEverythingReachable() {
        for (GraphGenerator.Density d : GraphGenerator.Density.values()) {
            Graph g = GraphGenerator.generate(1000, d, 3);
            check(reachableCount(g, GraphGenerator.SOURCE) == g.vertexCount(),
                    d + ": every vertex reachable from the source");
        }
    }

    // ------------------------------------------------------------------
    // Bellman-Ford
    // ------------------------------------------------------------------

    static void testBellmanFordSmallGraph() {
        Result r = BellmanFord.run(smallGraph(), 0);
        long inf = Result.INFINITY;
        long[] want = {0, 3, 1, 4, 7, inf};   // worked out by hand, see smallGraph()

        boolean ok = true;
        for (int v = 0; v < want.length; v++) {
            if (r.dist()[v] != want[v]) ok = false;
        }
        check(ok, "distances match the ones worked out by hand");
        check(r.pathTo(4).equals(List.of(0, 2, 1, 3, 4)), "path to 4 is 0 -> 2 -> 1 -> 3 -> 4");
        check(r.pathTo(5).isEmpty(), "no path to unreachable vertex 5");
    }

    static void testCompareCatchesWrongAnswers() {
        Graph g = smallGraph();
        Result right = BellmanFord.run(g, 0);
        check(BellmanFord.compare(g, 0, right, right) == null, "a correct answer passes");

        Result wrongDist = copy(right);
        wrongDist.dist()[4] = 8;
        check(BellmanFord.compare(g, 0, right, wrongDist) != null, "a wrong distance gets caught");

        Result wrongParent = copy(right);
        wrongParent.parent()[3] = 2;   // 2 -> 3 costs 5, so dist[3] would be 6, not 4
        check(BellmanFord.compare(g, 0, right, wrongParent) != null, "a wrong parent gets caught");
    }

    // ------------------------------------------------------------------
    // Dijkstra versions
    // ------------------------------------------------------------------

    /** The graphs every version is checked on. */
    static List<Graph> testGraphs() {
        List<Graph> graphs = new ArrayList<>();
        graphs.add(smallGraph());

        // Edge cases from the proposal (section 4)
        graphs.addAll(edgeCaseGraphs());

        // add random graphs from GraphGenerator,
        // V = 100 and 300, all three densities, a few seeds each.

        int[] randomGraphVertexCount = {100, 300};
        for (int v : randomGraphVertexCount) {
            for (GraphGenerator.Density density : GraphGenerator.Density.values()) {
                for (long seed = 1; seed <= 3; seed++) {
                    
                    graphs.add(GraphGenerator.generate(v, density, seed));

                }
            }
        }

        return graphs;
    }

    /**
     * One small graph per edge case. Every version runs from vertex 0 and has
     * to match Bellman-Ford on each of them.
     */
    static List<Graph> edgeCaseGraphs() {
        List<Graph> graphs = new ArrayList<>();

        // One vertex, no edges: the source is the only vertex
        graphs.add(new Graph(1));

        // Unreachable vertices: 2 and 3 have no way in from 0
        Graph unreachable = new Graph(4);
        unreachable.addEdge(0, 1, 5);
        unreachable.addEdge(2, 3, 1);
        unreachable.addEdge(3, 0, 1);   // edges out of unreachable vertices don't matter
        graphs.add(unreachable);

        // Source with no edges out: everything else is unreachable
        Graph stuck = new Graph(3);
        stuck.addEdge(1, 2, 4);
        graphs.add(stuck);

        // Ties: 0 -> 1 -> 3 and 0 -> 2 -> 3 both cost 4, either parent is fine
        Graph ties = new Graph(4);
        ties.addEdge(0, 1, 2);
        ties.addEdge(0, 2, 3);
        ties.addEdge(1, 3, 2);
        ties.addEdge(2, 3, 1);
        graphs.add(ties);

        // Zero-weight edges, including a zero-weight chain
        Graph zeros = new Graph(4);
        zeros.addEdge(0, 1, 0);
        zeros.addEdge(1, 2, 0);
        zeros.addEdge(0, 2, 5);
        zeros.addEdge(2, 3, 0);
        graphs.add(zeros);

        // Parallel edges: the cheaper 0 -> 1 edge (2) should win over the others
        Graph parallel = new Graph(3);
        parallel.addEdge(0, 1, 9);
        parallel.addEdge(0, 1, 2);
        parallel.addEdge(0, 1, 5);
        parallel.addEdge(1, 2, 1);
        graphs.add(parallel);

        // Self-loops, including a zero-weight one on the source
        Graph loops = new Graph(3);
        loops.addEdge(0, 0, 0);
        loops.addEdge(0, 1, 3);
        loops.addEdge(1, 1, 2);
        loops.addEdge(1, 2, 4);
        graphs.add(loops);

        // All of them at once (same graph the demo uses, graphs/edge-cases.txt)
        Graph mixed = new Graph(6);
        mixed.addEdge(0, 1, 0);
        mixed.addEdge(0, 2, 5);
        mixed.addEdge(0, 2, 3);
        mixed.addEdge(1, 3, 4);
        mixed.addEdge(2, 3, 1);
        mixed.addEdge(3, 3, 7);
        mixed.addEdge(3, 4, 2);
        mixed.addEdge(5, 4, 1);
        graphs.add(mixed);

        return graphs;
    }

    /**
     * Checks every version counts the right things (proposal section 4).
     *
     * Every version relaxes each edge out of a reachable vertex exactly once,
     * so all three should report the same number of relaxations. The rest
     * depends on the version:
     *   array        only array scans, no queue at all
     *   indexed-heap one insert and one extract per reachable vertex, queue never above V
     *   lazy-heap    every insert comes out again; the extra ones are the stale skips
     */
    static void testCounters() {
        Graph g = GraphGenerator.generate(300, GraphGenerator.Density.MEDIUM, 5);
        int reachable = reachableCount(g, 0);

        Counters array = Counters.on();
        Counters indexed = Counters.on();
        Counters lazy = Counters.on();
        try {
            new ArrayDijkstra().run(g, 0, array);
            new IndexedHeapDijkstra().run(g, 0, indexed);
            new LazyHeapDijkstra().run(g, 0, lazy);
        } catch (UnsupportedOperationException e) {
            System.out.println("  --    skipped: not every version is done yet");
            return;
        }

        check(array.relaxations == g.edgeCount() && indexed.relaxations == g.edgeCount()
                && lazy.relaxations == g.edgeCount(),
                "all versions relax every edge once (" + g.edgeCount() + ")");

        check(array.arrayScans > 0 && array.inserts == 0 && array.maxQueueSize == 0,
                "array: only array scans, no queue");

        check(indexed.inserts == reachable && indexed.extractMins == reachable
                && indexed.staleSkipped == 0 && indexed.maxQueueSize <= g.vertexCount(),
                "indexed-heap: one insert + extract per vertex, queue <= V");

        check(lazy.extractMins == lazy.inserts && lazy.staleSkipped == lazy.inserts - reachable
                && lazy.decreaseKeys == 0 && lazy.arrayScans == 0,
                "lazy-heap: every insert comes out, extras are stale skips");
    }

    static void testVersions() {
        List<Graph> graphs = testGraphs();

        for (ShortestPathAlgorithm version : VERSIONS) {
            try {
                boolean allMatch = true;
                for (int i = 0; i < graphs.size(); i++) {
                    Graph g = graphs.get(i);
                    int source = 0;
                    Result expected = BellmanFord.run(g, source);
                    Result actual = version.run(g, source, Counters.on());
                    String problem = BellmanFord.compare(g, source, expected, actual);
                    if (problem != null) {
                        allMatch = false;
                        System.out.println("  " + version.name() + ", test graph " + i + ": " + problem);
                    }
                }
                check(allMatch, version.name() + " matches Bellman-Ford on all " + graphs.size() + " test graphs");
            } catch (UnsupportedOperationException e) {
                System.out.println("  --    " + version.name() + ": not done yet");
            }
        }
    }

    static void testHeapAgreementOnLargeGraphs() {
        int[] sizes = {10_000, 100_000};

        for (int size : sizes) {
            Graph g = GraphGenerator.generate(
                    size, GraphGenerator.Density.SPARSE, 1);

            Result indexed = new IndexedHeapDijkstra().run(
                    g, GraphGenerator.SOURCE, Counters.off());

            Result lazy = new LazyHeapDijkstra().run(
                    g, GraphGenerator.SOURCE, Counters.off());

            check(Arrays.equals(indexed.dist(), lazy.dist()),
                    "indexed and lazy heaps agree on V=" + size);
    }
}

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * A small graph we can check by hand:
     *
     *   0 -> 1 (4)    0 -> 2 (1)    2 -> 1 (2)
     *   1 -> 3 (1)    2 -> 3 (5)    3 -> 4 (3)
     *   vertex 5 has no edges into it
     *
     * Shortest distances from 0: 0, 3, 1, 4, 7, unreachable
     */
    static Graph smallGraph() {
        Graph g = new Graph(6);
        g.addEdge(0, 1, 4);
        g.addEdge(0, 2, 1);
        g.addEdge(2, 1, 2);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 5);
        g.addEdge(3, 4, 3);
        return g;
    }

    static void check(boolean ok, String what) {
        if (ok) {
            passed++;
            System.out.println("  ok    " + what);
        } else {
            failed++;
            System.out.println("  FAIL  " + what);
        }
    }

    /** True if both graphs have the same vertices and the same edges in the same order. */
    static boolean sameGraph(Graph a, Graph b) {
        if (a.vertexCount() != b.vertexCount() || a.edgeCount() != b.edgeCount()) {
            return false;
        }
        for (int v = 0; v < a.vertexCount(); v++) {
            if (!a.edgesFrom(v).equals(b.edgesFrom(v))) {
                return false;
            }
        }
        return true;
    }

    /** Counts how many vertices a breadth-first search from source can reach. */
    static int reachableCount(Graph g, int source) {
        boolean[] seen = new boolean[g.vertexCount()];
        Deque<Integer> queue = new ArrayDeque<>();
        seen[source] = true;
        queue.add(source);
        int count = 1;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Edge e : g.edgesFrom(u)) {
                if (!seen[e.to()]) {
                    seen[e.to()] = true;
                    queue.add(e.to());
                    count++;
                }
            }
        }
        return count;
    }

    static Result copy(Result r) {
        return new Result(r.dist().clone(), r.parent().clone());
    }
}

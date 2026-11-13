import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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

        // TODO (Alsean): add the edge-case graphs from the proposal:
        //   unreachable vertices, ties, zero-weight edges, parallel edges,
        //   self-loops, and a graph with one vertex.

        // TODO (Luke): add random graphs from GraphGenerator,
        //   V = 100 and 300, all three densities, a few seeds each.

        return graphs;
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

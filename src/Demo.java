import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The live demo for the presentation (3 minutes max). Owner: Alex.
 *
 * Runs every finished version on a graph, prints the shortest distance and
 * path to a target, checks all versions agree, and shows how long each took
 * and the biggest the queue got (our memory measure).
 *
 * Ways to run it (from the repo folder, after javac -d out src/*.java):
 *
 *   java -Xmx2g -cp out Demo
 *       The full demo: the small graph in graphs/demo.txt, the edge cases in
 *       graphs/edge-cases.txt, then a big dense graph and a big sparse graph.
 *
 *   java -Xmx2g -cp out Demo step
 *       Same thing, but waits for Enter between parts (for presenting).
 *
 *   java -cp out Demo file graphs/demo.txt 0 6
 *       Any graph file, from source 0 to target 6.
 *
 *   java -Xmx2g -cp out Demo generate 3000 dense 1
 *       A random graph: V = 3000, density sparse/medium/dense, seed 1.
 *       Target is the last vertex unless you add one more number.
 */
public class Demo {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            fullDemo(false);
        } else if (args[0].equals("step") && args.length == 1) {
            fullDemo(true);
        } else if (args[0].equals("file") && args.length == 4) {
            Graph g = Graph.load(args[1]);
            runAll("Graph from " + args[1], g, Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        } else if (args[0].equals("generate") && (args.length == 4 || args.length == 5)) {
            int vertexCount = Integer.parseInt(args[1]);
            GraphGenerator.Density density = GraphGenerator.Density.valueOf(args[2].toUpperCase());
            long seed = Long.parseLong(args[3]);
            int target = (args.length == 5) ? Integer.parseInt(args[4]) : vertexCount - 1;
            Graph g = GraphGenerator.generate(vertexCount, density, seed);
            runAll("Random graph, V = " + vertexCount + ", " + args[2] + ", seed " + seed,
                    g, GraphGenerator.SOURCE, target);
        } else {
            System.out.println("Usage:");
            System.out.println("  java -cp out Demo");
            System.out.println("  java -cp out Demo step");
            System.out.println("  java -cp out Demo file <graph file> <source> <target>");
            System.out.println("  java -cp out Demo generate <V> <sparse|medium|dense> <seed> [target]");
        }
    }

    /**
     * The demo we show in the presentation:
     * 1. A tiny graph everyone can follow on a slide.
     * 2. Edge cases: unreachable vertex, zero weight, tie, parallel edge, self-loop.
     * 3. A big dense graph, where the array scan should hold up well.
     * 4. A big sparse graph, where the heaps should pull ahead.
     */
    static void fullDemo(boolean step) throws Exception {
        runAll("1) Small graph (graphs/demo.txt)", Graph.load("graphs/demo.txt"), 0, 6);
        waitForEnter(step);

        edgeCaseDemo();
        waitForEnter(step);

        Graph dense = GraphGenerator.generate(3000, GraphGenerator.Density.DENSE, 1);
        runAll("3) Dense graph, V = 3,000", dense, GraphGenerator.SOURCE, 2999);
        waitForEnter(step);

        Graph sparse = GraphGenerator.generate(20_000, GraphGenerator.Density.SPARSE, 1);
        runAll("4) Sparse graph, V = 20,000", sparse, GraphGenerator.SOURCE, 19_999);
    }

    private static final java.util.Scanner keyboard = new java.util.Scanner(System.in);

    /** In step mode, pause so the presenter can talk through each part. */
    static void waitForEnter(boolean step) {
        if (!step) {
            return;
        }
        System.out.print("\n(press Enter for the next part) ");
        keyboard.nextLine();
    }

    /**
     * graphs/edge-cases.txt has one of each tricky input:
     *
     *   0 -> 1 (0)            zero-weight edge
     *   0 -> 2 (5), 0 -> 2 (3) parallel edges: the cheaper one (3) should win
     *   0 -> 1 -> 3 = 4 and 0 -> 2 -> 3 = 4    a tie: either path is right
     *   3 -> 3 (7)            self-loop: should be ignored
     *   5 -> 4 (1)            vertex 5 has no way in, so it's unreachable
     *
     * Expected distances from 0: 0, 0, 3, 4, 6, unreachable
     */
    static void edgeCaseDemo() throws Exception {
        Graph g = Graph.load("graphs/edge-cases.txt");
        Map<String, Result> results = runAll("2) Edge cases (graphs/edge-cases.txt)", g, 0, 4);

        // Show every vertex, not just one target, so each edge case is visible.
        long[] expected = {0, 0, 3, 4, 6, Result.INFINITY};
        System.out.println();
        System.out.printf("  Every vertex:  %-13s %s%n", "expected", showAll(expected));
        for (Map.Entry<String, Result> e : results.entrySet()) {
            String verdict = Arrays.equals(expected, e.getValue().dist()) ? "ok" : "WRONG";
            System.out.printf("                 %-13s %s   %s%n", e.getKey(), showAll(e.getValue().dist()), verdict);
        }
    }

    /** All distances on one line, e.g. "0 0 3 4 6 -". A dash means unreachable. */
    static String showAll(long[] dist) {
        StringBuilder sb = new StringBuilder();
        for (long d : dist) {
            sb.append(String.format("%-3s", d == Result.INFINITY ? "-" : Long.toString(d)));
        }
        return sb.toString();
    }

    /**
     * Runs every finished version on g and prints the results side by side.
     * Returns each finished version's result, by name.
     */
    static Map<String, Result> runAll(String title, Graph g, int source, int target) {
        System.out.println();
        System.out.println("=== " + title + " ===");
        System.out.println("V = " + g.vertexCount() + ", E = " + g.edgeCount()
                + ", from " + source + " to " + target);
        System.out.println();

        Map<String, Result> results = new LinkedHashMap<>();   // keeps the versions in order

        for (ShortestPathAlgorithm version : Tests.VERSIONS) {
            try {
                // Run once without timing first. Java speeds code up after it
                // has run a bit (JIT warm-up), so the first run is always slow.
                version.run(g, source, Counters.off());

                long start = System.nanoTime();
                Result r = version.run(g, source, Counters.off());
                double ms = (System.nanoTime() - start) / 1_000_000.0;

                // One more run with counting on, to get the biggest queue size.
                // It's separate so counting doesn't slow down the timed run.
                Counters counts = Counters.on();
                version.run(g, source, counts);
                String queue = counts.maxQueueSize == 0 ? "no queue" : "queue " + counts.maxQueueSize;

                results.put(version.name(), r);
                System.out.printf("  %-13s %8.2f ms   %-14s distance %s   path %s%n",
                        version.name(), ms, queue, showDistance(r.dist()[target]), showPath(r.pathTo(target)));
            } catch (UnsupportedOperationException e) {
                System.out.printf("  %-13s not done yet%n", version.name());
            }
        }

        // Make sure every version found the same distances to every vertex.
        System.out.println();
        if (results.size() < 2) {
            System.out.println("  (need at least 2 finished versions to compare)");
        } else if (allSameDistances(results.values())) {
            System.out.println("  All versions agree on every distance.");
        } else {
            System.out.println("  WARNING: versions disagree! Run Tests to find out which one is wrong.");
        }
        return results;
    }

    static boolean allSameDistances(Collection<Result> results) {
        long[] first = results.iterator().next().dist();
        for (Result r : results) {
            if (!Arrays.equals(first, r.dist())) {
                return false;
            }
        }
        return true;
    }

    static String showDistance(long d) {
        return d == Result.INFINITY ? "unreachable" : Long.toString(d);
    }

    /** Prints short paths in full, long ones as "first few ... last few". */
    static String showPath(List<Integer> path) {
        if (path.isEmpty()) {
            return "none";
        }
        if (path.size() <= 10) {
            return joinWithArrows(path);
        }
        return joinWithArrows(path.subList(0, 4)) + " -> ... -> "
                + joinWithArrows(path.subList(path.size() - 3, path.size()))
                + "  (" + path.size() + " vertices)";
    }

    static String joinWithArrows(List<Integer> vertices) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vertices.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(vertices.get(i));
        }
        return sb.toString();
    }
}

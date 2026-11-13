import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The live demo for the presentation (3 minutes max). Owner: Alex.
 *
 * Runs every finished version on a graph, prints the shortest distance and
 * path to a target, checks all versions agree, and shows how long each took.
 *
 * Ways to run it (from the repo folder, after javac -d out src/*.java):
 *
 *   java -Xmx2g -cp out Demo
 *       The full demo: the small graph in graphs/demo.txt, then a big dense
 *       graph, then a big sparse graph.
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
            fullDemo();
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
            System.out.println("  java -cp out Demo file <graph file> <source> <target>");
            System.out.println("  java -cp out Demo generate <V> <sparse|medium|dense> <seed> [target]");
        }
    }

    /**
     * The demo we show in the presentation:
     * 1. A tiny graph everyone can follow on a slide.
     * 2. A big dense graph, where the array scan should hold up well.
     * 3. A big sparse graph, where the heaps should pull ahead.
     */
    static void fullDemo() throws Exception {
        runAll("1) Small graph (graphs/demo.txt)", Graph.load("graphs/demo.txt"), 0, 6);

        Graph dense = GraphGenerator.generate(3000, GraphGenerator.Density.DENSE, 1);
        runAll("2) Dense graph, V = 3,000", dense, GraphGenerator.SOURCE, 2999);

        Graph sparse = GraphGenerator.generate(20_000, GraphGenerator.Density.SPARSE, 1);
        runAll("3) Sparse graph, V = 20,000", sparse, GraphGenerator.SOURCE, 19_999);
    }

    /** Runs every finished version on g and prints the results side by side. */
    static void runAll(String title, Graph g, int source, int target) {
        System.out.println();
        System.out.println("=== " + title + " ===");
        System.out.println("V = " + g.vertexCount() + ", E = " + g.edgeCount()
                + ", from " + source + " to " + target);
        System.out.println();

        List<Result> results = new ArrayList<>();

        for (ShortestPathAlgorithm version : Tests.VERSIONS) {
            try {
                // Run once without timing first. Java speeds code up after it
                // has run a bit (JIT warm-up), so the first run is always slow.
                version.run(g, source, Counters.off());

                long start = System.nanoTime();
                Result r = version.run(g, source, Counters.off());
                double ms = (System.nanoTime() - start) / 1_000_000.0;

                results.add(r);
                System.out.printf("  %-13s %8.2f ms   distance %s   path %s%n",
                        version.name(), ms, showDistance(r.dist()[target]), showPath(r.pathTo(target)));
            } catch (UnsupportedOperationException e) {
                System.out.printf("  %-13s not done yet%n", version.name());
            }
        }

        // Make sure every version found the same distances to every vertex.
        System.out.println();
        if (results.size() < 2) {
            System.out.println("  (need at least 2 finished versions to compare)");
        } else if (allSameDistances(results)) {
            System.out.println("  All versions agree on every distance.");
        } else {
            System.out.println("  WARNING: versions disagree! Run Tests to find out which one is wrong.");
        }
    }

    static boolean allSameDistances(List<Result> results) {
        long[] first = results.get(0).dist();
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

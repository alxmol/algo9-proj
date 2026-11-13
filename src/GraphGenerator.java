import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Makes the random benchmark graphs (Proposal section 3).
 *
 * - Simple directed graphs: no self-loops, no parallel edges.
 * - Weights are random integers from 1 to 100.
 * - Same V, density and seed always gives the exact same graph.
 * - A "backbone" path makes sure every vertex can be reached from the source.
 */
public class GraphGenerator {

    /** The three densities from the proposal. */
    public enum Density {
        SPARSE,   // E is about 4V
        MEDIUM,   // E is about 0.1 * V(V-1)
        DENSE;    // E is about 0.5 * V(V-1)

        /** How many edges a graph with V vertices should get at this density. */
        public long targetEdges(int vertexCount) {
            long v = vertexCount;
            long maxPossible = v * (v - 1);   // every ordered pair, no self-loops
            long target = switch (this) {
                case SPARSE -> 4 * v;
                case MEDIUM -> Math.round(0.1 * maxPossible);
                case DENSE -> Math.round(0.5 * maxPossible);
            };
            // Tiny graphs: can't have more edges than maxPossible,
            // and need at least V-1 edges for the backbone.
            target = Math.min(target, maxPossible);
            target = Math.max(target, v - 1);
            return target;
        }
    }

    public static final int MIN_WEIGHT = 1;
    public static final int MAX_WEIGHT = 100;

    /** The source vertex we always run from. */
    public static final int SOURCE = 0;

    /**
     * Builds a random graph.
     *
     * @param vertexCount V
     * @param density     SPARSE, MEDIUM or DENSE
     * @param seed        same seed = same graph
     */
    public static Graph generate(int vertexCount, Density density, long seed) {
        Random random = new Random(seed);
        Graph g = new Graph(vertexCount);
        EdgeTracker used = new EdgeTracker(vertexCount);
        long target = density.targetEdges(vertexCount);

        // Step 1: the backbone.
        // Put the other vertices in a random order and chain them after the
        // source: SOURCE -> a -> b -> c ... so every vertex is reachable.
        int[] order = randomOrderWithSourceFirst(vertexCount, random);
        for (int i = 0; i + 1 < vertexCount; i++) {
            int from = order[i];
            int to = order[i + 1];
            used.add(from, to);
            g.addEdge(from, to, randomWeight(random));
        }

        // Step 2: keep adding random edges until we hit the target.
        // Skip self-loops and any edge we already have.
        while (g.edgeCount() < target) {
            int from = random.nextInt(vertexCount);
            int to = random.nextInt(vertexCount);
            if (from == to || used.contains(from, to)) {
                continue;
            }
            used.add(from, to);
            g.addEdge(from, to, randomWeight(random));
        }

        return g;
    }

    private static int randomWeight(Random random) {
        return MIN_WEIGHT + random.nextInt(MAX_WEIGHT - MIN_WEIGHT + 1);
    }

    /** Returns 0..V-1 shuffled, but with SOURCE moved to the front. */
    private static int[] randomOrderWithSourceFirst(int vertexCount, Random random) {
        int[] order = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            order[i] = i;
        }
        // Shuffle everything after position 0 (Fisher-Yates shuffle).
        // SOURCE is 0, so it stays at the front.
        for (int i = vertexCount - 1; i > 1; i--) {
            int j = 1 + random.nextInt(i);   // pick from positions 1..i
            int tmp = order[i];
            order[i] = order[j];
            order[j] = tmp;
        }
        return order;
    }

    /**
     * Remembers which edges we already added, so we never add the same one twice.
     *
     * Small graphs (up to V = 46,340) use a BitSet with one bit per possible
     * edge, which is fast and uses little memory even for the dense V = 3,000
     * graph (4.5 million edges). Bigger graphs (V = 10,000+ only run sparse)
     * would need too many bits, so they use a HashSet instead.
     */
    private static class EdgeTracker {
        private final long vertexCount;
        private final BitSet bits;        // used when the graph is small enough
        private final Set<Long> set;      // used otherwise

        EdgeTracker(int vertexCount) {
            this.vertexCount = vertexCount;
            long possibleEdges = (long) vertexCount * vertexCount;
            if (possibleEdges <= Integer.MAX_VALUE) {
                bits = new BitSet((int) possibleEdges);
                set = null;
            } else {
                bits = null;
                set = new HashSet<>();
            }
        }

        /** Turns the edge (from, to) into one number: from * V + to. */
        private long key(int from, int to) {
            return from * vertexCount + to;
        }

        void add(int from, int to) {
            if (bits != null) bits.set((int) key(from, to));
            else set.add(key(from, to));
        }

        boolean contains(int from, int to) {
            if (bits != null) return bits.get((int) key(from, to));
            else return set.contains(key(from, to));
        }
    }
}

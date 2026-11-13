import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A directed, weighted graph stored as an adjacency list.
 *
 * Vertices are numbered 0 to V-1. For each vertex we keep a list of the
 * edges leaving it. Every version of Dijkstra uses this same class, so the
 * only thing that changes between versions is how the next vertex is picked.
 *
 * Self-loops, parallel edges and zero-weight edges are all allowed here,
 * because the edge-case tests need them. The random generator just never
 * makes them for the benchmark graphs.
 */
public class Graph {

    private final List<List<Edge>> adjacency;
    private int edgeCount = 0;

    /** Makes a graph with V vertices and no edges yet. */
    public Graph(int vertexCount) {
        adjacency = new ArrayList<>(vertexCount);
        for (int v = 0; v < vertexCount; v++) {
            adjacency.add(new ArrayList<>());
        }
    }

    /** Adds a directed edge from -> to with the given weight. */
    public void addEdge(int from, int to, int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Dijkstra needs nonnegative weights, got " + weight);
        }
        adjacency.get(from).add(new Edge(to, weight));
        edgeCount++;
    }

    /** V: how many vertices. */
    public int vertexCount() {
        return adjacency.size();
    }

    /** E: how many edges. */
    public int edgeCount() {
        return edgeCount;
    }

    /** The edges leaving vertex v. */
    public List<Edge> edgesFrom(int v) {
        return adjacency.get(v);
    }

    /**
     * Saves the graph to a text file.
     * Format: first line is "V E", then one line per edge: "from to weight".
     */
    public void save(String path) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.println(vertexCount() + " " + edgeCount());
            for (int from = 0; from < vertexCount(); from++) {
                for (Edge e : edgesFrom(from)) {
                    out.println(from + " " + e.to() + " " + e.weight());
                }
            }
        }
    }

    /** Loads a graph from a file written by save(). */
    public static Graph load(String path) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(path))) {
            String[] header = in.readLine().trim().split("\\s+");
            int vertexCount = Integer.parseInt(header[0]);
            int expectedEdges = Integer.parseInt(header[1]);

            Graph g = new Graph(vertexCount);
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                g.addEdge(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }

            if (g.edgeCount() != expectedEdges) {
                throw new IOException("File says " + expectedEdges + " edges but has " + g.edgeCount());
            }
            return g;
        }
    }
}

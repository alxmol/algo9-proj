# Dijkstra: Array Scan vs. Binary Heaps

CS 4306 Algorithm Analysis, team Algo 9: Luke Fritts, Alsean Griggs, Anthony Langston, Alex Molina.

## What this is

Dijkstra's algorithm finds the shortest paths in a graph (as long as no edge weight is negative). The slow part is picking the next closest vertex each round, and how you do that changes the runtime a lot.

We're building three versions that only differ in that one step, running them on graphs that get bigger and denser, and comparing what we measure to what the Big-O says should happen.

| Version | How it picks the next vertex | Time |
| --- | --- | --- |
| Array scan | Look at every unvisited vertex, take the smallest | O(V² + E) |
| Indexed binary heap | Heap with decrease-key, one entry per vertex | O((V + E) log V) |
| Lazy PriorityQueue | Java's PriorityQueue, add a new entry when a distance improves, skip old ones | O(E log V) |

What we expect: the heaps win on sparse graphs, the array keeps up or wins on dense ones. We'll see if that holds.

## Benchmarks

- Random directed graphs, weights 1 to 100, fixed seeds so we can make the same graph again
- V = 100, 300, 1,000, 3,000 at three densities: E ≈ 4V, 0.1·V(V−1), 0.5·V(V−1)
- Heap versions also on sparse V = 10,000 and 100,000
- Every version checked against Bellman-Ford before we time anything
- 10 graphs per setup, JVM warmed up, median + IQR, results saved as CSV

## Build and run

Needs Java 17 or newer. No build tool.

```bash
javac -d out src/*.java
java -cp out Tests
```

Demo (what we run in the presentation):

```bash
java -Xmx2g -cp out Demo
```

## Files

| File | What it does |
| --- | --- |
| `Graph.java`, `Edge.java` | The graph (adjacency list), save/load to a text file |
| `GraphGenerator.java` | Makes the random benchmark graphs |
| `ShortestPathAlgorithm.java` | The interface every version implements |
| `Result.java` | What a version returns: distances + parents (for printing paths) |
| `Counters.java` | Counts heap ops, scans, relaxations, queue size |
| `BellmanFord.java` | The answer key, plus `compare()` to check a version's answer |
| `ArrayDijkstra.java` | Array scan version |
| `IndexedHeapDijkstra.java` | Indexed heap version |
| `IndexedMinHeap.java` | The indexed heap it uses (with decrease-key) |
| `LazyHeapDijkstra.java` | Lazy PriorityQueue version |
| `Tests.java` | Runs all the checks |
| `Benchmark.java` | Times every version, saves results as CSV |
| `Demo.java` | The live demo: runs every version, prints distance, path and time |
| `graphs/demo.txt` | Small graph for the demo |

## Who's doing what

See [WALKTHROUGH.md](WALKTHROUGH.md). Presentation is Fri Nov 20, report is due Mon Nov 30.

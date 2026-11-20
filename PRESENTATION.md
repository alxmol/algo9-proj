# Presentation

**Fri Nov 20, 4:30.** 12 minutes including the demo, then 3 minutes of Q&A. Deck is 21 slides.

## The project in 30 seconds

Read our proposal first (`Algo9_PP.pdf`, it's in the GroupMe). We picked Option 1: speeding up Dijkstra's algorithm.

Dijkstra finds shortest paths. The slow part is picking the closest unfinished vertex each round. We built three versions that only differ in that one step:

- **Array scan:** look at every vertex each round. O(V² + E)
- **Indexed heap:** binary heap with decrease-key. O((V + E) log V)
- **Lazy heap:** Java's PriorityQueue, add a new entry when a distance improves, skip old ones. O(E log V)

We checked every version against Bellman-Ford (the answer key), ran them on random graphs from 100 to 100,000 vertices at three densities, and compared the times to the Big-O.

What we found:

- **Sparse graphs:** the heaps win big, 23× faster than the array at V = 3,000
- **Dense graphs:** the array catches up and wins at V = 3,000 (28 ms vs 33 ms)
- **Indexed vs lazy:** the indexed heap won from V = 1,000 up. The lazy heap's queue got up to 4.6× bigger

## Who presents what

| Slides | Who | Time |
| --- | --- | --- |
| 1. Title | Alex | 0:15 |
| 2–5. Team, problem, goals, requirements | Alsean | 1:45 |
| 6–11. Design, data structures, process, GitHub | Alex | 2:35 |
| 12–14. Testing, benchmark method, optimizations | Luke | 1:25 |
| 15–18. Big-O, runtime plots, crossover, lazy vs indexed | Anthony | 2:15 |
| 19. Live demo | Alex | 2:15 |
| 20–21. Conclusion, references | Alsean | 0:45 |
| | **Total** | **11:15** |

That leaves about 45 seconds for handoffs. Keep each slide short: say the title's takeaway, point at the picture, move on.

## What to say, slide by slide

**1. Title (Alex).** "We're Algo 9. Our project compares three ways to run Dijkstra's algorithm and when each one wins." Hand off to Alsean.

**2. Team and roles (Alsean).** One line per person, read off the cards.

**3. The problem (Alsean).** Dijkstra finds shortest paths with no negative weights. Each round it picks the closest unfinished vertex, V times total, so how you do that pick decides the speed. Used in GPS, network routing (OSPF), game and robot path planning.

**4. Our goal (Alsean).** Build three versions that all give correct answers, measure them as graphs get bigger and denser, and compare that to the Big-O.

**5. Requirements (Alsean).** Users: students comparing priority queues, run on a laptop. Functional: graph in, distance and path out, three versions behind one interface. Non-functional: must match Bellman-Ford, the complexity targets, scales to 100,000 vertices. Hand off to Alex.

**6. One interface (Alex).** Every version plugs into the same interface, so the tests, benchmark and demo run any of them the same way. Shared pieces on the right.

**7. The flowchart (Alex).** Walk the loop: start at 0, pick the closest, mark it done, relax its edges, repeat. The highlighted box is the only thing that changes between the three versions.

**8. Data structures (Alex).** Adjacency list for the graph, two arrays for distance and parent, then the two kinds of heap. Plain Java, no libraries.

**9. Process (Alex).** Scope fixed in the proposal (Waterfall-style), then short daily loops to build and test (Agile-style).

**10. Task split (Alex).** Shared setup came first so everyone could build their part at the same time.

**11. GitHub (Alex).** Branch, pull request, review, merge. Point at the screenshot of Luke's PR. Hand off to Luke.

**12. Testing (Luke).** Bellman-Ford is the answer key. Every version has to match it on 27 graphs, including edge cases. The big graphs are too slow for Bellman-Ford, so the two heaps check each other there.

**13. Benchmark method (Luke).** Fixed seeds so anyone can rerun it. 10 graphs per setup, 5 timed runs each, warm-up first, medians. We ran the whole thing twice.

**14. Optimizations (Luke).** Decrease-key keeps the indexed heap at V entries. Lazy deletion is simpler but the queue grows. Counters are off during timing. Hand off to Anthony.

**15. Big-O table (Anthony).** On paper: heaps win on sparse graphs, the array wins on dense ones. A Fibonacci heap is better on paper but usually slower in practice, so we left it for future work.

**16. Runtime plot (Anthony).** Solid lines are our times, dashed lines are the Big-O. They follow each other. On sparse graphs (left) the array shoots up and the heaps pull away. On dense graphs (right) all three end up close.

**17. Crossover (Anthony).** Above the dotted line the heap is faster, below it the array is. Sparse keeps climbing to 23×. Dense drops below 1 at V = 3,000, which is where the array wins.

**18. Lazy vs indexed (Anthony).** The proposal guessed the lazy heap might be faster. It wasn't: from V = 1,000 up the indexed heap won, and the lazy queue got up to 4.6× bigger on dense graphs. Hand off to Alex for the demo.

**19. Live demo (Alex).** See the demo section below.

**20. Conclusion (Alsean).** Read the three cards: what we found, lessons learned, future work. The one-line takeaway: the right priority queue depends on how dense the graph is.

**21. References (Alsean).** "These are our sources." Then "Thank you, any questions?"

## The demo (Alex)

### Before you present

1. Open Terminal in the repo folder and run:
   ```bash
   git pull
   javac -d out src/*.java
   ```
2. Make the text big: press Cmd + a few times.
3. Run it once so you know it works, then `clear`.
4. Have the terminal ready next to the slides, so switching is one click.

### Run it

```bash
java -Xmx2g -cp out Demo step
```

`step` makes it stop after each part until you press Enter, so you can talk. Without `step` it prints everything at once.

Every part prints one line per version:

```
  indexed-heap      0.01 ms   queue 3        distance 9   path 0 -> 2 -> 1 -> 4 -> 5 -> 6
```

- **ms:** how long that version took
- **queue:** the most entries its queue held at once (our memory measure). The array has no queue
- **distance / path:** the shortest route to the target
- **"All versions agree on every distance":** all three got the exact same answers for every vertex

Your times will be a little different each run. The pattern stays the same.

### What to say (about 2 minutes)

**Part 1, small graph (35 s).** "Here's a 7-vertex graph, going from 0 to 6. All three find the same path, 0 → 2 → 1 → 4 → 5 → 6, total 9. The direct-looking edge 0 → 1 costs 7 by itself, so the shortest path takes the long way around." Press Enter.

**Part 2, edge cases (35 s).** "This graph has one of each tricky case." Point at the "Every vertex" table:
- vertex 1 = 0: zero-weight edge
- vertex 2 = 3: two parallel edges, the cheaper one (3) wins over 5
- vertex 3 = 4: a tie, two paths both cost 4
- vertex 5 = `-`: unreachable
- there's also a self-loop on vertex 3 that gets ignored

"Every version matches the expected answers." Press Enter.

**Part 3, dense graph (25 s).** "3,000 vertices, about 4.5 million edges. The array is the fastest here, and the lazy heap's queue is over 4× bigger than the indexed heap's." Press Enter.

**Part 4, sparse graph (25 s).** "20,000 vertices, only 80,000 edges. Now the heaps are way faster than the array, around 80×. Same answers from all three." Back to the slides.

### If something goes wrong

If the laptop or terminal acts up, skip it and say what it shows: "The demo runs all three on a small graph, edge cases, a dense graph and a sparse graph. They all agree, the array wins on dense, and the heaps win on sparse." Then go to the conclusion. Don't spend more than 2:15 on it.

## Code each person should know

Everyone should be able to explain the whole project at a high level. Know these files well, since they're your part:

**Alex**
- `Graph.java`, `Edge.java`, `GraphGenerator.java`: the graph, and how random graphs are made (backbone so everything is reachable, fixed seeds)
- `ShortestPathAlgorithm.java`, `Result.java`: the interface, and how paths come from the parent array
- `BellmanFord.java`: the answer key and `compare()`
- `IndexedMinHeap.java`, `IndexedHeapDijkstra.java`: the heap and the position array that makes decrease-key fast
- `Demo.java`

**Alsean**
- `ArrayDijkstra.java`: scan every unvisited vertex for the smallest, stop when the rest are unreachable
- `edgeCaseGraphs()` in `Tests.java` and `graphs/edge-cases.txt`: what each edge case is and why it matters

**Anthony**
- `LazyHeapDijkstra.java`: why it adds a new entry instead of decrease-key, and why old entries get skipped
- `Counters.java` and `testCounters()` in `Tests.java`: what each counter means
- `results/make_plots.py` and `results/plots/`: what each line on the plots means, and how the dashed Big-O curves are scaled to the data

**Luke**
- `Benchmark.java`: warm-up, timed runs vs counting runs, what goes in the CSVs
- `testGraphs()` and `testHeapAgreementOnLargeGraphs()` in `Tests.java`
- `results/`: both runs, `crossover.md`, `summary.csv` (median and IQR)

## Slides each person owns

Everyone should go through the whole deck once. Know your own slides well enough to talk without reading:

- **Alex:** 1, 6–11, 19 (demo)
- **Alsean:** 2–5, 20–21
- **Luke:** 12–14
- **Anthony:** 15–18

## Likely questions

**Why does the array win on dense graphs? (Anthony)**
On a dense graph E is about V². The array costs O(V²) no matter what. The heaps pay a log V for each of those ~V² edge updates, so O(V² log V). Once there are that many edges, the heap's extra work costs more than it saves.

**Why didn't you do a Fibonacci heap? (Alsean)**
It was optional in our proposal. It's better on paper, O(E + V log V), but past studies we cite ([5], [6]) found it's usually slower in practice because of the extra bookkeeping. It's in our future work.

**Why was the indexed heap faster than the lazy one? (Anthony)**
The lazy heap never removes old entries, it just skips them later, so its queue got up to 4.6× bigger. Each entry is a separate Java object, so more entries means more memory and more time.

**How do you know your answers are right? (Luke)**
Every version is checked against Bellman-Ford on 27 graphs, including the edge cases. On the big graphs where Bellman-Ford is too slow, the two heaps are checked against each other. The demo also shows all three agreeing.

**Why medians and not averages? Why the warm-up? (Luke)**
A few runs get slowed down by Java's garbage collection, and one slow run throws off an average. The median ignores those. The warm-up is because Java speeds up code after it has run a few times, so the first runs are always slow.

**Why do the small graphs look weird on the plot? (Luke)**
Those runs take well under a millisecond, where timer noise is about as big as the work itself. Between our two full runs they varied by up to 2×, so we go by the bigger sizes.

**What about negative edge weights? (Alsean)**
Dijkstra doesn't work with them, since it assumes a finished vertex can't get any cheaper later. Our graph class rejects negative weights. Bellman-Ford handles them, which is part of why it's slower.

**What's decrease-key, and what's the position array for? (Alex)**
Decrease-key lowers a vertex's distance while it's in the heap. A normal heap would have to search for the vertex first. The position array remembers where each vertex sits in the heap, so we jump straight to it.

**How much memory does each one use? (Anthony)**
The array uses no queue at all. The indexed heap holds at most V entries. The lazy heap can hold up to E entries. Slide 18 has the real numbers.

**Would the results be different on another computer? (Alex)**
The times would change a bit, but the pattern doesn't. We ran it on two different Macs and got the same winners.

## Before tomorrow

- [ ] Read `Algo9_PP.pdf` (GroupMe)
- [ ] Skim the README and the code files listed under your name
- [ ] Go through the whole deck, then practice your slides out loud with a timer
- [ ] Everyone together: one full run-through with the demo, timed (aim for 11–12 minutes)
- [ ] Submit by Sat Nov 21, end of day

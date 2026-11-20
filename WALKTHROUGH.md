# Walkthrough

Presentation **Fri Nov 20, 4:30**. Report due **Mon Nov 30**.

Focus right now is the project itself: code, benchmarks, slides. Not the report. Just keep a few notes on what you build so the report is easy later.

## What waits on what

```
Alex's setup (done)
 ├── Array version (Alsean)      ┐
 ├── Indexed heap (Alex)         ├── Full benchmark run (Luke) ── Plots (Anthony) ── Results slides + deck (Anthony)
 ├── Lazy heap (Anthony)         ┘                             └─ Results + crossover (Luke)
 ├── Benchmark harness (Luke) ───┘
 └── Demo (Alex), once all 3 versions work
```

- Setup is in, so everyone can start Fri.
- The three versions and Luke's harness can all happen at the same time.
- The full run needs all three versions passing the tests.
- Plots need Luke's CSVs. Slides need the plots and everyone's slides.

## Alex

- [x] Setup: Graph, generator, Bellman-Ford checker, interface, Counters, tests
- [x] Indexed heap version, `IndexedHeapDijkstra.java`
- [x] Demo program, `Demo.java` (picks up your version automatically once it's done), now with edge cases and queue size
- [ ] Review PRs
- [x] Slides: everything except results (see Slides below)
- [ ] Report, later: lit review, methods for my part

## Alsean

- [x] Array version, `ArrayDijkstra.java` (Alex took it over)
- [x] Edge-case graphs in `Tests.java` → `edgeCaseGraphs()` (Alex took it over)
- [x] Requirements slide (Alex did it, give it a look)
- [ ] Report, later: put it together, write intro, objectives, conclusion

## Anthony

- [x] Lazy heap version, `LazyHeapDijkstra.java` (Alex took it over)
- [x] Check every version counts the right things, now `testCounters()` in `Tests.java` (Alex took it over)
- [ ] Plots, once Luke has CSVs: time vs E (log-log) with the theory curves on top, time ratios per density, op counts, queue sizes (Alex taking over)
- [ ] Results slides (Alex taking over)
- [ ] Put the deck together (Alex taking over)

## Luke

- [x] Random graphs in `Tests.java` → `testGraphs()`: V = 100 and 300, all 3 densities, a few seeds
- [x] Benchmark harness, `Benchmark.java` (steps are in the TODO): warm up JVM, repeat runs, don't time generation or file I/O, CSV out, write down hardware / JVM version / heap settings
- [x] Counting runs separate from timing runs (`Counters.on()` vs `Counters.off()`)
- [x] Big graphs (V = 10k, 100k, sparse) in the benchmark
- [x] Pilot run (Sun): make sure sizes work, note any time/memory limits

Left to do (tonight, in this order):

- [x] Full run, about 20 seconds:
  ```bash
  git pull
  javac -d out src/*.java && java -Xmx4g -cp out Benchmark
  ```
  Write down what it prints about the machine, plus your CPU model and RAM from your system settings (we said we'd record those).
- [x] Repeat run: the benchmark overwrites the CSVs, so first rename them to `results/times-run1.csv` and `results/counts-run1.csv`, then run it again. If the medians from both runs are close, the results are stable.
- [x] Check the two heaps agree on the big graphs: Bellman-Ford is too slow at V = 10k and 100k, so run the indexed heap and lazy heap on the same sparse graph and check their `dist` arrays match. About 10 lines, e.g. a small test in `Tests.java`.
- [x] Crossover: from the CSVs, which version is fastest at each size and density, and where the winner changes. A few lines or a small table in `results/crossover.md` is enough.
- [x] Push it all in a PR: both runs' CSVs, the heap check and the crossover notes.

## Slides

Deck is in Google Slides. Everything's done except:

- [ ] Runtime plots, theory curves on top (Alex, needs Luke's CSVs)
- [ ] Crossover + op counts (Luke and Alex, needs the full run)
- [ ] Conclusion: what we found (Alex, needs results). Lessons learned and future work can go in now
- [ ] Screenshot of a merged PR on the GitHub workflow slide (whoever merges first)

Keep slide titles as the takeaway ("Heaps win on sparse graphs"), not just a label ("Results").

## Dates

| When | What |
| --- | --- |
| Thu Nov 12 | Setup done (Alex) |
| Fri Nov 13 | Everyone starts. Alex: indexed heap. Alsean: array. Anthony: lazy heap. Luke: harness |
| Sat Nov 14 | Alex: demo. Alsean: edge-case graphs. Anthony: counters check. Luke: random test graphs, harness ready |
| Sun Nov 15 | All 3 versions pass tests. Luke: pilot run. Alsean: requirements slide |
| Mon Nov 16 | Code merged. Luke: full run |
| Tue Nov 17 | Anthony: plots. Luke: results + crossover. Alex: design + dev strategy slides |
| Wed Nov 18 | Luke: repeat runs. Anthony: results slides, deck together |
| Thu Nov 19 | Project done. Timed run with demo (keep it 12 min) |
| Fri Nov 20 | Submit by noon (due 3 PM). Present 4:30 |
| Nov 21 – 29 | Write report from notes, plots, slides. Alsean puts it together |
| Mon Nov 30 | Report due |

## How we work

- Make a branch for your task, open a PR into main, someone reviews it. Nobody pushes straight to main.
- Run the tests before opening a PR:
  ```bash
  javac -d out src/*.java
  java -cp out Tests
  ```
- Your version is done when `Tests` says it matches Bellman-Ford instead of "not done yet".

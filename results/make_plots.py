"""
Makes the result plots for the slides and the report.

Reads results/times.csv and results/times-run1.csv (made by Benchmark.java),
saves PNGs in results/plots/ and a median + IQR table in results/summary.csv.

Run from the repo folder:
    python3 results/make_plots.py

Needs matplotlib (pip install matplotlib).
"""
import csv
import math
import os
import statistics
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

RESULTS = "results"
OUT = os.path.join(RESULTS, "plots")

VERSIONS = ["array", "indexed-heap", "lazy-heap"]
NAMES = {"array": "Array scan", "indexed-heap": "Indexed heap", "lazy-heap": "Lazy heap"}
COLORS = {"array": "#B5651D", "indexed-heap": "#2F5D8A", "lazy-heap": "#3F7255"}
TEXT, MUTED, GRID = "#1F2937", "#6B7280", "#E5E7EB"


def load_raw_times():
    """Every timed run in ms for each (version, V, density), pooling both full runs."""
    times = defaultdict(list)
    edges = {}
    for name in ("times-run1.csv", "times.csv"):
        path = os.path.join(RESULTS, name)
        if not os.path.exists(path):
            continue
        for row in csv.DictReader(open(path)):
            key = (row["version"], int(row["V"]), row["density"])
            times[key].append(int(row["nanos"]) / 1e6)
            edges[(int(row["V"]), row["density"])] = int(row["E"])
    return times, edges


def write_summary(times, edges):
    """results/summary.csv: median and IQR (middle 50% spread) per setup, in ms."""
    with open(os.path.join(RESULTS, "summary.csv"), "w", newline="") as f:
        out = csv.writer(f)
        out.writerow(["version", "V", "E", "density", "runs", "median_ms", "q1_ms", "q3_ms", "iqr_ms"])
        for (version, v, density), ts in sorted(times.items(), key=lambda kv: (kv[0][2], kv[0][1], kv[0][0])):
            q1, _, q3 = statistics.quantiles(ts, n=4)
            out.writerow([version, v, edges[(v, density)], density, len(ts),
                          f"{statistics.median(ts):.4f}", f"{q1:.4f}", f"{q3:.4f}", f"{q3 - q1:.4f}"])


def theory(version, v, e):
    """The Big-O growth function for each version (no constant)."""
    if version == "array":
        return v * v + e
    if version == "indexed-heap":
        return (v + e) * math.log2(v)
    return e * math.log2(v)


def style(ax):
    ax.set_xscale("log")
    ax.set_yscale("log")
    for side in ("top", "right", "left"):
        ax.spines[side].set_visible(False)
    ax.spines["bottom"].set_color("#BFBFBF")
    ax.grid(axis="y", color=GRID, linewidth=0.8)
    ax.tick_params(colors=MUTED, labelsize=11, length=0)
    ax.set_axisbelow(True)


def ms_label(y, _):
    return f"{y:g} ms"


def runtime_plot(medians, edges):
    """Time vs V (log-log), sparse and dense side by side, with the Big-O curves dashed."""
    fig, axes = plt.subplots(1, 2, figsize=(10, 4.2), dpi=200)
    for ax, (density, label) in zip(axes, [("SPARSE", "Sparse graphs (E ≈ 4V)"),
                                           ("DENSE", "Dense graphs (E ≈ 0.5·V²)")]):
        style(ax)
        for version in VERSIONS:
            pts = sorted((v, t) for (ver, v, d), t in medians.items() if ver == version and d == density)
            if not pts:
                continue
            vs = [p[0] for p in pts]
            ts = [p[1] for p in pts]
            ax.plot(vs, ts, color=COLORS[version], linewidth=2.2, marker="o", markersize=4,
                    label=NAMES[version])

            # Scale the Big-O curve to the measured points (V >= 1000, where timer noise is small)
            big = [(v, t) for v, t in pts if v >= 1000] or pts
            c = math.exp(statistics.mean(math.log(t / theory(version, v, edges[(v, density)])) for v, t in big))
            ax.plot(vs, [c * theory(version, v, edges[(v, density)]) for v in vs],
                    color=COLORS[version], linewidth=1.2, linestyle="--", alpha=0.7)

        ax.set_title(label, loc="left", color=TEXT, fontsize=13, pad=10)
        ax.set_xlabel("Vertices (V)", color=MUTED, fontsize=11)
        ax.yaxis.set_major_formatter(matplotlib.ticker.FuncFormatter(ms_label))
        ax.xaxis.set_major_formatter(matplotlib.ticker.FuncFormatter(lambda x, _: f"{int(x):,}"))
        ax.margins(x=0.08)

    # One legend for both panels: the three versions plus what the dashed lines mean.
    handles, labels = axes[0].get_legend_handles_labels()
    handles.append(matplotlib.lines.Line2D([], [], color=MUTED, linestyle="--", linewidth=1.2))
    labels.append("Big-O curve (scaled)")
    leg = fig.legend(handles, labels, loc="upper center", ncol=4, frameon=False, fontsize=11,
                     bbox_to_anchor=(0.5, 1.0))
    for text in leg.get_texts():
        text.set_color(TEXT)
    fig.tight_layout(rect=(0, 0, 1, 0.92))
    fig.savefig(os.path.join(OUT, "runtime.png"), transparent=True)
    plt.close(fig)


def ratio_plot(medians):
    """Array time / indexed-heap time, per density. Above 1 = heap faster, below 1 = array faster."""
    fig, ax = plt.subplots(figsize=(6.2, 4.0), dpi=200)
    style(ax)
    ax.set_yscale("log")
    colors = {"SPARSE": "#2F5D8A", "MEDIUM": "#7A9CC6", "DENSE": "#B5651D"}
    names = {"SPARSE": "Sparse", "MEDIUM": "Medium", "DENSE": "Dense"}
    for density in ("SPARSE", "MEDIUM", "DENSE"):
        vs = sorted(v for (ver, v, d) in medians if ver == "array" and d == density)
        ratios = [medians[("array", v, density)] / medians[("indexed-heap", v, density)] for v in vs]
        ax.plot(vs, ratios, color=colors[density], linewidth=2.2, marker="o", markersize=4)
        ax.annotate(f"{names[density]}  {ratios[-1]:.1f}×", (vs[-1], ratios[-1]), xytext=(6, 0),
                    textcoords="offset points", color=colors[density], fontsize=11, va="center",
                    fontweight="bold")
    ax.axhline(1, color=TEXT, linewidth=1, linestyle=":")
    ax.annotate("Same speed", (100, 1), xytext=(0, 4), textcoords="offset points", color=MUTED, fontsize=10)
    ax.set_xlabel("Vertices (V)", color=MUTED, fontsize=11)
    ax.set_ylabel("Array time ÷ indexed heap time", color=MUTED, fontsize=11)
    ax.xaxis.set_major_formatter(matplotlib.ticker.FuncFormatter(lambda x, _: f"{int(x):,}"))
    ax.set_yticks([0.5, 1, 2, 5, 10, 20])
    ax.yaxis.set_minor_locator(matplotlib.ticker.NullLocator())
    ax.yaxis.set_major_formatter(matplotlib.ticker.FuncFormatter(lambda y, _: f"{y:g}×"))
    ax.set_ylim(0.5, 35)
    ax.set_xlim(right=3000 * 4)
    fig.tight_layout()
    fig.savefig(os.path.join(OUT, "array-vs-heap.png"), transparent=True)
    plt.close(fig)


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    times, edges = load_raw_times()
    write_summary(times, edges)
    medians = {k: statistics.median(v) for k, v in times.items()}
    runtime_plot(medians, edges)
    ratio_plot(medians)
    print("saved plots to", OUT)

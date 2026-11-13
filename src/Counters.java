/**
 * Counts the operations each version does (Proposal section 4).
 *
 * We collect counts and times in separate runs, so counting never slows down
 * a timed run. For timing, pass Counters.off(): every count() call then
 * returns right away. For a counting run, pass Counters.on().
 *
 * Not every version uses every counter. For example, the array version
 * never does heap inserts, so that stays 0.
 */
public class Counters {

    private final boolean enabled;

    public long inserts = 0;          // heap inserts (both heap versions)
    public long decreaseKeys = 0;     // decrease-key calls (indexed heap only)
    public long extractMins = 0;      // remove-the-smallest calls (both heap versions)
    public long staleSkipped = 0;     // outdated entries thrown away (lazy heap only)
    public long arrayScans = 0;       // vertices looked at while scanning for the min (array only)
    public long relaxations = 0;      // edges checked with "is this path shorter?" (all versions)
    public long maxQueueSize = 0;     // biggest the queue ever got (both heap versions)

    private Counters(boolean enabled) {
        this.enabled = enabled;
    }

    /** Counters that actually count. Use for counting runs. */
    public static Counters on() {
        return new Counters(true);
    }

    /** Counters that do nothing. Use for timing runs. */
    public static Counters off() {
        return new Counters(false);
    }

    public void countInsert() {
        if (enabled) inserts++;
    }

    public void countDecreaseKey() {
        if (enabled) decreaseKeys++;
    }

    public void countExtractMin() {
        if (enabled) extractMins++;
    }

    public void countStaleSkipped() {
        if (enabled) staleSkipped++;
    }

    public void countArrayScan() {
        if (enabled) arrayScans++;
    }

    public void countRelaxation() {
        if (enabled) relaxations++;
    }

    /** Call this whenever the queue changes size; it keeps the biggest size seen. */
    public void recordQueueSize(int size) {
        if (enabled && size > maxQueueSize) maxQueueSize = size;
    }

    /** Column names for a CSV file, in the same order as toCsvRow(). */
    public static String csvHeader() {
        return "inserts,decrease_keys,extract_mins,stale_skipped,array_scans,relaxations,max_queue_size";
    }

    public String toCsvRow() {
        return inserts + "," + decreaseKeys + "," + extractMins + "," + staleSkipped + ","
                + arrayScans + "," + relaxations + "," + maxQueueSize;
    }

    @Override
    public String toString() {
        return "inserts=" + inserts + ", decreaseKeys=" + decreaseKeys + ", extractMins=" + extractMins
                + ", staleSkipped=" + staleSkipped + ", arrayScans=" + arrayScans
                + ", relaxations=" + relaxations + ", maxQueueSize=" + maxQueueSize;
    }
}

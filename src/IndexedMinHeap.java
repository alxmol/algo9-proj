/**
 * A binary min-heap of vertex ids, ordered by each vertex's key (its distance).
 * Owner: Alex.
 *
 * A normal heap can't change a value that's already inside it without
 * searching for it first, which takes O(n). This one is "indexed": it keeps a
 * position array, pos[v] = where vertex v sits in the heap. So decreaseKey()
 * can jump straight to the vertex in O(1) and fix the heap in O(log n).
 *
 * Each vertex is in the heap at most once, so the heap never holds more than
 * V entries (O(V) space). See Sedgewick and Wayne, IndexMinPQ [2].
 *
 * How the heap is laid out (positions start at 0):
 *   - heap[0] is the vertex with the smallest key
 *   - the children of position i are at 2i+1 and 2i+2
 *   - the parent of position i is at (i-1)/2
 *   - every vertex's key is <= its children's keys
 */
public class IndexedMinHeap {

    private final int[] heap;   // heap[i] = the vertex at position i
    private final int[] pos;    // pos[v]  = position of vertex v in heap[], or -1 if v isn't in it
    private final long[] key;   // key[v]  = vertex v's current key (its distance)
    private int size = 0;       // how many vertices are in the heap right now

    /** An empty heap that can hold vertices 0 to capacity-1. */
    public IndexedMinHeap(int capacity) {
        heap = new int[capacity];
        pos = new int[capacity];
        key = new long[capacity];
        for (int v = 0; v < capacity; v++) {
            pos[v] = -1;   // nothing is in the heap yet
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    /** True if vertex v is in the heap right now. */
    public boolean contains(int v) {
        return pos[v] != -1;
    }

    /** Adds vertex v with the given key. O(log n). */
    public void insert(int v, long newKey) {
        key[v] = newKey;
        // Put it at the very end, then move it up until it's in the right place.
        heap[size] = v;
        pos[v] = size;
        size++;
        moveUp(size - 1);
    }

    /** Lowers vertex v's key. v must already be in the heap. O(log n). */
    public void decreaseKey(int v, long newKey) {
        key[v] = newKey;
        // A smaller key can only need to move up, never down.
        moveUp(pos[v]);
    }

    /** Removes and returns the vertex with the smallest key. O(log n). */
    public int extractMin() {
        int min = heap[0];

        // Move the last vertex to the top, then move it down to where it belongs.
        size--;
        swap(0, size);
        pos[min] = -1;   // min is out of the heap now
        if (size > 0) {
            moveDown(0);
        }
        return min;
    }

    /** Swaps the vertex at position i with its parent while it's smaller than the parent. */
    private void moveUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (key[heap[i]] >= key[heap[parent]]) {
                break;   // parent is already smaller or equal, we're done
            }
            swap(i, parent);
            i = parent;
        }
    }

    /** Swaps the vertex at position i with its smaller child while a child is smaller. */
    private void moveDown(int i) {
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int smallest = i;

            if (left < size && key[heap[left]] < key[heap[smallest]]) {
                smallest = left;
            }
            if (right < size && key[heap[right]] < key[heap[smallest]]) {
                smallest = right;
            }
            if (smallest == i) {
                break;   // both children are bigger or equal, we're done
            }
            swap(i, smallest);
            i = smallest;
        }
    }

    /** Swaps two positions in the heap and keeps pos[] in sync. */
    private void swap(int i, int j) {
        int vi = heap[i];
        int vj = heap[j];
        heap[i] = vj;
        heap[j] = vi;
        pos[vj] = i;
        pos[vi] = j;
    }
}

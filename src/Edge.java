/**
 * One directed edge in the graph: it goes to vertex "to" and costs "weight".
 * (The "from" vertex is whichever list in Graph this edge is stored in.)
 */
public record Edge(int to, int weight) {
}

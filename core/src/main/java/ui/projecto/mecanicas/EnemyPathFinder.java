package ui.projecto.mecanicas;

import com.badlogic.gdx.math.Vector2;
import java.util.*;

public class EnemyPathFinder {

    private final MapManager map;
    private final int tileSize;

    public EnemyPathFinder(MapManager mapManager, int tileSize) {
        this.map = mapManager;
        this.tileSize = tileSize;
    }

    public Vector2 findNextStep(float startX, float startY, float targetX, float targetY) {

        float dx = targetX - startX;
        float dy = targetY - startY;

        if (dx * dx + dy * dy < 16f) {
            return new Vector2(targetX, targetY);
        }

        Node startNode = new Node(worldToTile(startX, startY));
        Node targetNode = new Node(worldToTile(targetX, targetY));

        if (isBlocked(startNode) || isBlocked(targetNode)) return null;

        List<Node> path = aStar(startNode, targetNode);

        if (path == null || path.size() < 2) return null;

        Node next = path.get(1);
        return tileToWorld(next.x, next.y);
    }

    private boolean isBlocked(Node n) {
        return map.isBlocked(n.x * tileSize, n.y * tileSize, tileSize, tileSize);
    }

    private int[] worldToTile(float x, float y) {
        return new int[]{(int)(x / tileSize), (int)(y / tileSize)};
    }

    private Vector2 tileToWorld(float tx, float ty) {
        return new Vector2(
            tx * tileSize + tileSize / 2f,
            ty * tileSize + tileSize / 2f
        );
    }

    private static class Node {
        int x, y;
        Node parent;
        float g = Float.MAX_VALUE;
        float h = 0;

        Node(int[] pos) {
            x = pos[0];
            y = pos[1];
        }
        float f() {
            return g + h;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Node)) return false;
            Node n = (Node) o;
            return n.x == x && n.y == y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private List<Node> aStar(Node start, Node target) {
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::f));
        Map<String, Node> allNodes = new HashMap<>();
        Set<String> closed = new HashSet<>();

        start.g = 0;
        start.h = heuristic(start, target);

        open.add(start);
        allNodes.put(key(start.x, start.y), start);

        while (!open.isEmpty()) {
            Node current = open.poll();
            String currentKey = key(current.x, current.y);

            if (current.equals(target)) {
                return reconstructPath(current);
            }
            closed.add(currentKey);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (Math.abs(dx) + Math.abs(dy) != 1) continue;

                    int nx = current.x + dx;
                    int ny = current.y + dy;

                    String neighborKey = key(nx, ny);
                    if (closed.contains(neighborKey)) continue;

                    float worldX = nx * tileSize;
                    float worldY = ny * tileSize;

                    if (map.isBlocked(worldX, worldY, tileSize, tileSize)) continue;
                    Node neighbor = allNodes.get(neighborKey);
                    if (neighbor == null) {
                        neighbor = new Node(new int[]{nx, ny});
                        allNodes.put(neighborKey, neighbor);
                    }
                    float tentativeG = current.g + 1;
                    if (tentativeG < neighbor.g) {
                        neighbor.g = tentativeG;
                        neighbor.h = heuristic(neighbor, target);
                        neighbor.parent = current;

                        if (!open.contains(neighbor)) {
                            open.add(neighbor);
                        }
                    }
                }
            }
        }
        return null;
    }

    private List<Node> reconstructPath(Node node) {
        List<Node> path = new ArrayList<>();
        while (node != null) {
            path.add(0, node);
            node = node.parent;
        }
        return path;
    }

    private float heuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private String key(int x, int y) {
        return x + "," + y;
    }
}

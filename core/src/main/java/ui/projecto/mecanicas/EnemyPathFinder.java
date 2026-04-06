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
        Node startNode = new Node(worldToTile(startX, startY));
        Node targetNode = new Node(worldToTile(targetX, targetY));

        if (map.isBlocked(startNode.x * tileSize, startNode.y * tileSize, tileSize, tileSize) ||
            map.isBlocked(targetNode.x * tileSize, targetNode.y * tileSize, tileSize, tileSize)) {
            return null;
        }

        List<Node> path = aStar(startNode, targetNode);
        if (path == null || path.size() < 2) return null;

        Node next = path.get(1);
        return tileToWorld(next.x, next.y);
    }

    private int[] worldToTile(float x, float y) {
        return new int[]{(int)(x / tileSize), (int)(y / tileSize)};
    }

    private Vector2 tileToWorld(float tx, float ty) {
        return new Vector2(tx * tileSize, ty * tileSize);
    }

    private static class Node {
        int x, y;
        Node parent;
        float g,h;

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
            return Objects.hash(x,y);
        }
    }

    private List<Node> aStar(Node start, Node target) {
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::f));
        Set<Node> closed = new HashSet<>();

        start.g = 0;
        start.h = heuristic(start, target);
        open.add(start);

        while (!open.isEmpty()) {
            Node current = open.poll();

            if (current.equals(target)){
                List<Node> path = new ArrayList<>();
                Node temp = current;
                while (temp != null){
                    path.add(0, temp);
                    temp = temp.parent;
                }
                return path;
            }
            closed.add(current);

            for (int dx = -1; dx <= 1; dx++){
                for (int dy = -1; dy <= 1; dy++){
                    if (Math.abs(dx) + Math.abs(dy) != 1) continue;

                    Node neighbor = new Node(new int[]{current.x + dx, current.y + dy});
                    if (closed.contains(neighbor)) continue;

                    float worldX = neighbor.x * tileSize;
                    float worldY = neighbor.y * tileSize;

                    if (map.isBlocked(worldX, worldY, tileSize, tileSize)) continue;

                    float tentativeG = current.g + 1;
                    boolean inOpen = open.contains(neighbor);
                    if (!inOpen || tentativeG < neighbor.g) {
                        neighbor.g = tentativeG;
                        neighbor.h = heuristic(neighbor, target);
                        neighbor.parent = current;
                        if (!inOpen) open.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    private float heuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}

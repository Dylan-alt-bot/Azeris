package ui.projecto.mecanicas.Enemies;

import com.badlogic.gdx.math.Vector2;
import ui.projecto.mecanicas.MapManager;

import java.util.*;

public class EnemyPathFinder {
    private final MapManager map;
    private final int tileSize;
    private final float maxStep;

    public EnemyPathFinder(MapManager mapManager, int tileSize) {
        this.map = mapManager;
        this.tileSize = tileSize;
        this.maxStep = tileSize * 1.5f;
    }

    public Vector2 findNextStep(float startX, float startY, float targetX, float targetY) {
        float dx = targetX - startX;
        float dy = targetY - startY;
        if (dx * dx + dy * dy < (tileSize * 0.5f) * (tileSize * 0.5f)) {
            return new Vector2(targetX, targetY);
        }
        Node startNode = new Node(worldToTile(startX, startY));
        Node targetNode = new Node(worldToTile(targetX, targetY));
        if (isBlocked(startNode)) {
            startNode = findNearestFreeNode(startNode);
            if (startNode == null) return null;
        }
        if (isBlocked(targetNode)) return null;
        List<Node> path = aStar(startNode, targetNode);
        if (path == null || path.size() < 2) return null;
        Node next = getNextSmoothNode(startNode, path);
        float tileWorldX = next.x * tileSize + tileSize / 2f;
        float tileWorldY = next.y * tileSize + tileSize / 2f;
        float stepDx = tileWorldX - startX;
        float stepDy = tileWorldY - startY;
        float stepDist = (float) Math.sqrt(stepDx * stepDx + stepDy * stepDy);
        if (stepDist > maxStep) {
            float scale = maxStep / stepDist;
            return new Vector2(startX + stepDx * scale, startY + stepDy * scale);
        }
        return new Vector2(tileWorldX, tileWorldY);
    }

    private Node findNearestFreeNode(Node blocked) {
        for (int radius = 1; radius <= 3; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    Node candidate = new Node(new int[]{blocked.x + dx, blocked.y + dy});
                    if (!isBlocked(candidate)) return candidate;
                }
            }
        }
        return null;
    }

    private Node getNextSmoothNode(Node current, List<Node> path) {
        if (path.size() <= 2) return path.get(1);
        for (int i = Math.min(2, path.size() - 1); i > 0; i--) {
            Node candidate = path.get(i);
            if (isWalkableLine(current, candidate)) {
                return candidate;
            }
        }
        return path.get(1);
    }

    private boolean isWalkableLine(Node from, Node to) {
        float fromX = from.x * tileSize + tileSize / 2f;
        float fromY = from.y * tileSize + tileSize / 2f;
        float toX = to.x * tileSize + tileSize / 2f;
        float toY = to.y * tileSize + tileSize / 2f;

        return map.isLineOfSightFree(fromX, fromY, toX, toY);
    }

    public boolean hasLineOfSight(float startX, float startY, float targetX, float targetY) {
        return map.isLineOfSightFree(startX, startY, targetX, targetY);
    }

    private boolean isDiagonalWalkable(Node current, Node neighbor) {
        if (Math.abs(neighbor.x - current.x) == 1 && Math.abs(neighbor.y - current.y) == 1) {
            Node horizontal = new Node(new int[]{neighbor.x, current.y});
            Node vertical = new Node(new int[]{current.x, neighbor.y});
            return !isBlocked(horizontal) && !isBlocked(vertical);
        }
        return true;
    }

    private boolean isBlocked(Node n) {
        return map.isBlocked(n.x * tileSize, n.y * tileSize, tileSize, tileSize);
    }

    private int[] worldToTile(float x, float y) {
        return new int[]{(int)(x / tileSize), (int)(y / tileSize)};
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
                    if (dx == 0 && dy == 0) continue;
                    int nx = current.x + dx;
                    int ny = current.y + dy;
                    String neighborKey = key(nx, ny);
                    if (closed.contains(neighborKey)) continue;
                    Node neighbor = new Node(new int[]{nx, ny});
                    if (isBlocked(neighbor)) continue;
                    if (!isDiagonalWalkable(current, neighbor)) continue;

                    Node existing = allNodes.get(neighborKey);
                    if (existing != null) {
                        neighbor = existing;
                    }

                    float moveCost = (dx != 0 && dy != 0) ? 1.414f : 1.0f;
                    float tentativeG = current.g + moveCost;
                    if (tentativeG < neighbor.g) {
                        neighbor.g = tentativeG;
                        neighbor.h = heuristic(neighbor, target);
                        neighbor.parent = current;
                        if (existing == null) {
                            allNodes.put(neighborKey, neighbor);
                            open.add(neighbor);
                        } else if (!open.contains(neighbor)) {
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

package io.github.azerisproyecte.levelGenerator;

// DungeonGenerator.java

import java.util.*;

public class DungeonGenerator {
    private Random random;
    private int gridWidth, gridHeight;
    private Room[][] roomGrid;
    private List<Room> rooms;

    // Track actual world positions for each grid cell
    private float[][] roomWorldX;
    private float[][] roomWorldY;

    private int uniformRoomWidth;
    private int uniformRoomHeight;

    public DungeonGenerator(int gridWidth, int gridHeight) {
        this.random = new Random();
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.roomGrid = new Room[gridWidth][gridHeight];

        this.roomWorldX = new float[gridWidth][gridHeight];
        this.roomWorldY = new float[gridWidth][gridHeight];
        this.rooms = new ArrayList<>();
    }

    public List<Room> generateDungeon(int roomCount) {
        rooms.clear();



        uniformRoomWidth = random.nextInt(5) + 10;  // Random width between 10-14
        uniformRoomHeight = random.nextInt(5) + 10;

        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                roomGrid[i][j] = null;
            }
        }

        // Start with first room at grid center, world position (0,0)
        int startX = gridWidth / 2;
        int startY = gridHeight / 2;

        Room startRoom = createUniformRoom(startX, startY);
        roomGrid[startX][startY] = startRoom;
        roomWorldX[startX][startY] = 0;  // First room starts at 0
        roomWorldY[startX][startY] = 0;
        rooms.add(startRoom);

        // Generate additional rooms
        for (int i = 1; i < roomCount; i++) {
            if (!addNewRoom()) {
                System.out.println("Placed " + i + " rooms");
                break;
            }
        }

        addDoorsToAllConnections();

        return rooms;
    }

    // CHANGED: Now uses uniform room size instead of random
    private Room createUniformRoom(int gridX, int gridY) {
        return new Room(gridX, gridY, uniformRoomWidth, uniformRoomHeight);
    }

    private boolean addNewRoom() {
        if (rooms.isEmpty()) return false;

        // Try multiple times to place a room
        for (int attempts = 0; attempts < 1000; attempts++) { // Increased attempts
            // Pick a random existing room to attach to - PREFER rooms with fewer neighbors
            Room existingRoom = pickRoomWithSpace();

            // Pick a random direction
            Door.Direction dir = Door.Direction.values()[random.nextInt(4)];

            int newGridX = existingRoom.x;
            int newGridY = existingRoom.y;

            // Calculate new grid position
            switch (dir) {
                case NORTH: newGridY++; break;
                case SOUTH: newGridY--; break;
                case EAST:  newGridX++; break;
                case WEST:  newGridX--; break;
            }

            // Check if grid position is valid and empty
            if (isValidGridPosition(newGridX, newGridY) && roomGrid[newGridX][newGridY] == null) {

                // NEW: Check if this direction has FEWER neighbors (prefer open space)
                if (hasTooManyNeighbors(newGridX, newGridY)) {
                    continue;
                }

                // Calculate world position based on the existing room's END position
                float newWorldX = 0;
                float newWorldY = 0;

                float existingStartX = roomWorldX[existingRoom.x][existingRoom.y];
                float existingStartY = roomWorldY[existingRoom.x][existingRoom.y];
                float existingEndX = existingStartX + existingRoom.width;
                float existingEndY = existingStartY + existingRoom.height;

                Room newRoom = createUniformRoom(newGridX, newGridY);

                // Position the new room based on direction (same overlap logic)
                switch (dir) {
                    case EAST:
                        newWorldX = existingEndX - 1;
                        newWorldY = existingStartY;
                        break;
                    case WEST:
                        newWorldX = existingStartX - newRoom.width + 1;
                        newWorldY = existingStartY;
                        break;
                    case NORTH:
                        newWorldX = existingStartX;
                        newWorldY = existingEndY - 1;
                        break;
                    case SOUTH:
                        newWorldX = existingStartX;
                        newWorldY = existingStartY - newRoom.height + 1;
                        break;
                }

                // RELAXED overlap check for uniform sizes
                if (!overlapsRelaxed(newRoom, newWorldX, newWorldY, existingRoom)) {
                    // Place the room!
                    roomGrid[newGridX][newGridY] = newRoom;
                    roomWorldX[newGridX][newGridY] = newWorldX;
                    roomWorldY[newGridX][newGridY] = newWorldY;
                    rooms.add(newRoom);
                    return true;
                }
            }
        }
        return false;
    }

    // NEW: Prefer rooms with fewer neighbors (encourages branching)
    private Room pickRoomWithSpace() {
        List<Room> candidates = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();

        int totalWeight = 0;

        for (Room room : rooms) {
            int neighborCount = countNeighbors(room);
            int weight = Math.max(1, 5 - neighborCount); // Rooms with fewer neighbors get higher weight

            candidates.add(room);
            weights.add(weight);
            totalWeight += weight;
        }

        // Weighted random selection
        int randomWeight = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (int i = 0; i < candidates.size(); i++) {
            currentWeight += weights.get(i);
            if (randomWeight < currentWeight) {
                return candidates.get(i);
            }
        }

        return candidates.get(candidates.size() - 1); // Fallback
    }

    // NEW: Count how many neighbors a room has
    private int countNeighbors(Room room) {
        int count = 0;
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};

        for (int i = 0; i < 4; i++) {
            int nx = room.x + dx[i];
            int ny = room.y + dy[i];
            if (isValidGridPosition(nx, ny) && roomGrid[nx][ny] != null) {
                count++;
            }
        }
        return count;
    }

    // NEW: Skip positions with too many neighbors (prevents overcrowding)
    private boolean hasTooManyNeighbors(int gridX, int gridY) {
        int neighborCount = 0;
        int[] dx = {0, 1, 0, -1, 1, 1, -1, -1}; // Include diagonals
        int[] dy = {1, 0, -1, 0, 1, -1, 1, -1};

        for (int i = 0; i < 8; i++) {
            int nx = gridX + dx[i];
            int ny = gridY + dy[i];
            if (isValidGridPosition(nx, ny) && roomGrid[nx][ny] != null) {
                neighborCount++;
            }
        }

        return neighborCount > 3; // Allow max 3 neighbors (including the one we're attaching to)
    }

    // NEW: Relaxed overlap detection for uniform sizes
    private boolean overlapsRelaxed(Room newRoom, float newWorldX, float newWorldY, Room skipRoom) {
        float newEndX = newWorldX + newRoom.width;
        float newEndY = newWorldY + newRoom.height;

        for (Room room : rooms) {
            if (room == skipRoom) continue;

            float roomX = roomWorldX[room.x][room.y];
            float roomY = roomWorldY[room.x][room.y];
            float roomEndX = roomX + room.width;
            float roomEndY = roomY + room.height;

            // More lenient overlap check - allow closer proximity
            if (newWorldX < roomEndX + 2f && newEndX > roomX - 2f &&
                newWorldY < roomEndY + 2f && newEndY > roomY - 2f) {

                // Still prevent actual interior overlap
                if (newWorldX < roomEndX - 1f && newEndX > roomX + 1f &&
                    newWorldY < roomEndY - 1f && newEndY > roomY + 1f) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addDoorsToAllConnections() {
        // Clear existing doors
        for (Room room : rooms) {
            room.doors.clear();
        }

        // Check every grid cell for adjacent rooms
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                Room currentRoom = roomGrid[x][y];

                // Only proceed if there IS a room here
                if (currentRoom == null) continue;

                // Check neighbor to the EAST
                if (x + 1 < gridWidth) {
                    Room eastRoom = roomGrid[x + 1][y];
                    if (eastRoom != null) {
                        addDoorsBetweenRooms(currentRoom, eastRoom, Door.Direction.EAST);
                    }
                }

                // Check neighbor to the NORTH
                if (y + 1 < gridHeight) {
                    Room northRoom = roomGrid[x][y + 1];
                    if (northRoom != null) {
                        addDoorsBetweenRooms(currentRoom, northRoom, Door.Direction.NORTH);
                    }
                }
            }
        }

        ensureAllRoomsAccessible();
    }

    private void addDoorsBetweenRooms(Room room1, Room room2, Door.Direction direction) {

        float room1X = roomWorldX[room1.x][room1.y];
        float room1Y = roomWorldY[room1.x][room1.y];
        float room2X = roomWorldX[room2.x][room2.y];
        float room2Y = roomWorldY[room2.x][room2.y];

        switch (direction) {
            case EAST:
                // room1 is to the WEST of room2
                int startY = Math.max(0, (int) (room2Y - room1Y));
                int endY = Math.min(room1.height - 1, (int) (room2Y + room2.height - room1Y - 1));

                if (endY >= startY) {
                    int doorY = startY + (endY - startY) / 2;

                    Door door1 = new Door();
                    door1.dir = Door.Direction.EAST;
                    door1.position = doorY;
                    door1.connectedRoom = room2;
                    room1.doors.add(door1);

                    Door door2 = new Door();
                    door2.dir = Door.Direction.WEST;
                    door2.position = (int) (room1Y + doorY - room2Y);
                    door2.connectedRoom = room1;
                    room2.doors.add(door2);
                }
                break;

            case WEST:
                // room1 is to the EAST of room2
                startY = Math.max(0, (int) (room2Y - room1Y));
                endY = Math.min(room1.height - 1, (int) (room2Y + room2.height - room1Y - 1));

                if (endY >= startY) {
                    int doorY = startY + (endY - startY) / 2;

                    Door door1 = new Door();
                    door1.dir = Door.Direction.WEST;
                    door1.position = doorY;
                    door1.connectedRoom = room2;
                    room1.doors.add(door1);

                    Door door2 = new Door();
                    door2.dir = Door.Direction.EAST;
                    door2.position = (int) (room1Y + doorY - room2Y);
                    door2.connectedRoom = room1;
                    room2.doors.add(door2);
                }
                break;

            case NORTH:
                // room1 is to the SOUTH of room2
                int startX = Math.max(0, (int) (room2X - room1X));
                int endX = Math.min(room1.width - 1, (int) (room2X + room2.width - room1X - 1));  // FIXED: Use width, not height

                if (endX >= startX) {
                    int doorX = startX + (endX - startX) / 2;

                    Door door1 = new Door();
                    door1.dir = Door.Direction.NORTH;
                    door1.position = doorX;
                    door1.connectedRoom = room2;
                    room1.doors.add(door1);

                    Door door2 = new Door();
                    door2.dir = Door.Direction.SOUTH;
                    door2.position = (int) (room1X + doorX - room2X);
                    door2.connectedRoom = room1;
                    room2.doors.add(door2);
                }
                break;

            case SOUTH:
                // room1 is to the NORTH of room2
                startX = Math.max(0, (int) (room2X - room1X));
                endX = Math.min(room1.width - 1, (int) (room2X + room2.width - room1X - 1));

                if (endX >= startX) {
                    int doorX = startX + (endX - startX) / 2;

                    Door door1 = new Door();
                    door1.dir = Door.Direction.SOUTH;
                    door1.position = doorX;
                    door1.connectedRoom = room2;
                    room1.doors.add(door1);

                    Door door2 = new Door();
                    door2.dir = Door.Direction.NORTH;
                    door2.position = (int) (room1X + doorX - room2X);
                    door2.connectedRoom = room1;
                    room2.doors.add(door2);
                }
                break;
        }
    }

    private void ensureAllRoomsAccessible() {

        // Creamos un hashset para tener en cuenta todos las habitaciones visitados
        HashSet<Room> visited = new HashSet<>();

        // Empezamos con la primera habitacion y seguimos en orden
        Room startRoom = rooms.get(0);
        Queue<Room> queue = new LinkedList<>();
        queue.add(startRoom);
        visited.add(startRoom);

        while (!queue.isEmpty()) {
            Room current = queue.poll();

            // El bucle revisa todas las puertas/conexiones de la habitacion actual.
            for (Door door : current.doors) {
                Room connected = door.connectedRoom;
                if (connected != null && !visited.contains(connected)) {
                    visited.add(connected);
                    queue.add(connected);
                }
            }
        }

        // Si hay menos habitaciones visitadas de las que realmente existen, entonces
        // hay habitaciones que no tienen puerta y por tant no se han visitado.
        if (visited.size() < rooms.size()) {
            System.out.println("Warning: Some rooms are isolated! Adding additional doors...");

            // Find isolated rooms and connect them to the main group
            for (Room room : rooms) {
                if (!visited.contains(room)) {
                    // Find the nearest room in the main group
                    connectRoomToMainGroup(room, visited);
                }
            }
        }
    }

    private void connectRoomToMainGroup(Room isolatedRoom, java.util.Set<Room> mainGroup) {
        // Check all grid neighbors to find a connection to the main group
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
            Door.Direction[] dirs = {
            Door.Direction.NORTH,
            Door.Direction.EAST,
            Door.Direction.SOUTH,
            Door.Direction.WEST
        };

        for (int i = 0; i < 4; i++) {
            int checkX = isolatedRoom.x + dx[i];
            int checkY = isolatedRoom.y + dy[i];

            if (isValidGridPosition(checkX, checkY)) {
                Room neighbor = roomGrid[checkX][checkY];
                if (neighbor != null && mainGroup.contains(neighbor)) {
                    // Found a connection! Add doors between them
                    addDoorsBetweenRooms(isolatedRoom, neighbor, dirs[i]);
                    System.out.println("Added emergency door to connect isolated room");
                    return;
                }
            }
        }
    }



    private boolean isValidGridPosition(int x, int y) {
        return x >= 0 && x < gridWidth && y >= 0 && y < gridHeight;
    }

    // Get world position for a room (used by FirstScreen)
    public float getRoomWorldX(Room room) {
        return roomWorldX[room.x][room.y];
    }

    public float getRoomWorldY(Room room) {
        return roomWorldY[room.x][room.y];
    }
}

package io.github.azerisproyecte.levelGenerator;

// DungeonGenerator.java
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DungeonGenerator {
    private Random random;
    private int gridWidth, gridHeight;
    private Room[][] roomGrid;
    private List<Room> rooms;

    public DungeonGenerator(int gridWidth, int gridHeight) {
        this.random = new Random();
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.roomGrid = new Room[gridWidth][gridHeight];
        this.rooms = new ArrayList<>();
    }

    public List<Room> generateDungeon(int roomCount) {
        rooms.clear();

        // Start with first room in the center
        int startX = gridWidth / 2;
        int startY = gridHeight / 2;
        Room startRoom = createRandomRoom(startX, startY);
        roomGrid[startX][startY] = startRoom;
        rooms.add(startRoom);

        // Generate additional rooms
        for (int i = 1; i < roomCount; i++) {
            if (!addNewRoom()) {
                break; // Couldn't place more rooms
            }
        }

        // Connect rooms with corridors
        connectRooms();

        return rooms;
    }

    private Room createRandomRoom(int gridX, int gridY) {
        // Random room size between 5x5 and 10x10
        int roomWidth = random.nextInt(6) + 5;
        int roomHeight = random.nextInt(6) + 5;

        return new Room(gridX, gridY, roomWidth, roomHeight);
    }

    private boolean addNewRoom() {
        if (rooms.isEmpty()) return false;

        // Try to place a room adjacent to an existing room
        for (int attempts = 0; attempts < 100; attempts++) {
            Room existingRoom = rooms.get(random.nextInt(rooms.size()));
            Room.Door.Direction dir = Room.Door.Direction.values()[random.nextInt(4)];

            int newX = existingRoom.x;
            int newY = existingRoom.y;

            switch (dir) {
                case NORTH: newY++; break;
                case SOUTH: newY--; break;
                case EAST: newX++; break;
                case WEST: newX--; break;
            }

            // Check if position is valid and empty
            if (isValidGridPosition(newX, newY) && roomGrid[newX][newY] == null) {
                Room newRoom = createRandomRoom(newX, newY);
                roomGrid[newX][newY] = newRoom;
                rooms.add(newRoom);

                // Add doors between rooms
                addDoorsBetweenRooms(existingRoom, newRoom, dir);
                return true;
            }
        }
        return false;
    }

    private boolean isValidGridPosition(int x, int y) {
        return x >= 0 && x < gridWidth && y >= 0 && y < gridHeight;
    }

    private void addDoorsBetweenRooms(Room room1, Room room2, Room.Door.Direction dir) {
        // Simplified door placement - just mark positions
        // In a real implementation, you'd calculate exact positions
        Room.Door door1 = new Room.Door();
        Room.Door door2 = new Room.Door();

        // Set opposite directions
        door1.direction = dir;
        door2.direction = getOppositeDirection(dir);

        room1.doors.add(door1);
        room2.doors.add(door2);
    }

    private Room.Door.Direction getOppositeDirection(Room.Door.Direction dir) {
        switch (dir) {
            case NORTH: return Room.Door.Direction.SOUTH;
            case SOUTH: return Room.Door.Direction.NORTH;
            case EAST: return Room.Door.Direction.WEST;
            case WEST: return Room.Door.Direction.EAST;
        }
        return Room.Door.Direction.NORTH;
    }

    private void connectRooms() {
        // Simple connection - rooms already connected when placed
        // For more complex dungeons, add corridors here
    }
}

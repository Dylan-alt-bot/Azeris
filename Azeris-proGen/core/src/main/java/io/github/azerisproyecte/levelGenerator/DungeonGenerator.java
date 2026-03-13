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

    // Track actual world positions for each grid cell
    private float[][] roomWorldX;
    private float[][] roomWorldY;

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

        //clear the grid

        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                roomGrid[i][j] = null;
            }
        }

        // Start with first room at grid center, world position (0,0)
        int startX = gridWidth / 2;
        int startY = gridHeight / 2;

        Room startRoom = createRandomRoom(startX, startY);
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

        return rooms;
    }




    private boolean addNewRoom() {
        if (rooms.isEmpty()) return false;

        // Try multiple times to place a room
        for (int attempts = 0; attempts < 500; attempts++) {
            // Pick a random existing room to attach to
            Room existingRoom = rooms.get(random.nextInt(rooms.size()));

            // Pick a random direction
            Room.Door.Direction dir = Room.Door.Direction.values()[random.nextInt(4)];

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

                // Calculate world position based on the existing room's END position
                float newWorldX = 0;
                float newWorldY = 0;

                // Get the existing room's start position
                float existingStartX = roomWorldX[existingRoom.x][existingRoom.y];
                float existingStartY = roomWorldY[existingRoom.x][existingRoom.y];

                // Calculate where the existing room ENDS
                float existingEndX = existingStartX + existingRoom.width;
                float existingEndY = existingStartY + existingRoom.height;

                // Create a potential new room (we need its size to calculate position)
                Room newRoom = createRandomRoom(newGridX, newGridY);

                // Position the new room based on direction
                switch (dir) {
                    case EAST:
                        // New room starts where existing room ends ADDED OVERLAP -1
                        newWorldX = existingEndX -1;
                        newWorldY = existingStartY; // Align vertically
                        break;
                    case WEST:
                        // New room ends where existing room starts ADDED OVERLAP +1
                        newWorldX = existingStartX - newRoom.width + 1;
                        newWorldY = existingStartY;
                        break;
                    case NORTH:
                        // New room starts where existing room ends (vertically) ADDED OVERLAP -1
                        newWorldX = existingStartX;
                        newWorldY = existingEndY - 1;
                        break;
                    case SOUTH:
                        // New room ends where existing room starts (vertically) ADDED OVERLAP +1
                        newWorldX = existingStartX;
                        newWorldY = existingStartY - newRoom.height +1;
                        break;
                }

                // Check if this position overlaps with any existing room
                boolean overlaps = false;
                for (Room room : rooms) {
                    if (room == existingRoom) continue; // Skip the room we're attaching to

                    float roomX = roomWorldX[room.x][room.y];
                    float roomY = roomWorldY[room.x][room.y];
                    float roomEndX = roomX + room.width;
                    float roomEndY = roomY + room.height;

                    float newEndX = newWorldX + newRoom.width;
                    float newEndY = newWorldY + newRoom.height;

                    // Check for overlap (allowing them to touch but not intersect beyond the shared wall)
                    if (newWorldX < roomEndX - 0.1f && newEndX > roomX + 0.1f &&
                        newWorldY < roomEndY - 0.1f && newEndY > roomY + 0.1f) {
                        overlaps = true;
                        break;
                    }
                }

                if (!overlaps) {
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

    private void addDoorsBetweenRooms(Room room1, Room room2, Room.Door.Direction dir,
                                      float room1X, float room1Y,
                                      float room2X, float room2Y) {
        // Add door to room1
        Room.Door door1 = new Room.Door();
        door1.direction = dir;

        // Calculate door position based on where rooms meet
        switch (dir) {
            case EAST:
                // Door on room1's east wall
                door1.position = (int)(room2Y - room1Y + room2.height/2);
                break;
            case WEST:
                // Door on room1's west wall
                door1.position = (int)(room2Y - room1Y + room2.height/2);
                break;
            case NORTH:
                // Door on room1's north wall
                door1.position = (int)(room2X - room1X + room2.width/2);
                break;
            case SOUTH:
                // Door on room1's south wall
                door1.position = (int)(room2X - room1X + room2.width/2);
                break;
        }
        room1.doors.add(door1);

        // Add door to room2 (opposite direction)
        Room.Door door2 = new Room.Door();
        door2.direction = getOppositeDirection(dir);

        switch (getOppositeDirection(dir)) {
            case EAST:
                door2.position = (int)(room1Y - room2Y + room1.height/2);
                break;
            case WEST:
                door2.position = (int)(room1Y - room2Y + room1.height/2);
                break;
            case NORTH:
                door2.position = (int)(room1X - room2X + room1.width/2);
                break;
            case SOUTH:
                door2.position = (int)(room1X - room2X + room1.width/2);
                break;
        }
        room2.doors.add(door2);
    }

    private Room.Door.Direction getOppositeDirection(Room.Door.Direction dir) {
        switch (dir) {
            case NORTH: return Room.Door.Direction.SOUTH;
            case SOUTH: return Room.Door.Direction.NORTH;
            case EAST: return Room.Door.Direction.WEST;
            case WEST: return Room.Door.Direction.EAST;
            default: return dir;
        }
    }

    private boolean isValidGridPosition(int x, int y) {
        return x >= 0 && x < gridWidth && y >= 0 && y < gridHeight;
    }

    private Room createRandomRoom(int gridX, int gridY) {
        int roomWidth = random.nextInt(5) + 10;
        int roomHeight = random.nextInt(5) + 10;
        return new Room(gridX, gridY, roomWidth, roomHeight);
    }

    // Get world position for a room (used by FirstScreen)
    public float getRoomWorldX(Room room) {
        return roomWorldX[room.x][room.y];
    }

    public float getRoomWorldY(Room room) {
        return roomWorldY[room.x][room.y];
    }
}

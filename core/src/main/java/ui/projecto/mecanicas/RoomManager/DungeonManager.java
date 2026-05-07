package ui.projecto.mecanicas.RoomManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DungeonManager {
    private final List<RoomData> dungeonRooms;
    private int currentRoomIndex;

    public DungeonManager() {
        dungeonRooms = new ArrayList<>();
        generateDungeon();
    }

    private void generateDungeon() {
        List<RoomData> normalRooms = new ArrayList<>();

        normalRooms.add(new RoomData("maps/mapas/Mapa1.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa2.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa3.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa4.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa5.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa6.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa7.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa8.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa9.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa10.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa11.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa12.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa26.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa28.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa29.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa30.tmx", false));

        RoomData bossRoom = new RoomData("maps/beta/finalboss.tmx", true);

        Collections.shuffle(normalRooms);
        Random random = new Random();
        int roomCount = random.nextInt(4) + 3;
        for (int i = 0; i < roomCount; i++) {
            dungeonRooms.add(normalRooms.get(i));
        }

        dungeonRooms.add(bossRoom);
        currentRoomIndex = 0;
        System.out.println("[DUNGEON] Habitaciones generadas: " + dungeonRooms.size());
    }

    public RoomData getCurrentRoom() {
        return dungeonRooms.get(currentRoomIndex);
    }

    public boolean nextRoom() {
        if (currentRoomIndex + 1 >= dungeonRooms.size()) {
            return false;
        }
        currentRoomIndex++;
        return true;
    }

    public boolean isLastRoom() {
        return currentRoomIndex == dungeonRooms.size() - 1;
    }

    public int getCurrentRoomIndex() {
        return currentRoomIndex;
    }
}

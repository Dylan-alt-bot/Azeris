package ui.projecto.mecanicas.RoomManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DungeonManager {
    private final List<RoomData> normalRooms;
    private final RoomData bossRoom;
    private RoomData currentRoom;
    private boolean onBoss = false;
    private int currentRoomIndex = 0;
    private final Random random = new Random();

    public DungeonManager() {
        normalRooms = new ArrayList<>();
        loadAllRooms();
        bossRoom = new RoomData("maps/beta/finalboss.tmx", true);
        currentRoom = normalRooms.get(random.nextInt(normalRooms.size()));
        System.out.println("[DUNGEON] Dungeon iniciado con " + normalRooms.size() + " salas disponibles");
    }

    private void loadAllRooms() {
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
        normalRooms.add(new RoomData("maps/mapas/Mapa13.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa14.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa24.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa25.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa26.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa28.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa29.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa30.tmx", false));
    }

    public RoomData getCurrentRoom() {
        return onBoss ? bossRoom : currentRoom;
    }

    public boolean nextRoom() {
        if (onBoss) return false;
        RoomData previous = currentRoom;
        if (normalRooms.size() > 1) {
            RoomData next;
            do {
                next = normalRooms.get(random.nextInt(normalRooms.size()));
            } while (next.getMapPath().equals(previous.getMapPath()));
            currentRoom = next;
        }
        currentRoomIndex++;
        return true;
    }

    public void jumpToBoss() {
        onBoss = true;
        System.out.println("[DUNGEON] Saltando al boss");
    }

    public boolean isLastRoom() {
        return onBoss;
    }
}

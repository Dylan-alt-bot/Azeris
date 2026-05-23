package ui.projecto.mecanicas.RoomManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DungeonManager {
    private final List<RoomData> normalRooms;
    private final List<RoomData> availableRooms;
    private final RoomData bossRoom;
    private RoomData currentRoom;
    private boolean onBoss = false;
    private int currentRoomIndex = 0;
    private final Random random = new Random();

    public DungeonManager() {
        normalRooms = new ArrayList<>();
        availableRooms = new ArrayList<>();
        loadAllRooms();
        bossRoom = new RoomData("maps/mapas/BossFinal.tmx", true);
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
        normalRooms.add(new RoomData("maps/mapas/Mapa15.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa16.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa17.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa18.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa19.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa20.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa21.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa22.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa23.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa24.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa25.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa26.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa27.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa28.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa29.tmx", false));
        normalRooms.add(new RoomData("maps/mapas/Mapa30.tmx", false));
    }

    private void reshuffleRooms() {
        availableRooms.clear();
        availableRooms.addAll(normalRooms);
        Collections.shuffle(availableRooms);
        System.out.println("[DUNGEON] Salas mezcladas nuevamente");
    }

    public RoomData getCurrentRoom() {
        return onBoss ? bossRoom : currentRoom;
    }

    public boolean nextRoom() {
        if (onBoss) return false;
        if (availableRooms.isEmpty()) {
            reshuffleRooms();
        }
        currentRoom = availableRooms.remove(0);
        currentRoomIndex++;
        System.out.println("[DUNGEON] Nueva sala: " + currentRoom.getMapPath());
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

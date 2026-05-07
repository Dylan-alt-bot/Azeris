package ui.projecto.mecanicas.RoomManager;

public class RoomData {
    private final String mapPath;
    private final boolean bossRoom;

    public RoomData(String mapPath, boolean bossRoom) {
        this.mapPath = mapPath;
        this.bossRoom = bossRoom;
    }

    public String getMapPath() {
        return mapPath;
    }

    public boolean isBossRoom() {
        return bossRoom;
    }
}

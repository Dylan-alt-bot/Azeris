package ui.projecto.mecanicas;

public class Vida {
    private int vidaActual;
    private int vidaMaxima;
    private boolean recibiendoDanio;
    private boolean muerto;

    public Vida(int vidaMaxima) {
        this.vidaMaxima = vidaMaxima;
        this.vidaActual = vidaMaxima;
        this.recibiendoDanio = false;
        this.muerto = false;
    }

    public void recibirDolor(int cantidad) {
        if (!muerto && cantidad > 0) {
            vidaActual = Math.max(0, vidaActual - cantidad);
            recibiendoDanio = true;

            if (vidaActual <= 0) {
                morir();
            }
        }
    }

    public void curar(int cantidad) {
        if (!muerto && cantidad > 0) {
            vidaActual = Math.min(vidaMaxima, vidaActual + cantidad);
        }
    }

    public void revivir() {
        if (muerto) {
            muerto = false;
            vidaActual = vidaMaxima;
            recibiendoDanio = false;
        }
    }

    private void morir() {
        muerto = true;
        recibiendoDanio = false;
    }

    public void resetRecibiendoDolor() {
        recibiendoDanio = false;
    }

    public int getVidaActual() {
        return vidaActual;
    }
    public int getVidaMaxima() {
        return vidaMaxima;
    }
    public boolean isRecibiendoDanio() {
        return recibiendoDanio;
    }
    public boolean isMuerto() {
        return muerto;
    }
    public float getPorcentajeVida() {
        return (float) vidaActual / vidaMaxima;
    }
}

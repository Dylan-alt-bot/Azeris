package ui.projecto.mecanicas;

public class Vida {
    private int vidaActual;
    private int vidaMaxima;
    private boolean recibirDolor;
    private boolean muerto;

    public Vida(int vidaMaxima) {
        this.vidaMaxima = vidaMaxima;
        this.vidaActual = vidaMaxima;
        this.recibirDolor = false;
        this.muerto = false;
    }

    public void recibirDolor(int cantidad) {
        if (!muerto && cantidad > 0) {
            vidaActual = Math.max(0, vidaActual - cantidad);
            recibirDolor = true;

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
        muerto = false;
        vidaActual = vidaMaxima;
        recibirDolor = false;
    }

    private void morir() {
        muerto = true;
        recibirDolor = false;
    }
    public boolean isMuerto() {
        return muerto;
    }

    public float getPorcentajeVida() {
        return (float) vidaActual / vidaMaxima;
    }

    public int getMaxVida(){
        return vidaMaxima;
    }
    public void setMaxVida(int vida){
        this.vidaMaxima = vida;
    }
}

package com.example.rssiposition;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * CLasse para abstrair AP e concentrar funcionalidades que serão utilizadas para estimar posição
 */
public class AP {
    private final int RSSI_PLACEHOLDER = 1;

    private String name,mac;
    private float alpha,A;
    private Vector3D position;
    private int lastRssiMeasured;

    /**
     * Construtor da classe
     * @param _name nome dado ao AP
     * @param _mac endereço mac "BSSID" do AP
     * @param _x Coodenada x da posição do AP
     * @param _y Coodenada y da posição do AP
     * @param _z Coodenada z da posição do AP
     * @param _alpha Coeficiente de atenuação de sinal calculado para o AP, conforme o modelo de propagação de sinal
     * @param _A Parametro linear calculado para o AP, conforme o modelo de propagação de sinal
     */
    public AP(String _name, String _mac, float _x, float _y, float _z, float _alpha, float _A){
        name = _name;
        mac = _mac;
        position = new Vector3D(_x,_y,_z);
        alpha = Math.abs(_alpha);
        A = Math.abs(_A);
        lastRssiMeasured = RSSI_PLACEHOLDER;
    }

    /**
     * Utiliza o {@code rssi} infromado para calcular uma distancia conforme os valores númericos para os parametros do modelo de propagação de sinal
     * @param rssi valor do rssi (deve ser um inteiro negativo)
     * @return retorna 10^(- (A + rssi)/(10*alpha))
     */
    public float rssiToDistance(int rssi){
        return (float) Math.pow(10.0, -(A + rssi)/(10.0*alpha) );
    }

    /**
     * Chama {@code rssiToDistance} com rssi sendo {@link #lastRssiMeasured}
     * @return retorna 10^(- (A + {@link #lastRssiMeasured})/(10*alpha))
     */
    public float rssiToDistance(){
        return rssiToDistance(lastRssiMeasured);
    }

    /**
     * Define o valor de {@link #lastRssiMeasured}
     * @param lastRssiMeasured ultimo valor de rssi obtido através do scan wifi
     */
    public void setRssi(int lastRssiMeasured) {
        this.lastRssiMeasured = lastRssiMeasured;
    }


    /**
     * reset valor de rssi para o valor inicial {@link #RSSI_PLACEHOLDER}
     */
    public void resetRssi(){
        setRssi(RSSI_PLACEHOLDER);
    }

    public String getMac() {
        return mac;
    }

    public Vector3D getPosition() {
        return position;
    }

    public boolean hasMeasure(){
        return lastRssiMeasured != RSSI_PLACEHOLDER;
    }

    @Override
    public String toString() {
        return "AP {\n\tName = " + name + "\n\t Mac = " + mac + "\n\tPosition= " + position.toString() + "\n\tparameters= {" + alpha +"; " + A + "} \n}";
    }
}

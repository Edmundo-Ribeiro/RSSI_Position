package com.example.rssiposition;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class AP {
    private final int RSSI_PLACEHOLDER = 1;

    private String name,mac;
    private float alpha,A;
    private Vector3D position;
    private int lastRssiMeasured;
    public AP(String _name, String _mac, float _x, float _y, float _z, float _alpha, float _A){
        name = _name;
        mac = _mac;
        position = new Vector3D(_x,_y,_z);
        alpha = Math.abs(_alpha);
        A = Math.abs(_A);
        lastRssiMeasured = RSSI_PLACEHOLDER;
    }

    public float rssiToDistance(int rssi){
        return (float) Math.pow(10.0, -(A + rssi)/(10.0*alpha) );
    }

    public float rssiToDistance(){
        return rssiToDistance(lastRssiMeasured);
    }

    public void setRssi(int lastRssiMeasured) {
        this.lastRssiMeasured = lastRssiMeasured;
    }

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

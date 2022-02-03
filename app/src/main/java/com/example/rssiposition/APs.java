package com.example.rssiposition;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class APs extends HashMap<String,AP>{
    public APs(){
        super();
    }
    public APs(AP[] apArray){
        super();
        for(AP ap : apArray){
            this.put(ap.getMac(),ap);
        }
    }

    public int countAvailableAPs(){
        AtomicInteger count = new AtomicInteger();
        this.forEach((mac,ap)-> count.addAndGet(ap.hasMeasure() ? 1 : 0));
        return count.get();
    }

    public APs getAvailableAPs(){
        APs subMap = new APs();
        this.forEach((mac,ap)-> {
            if(ap.hasMeasure()) {
                subMap.put(mac, ap);
            }
        });
        return subMap;
    }

    public Vector3D getWeightedAveragePosition(){
        APs subset = this.getAvailableAPs();

        Vector3D wavPosition = new Vector3D(0f,0f,0f);
        float weightSum = 0f;

        for(AP ap : subset.values()){
            float weight = 1/ap.rssiToDistance();
            wavPosition.add(ap.getPosition().scalarMultiply(weight));
            weightSum += weight;
        }

        return wavPosition.scalarMultiply(1/ weightSum);
    }

    public void resetAps() {
        this.forEach((mac,ap)-> ap.resetRssi());
    }
}

package com.example.rssiposition;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Classe para simplificar manipulações com varios APs e acomodar todas as funções que precisam utilizar um número indefinido de APs simultanamente
 */
public class APs extends HashMap<String,AP>{

    /**
     * Contruto vazio
     */
    public APs(){
        super();
    }

    /**
     * Construtor alternativo, cria dicionário hashmap com funcionalidades especificas
     * @param apArray vetor com os AP instanciados
     */
    public APs(AP[] apArray){
        super();
        for(AP ap : apArray){
            this.put(ap.getMac(),ap);
        }
    }

    /**
     * Conta quantos Aps tem o valor de rssi diferente do {@code #RSSI_PLACEHOLDER}
     * @return número total de Aps que o valor do rssi não está setado como {@code #RSSI_PLACEHOLDER}
     */
    public int countAvailableAPs(){
        AtomicInteger count = new AtomicInteger();
        this.forEach((mac,ap)-> count.addAndGet(ap.hasMeasure() ? 1 : 0));
        return count.get();
    }

    /**
     * Filtra os conjunto de Aps atual e retorna um subconjunto contendo apenas os aps em que o rssi foi setado como sendo diferente de {@code #RSSI_PLACEHOLDER}
     * @return subconjunto do mapa original
     */
    public APs getAvailableAPs(){
        APs subMap = new APs();
        this.forEach((mac,ap)-> {
            if(ap.hasMeasure()) {
                subMap.put(mac, ap);
            }
        });
        return subMap;
    }

    /**
     * Seta o rssi dos aps para o valor de  {@code #RSSI_PLACEHOLDER}
     */
    public void resetAps() {
        this.forEach((mac,ap)-> ap.resetRssi());
    }
}

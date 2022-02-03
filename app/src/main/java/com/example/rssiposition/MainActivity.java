package com.example.rssiposition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {


    WifiManager wifiManager; //disponibilizar funcionalidades de rede
    BroadcastReceiver wifiScanReceiver; //receber retorno de do broadcast de busca por redes wifi


    //objetos necessários para repetir as medidas periodicamente
    private Runnable mRunnable;
    private final Handler mHandler = new Handler();

    //tempo entre escaneamentos
    private final int timeInterval = 5000;//ms
    private final int tickInterval = 500;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instanciando o wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //Se o wifi estiver inativo, ativa-lo

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi está inativo... ativando ele agora!", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }


        /**
         * criar o position estimator
         * apos preencher os rssi nos roteadores chamar:
         *  getEstimation(APs aps)
         *  limpar aps;
         */
    }
}
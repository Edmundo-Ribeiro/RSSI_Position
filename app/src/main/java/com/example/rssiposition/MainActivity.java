package com.example.rssiposition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;


public class MainActivity extends AppCompatActivity {

    TextView textCountDown; //tempo para o proximo scan
    ToggleButton btnStartStop; // botao para iniciar e parar coleta
    TextView textPosition; //posição estimada

    static AP[] registeredAps =  new AP[]{
            new AP("roteador_vivo_5GHz"  ,"f4:54:20:5d:4c:3h" ,0.240, 1.160, 1.050, 2.49819278, 28.34613884),
            new AP("roteador_vivo_2GHz","f4:54:20:5d:4c:3f"  ,0.240, 1.160, 1.050, 2.53053740, 32.50116084),
            new AP("roteador_celular", "82:7e:68:19:b6:fe"     ,5.469, 0.250, 0.450, 4.74093970, 26.02048906),
            new AP("roteador_oi" , "c8:5a:9f:e8:e2:c7"         ,1.000, 4.739, 0.450, 3.29406819, 35.49286952)
    };
    APs apMap = new APs(registeredAps);

    WifiManager wifiManager; //disponibilizar funcionalidades de rede
    WifiReceiver wifiScanReceiver; //receber retorno de do broadcast de busca por redes wifi


    //objetos necessários para repetir as medidas periodicamente
    private Runnable mRunnable;
    private final Handler mHandler = new Handler();

    //tempo entre escaneamentos
    private final int timeInterval = 8000;//ms
    private final int tickInterval = 500;


    MutableLiveData<Vector3D> uiLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textPosition = (TextView) findViewById(R.id.textPosition);
        btnStartStop = (ToggleButton) findViewById(R.id.toggleButton3);
        btnStartStop.setChecked(false);
        btnStartStop.setTextOff("Start Scan");
        btnStartStop.setTextOn("Stop Scan");


        final Handler mHandler = new Handler();

        //Bloco de codigo para repetir periodicamente o escaneamento
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("Debug","Running");
                boolean status = wifiScanReceiver.startScan();
                Log.d("Debug","Status: "+ String.valueOf(status));
                if(status)
                    Toast.makeText(getApplicationContext(), "Started WiFi broadcast", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Error Trying to Start WiFi broadcast", Toast.LENGTH_SHORT).show();
//                timer.start();

                mHandler.postDelayed(mRunnable, timeInterval);
            }
        };

        btnStartStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("Debug","Toggle: " + String.valueOf(isChecked));
                if (isChecked) {
                    mHandler.post(mRunnable); // iniciar repetição de escaneamentos,

                } else {
                    mHandler.removeCallbacks(mRunnable);
                }
            }
        });
        //Instanciando o wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //Se o wifi estiver inativo, ativa-lo

        wifiScanReceiver = new WifiReceiver(wifiManager,this.getApplicationContext(),apMap);
        uiLink = new MutableLiveData<Vector3D>();

        uiLink.observe(this, new Observer<Vector3D>() {
            @Override
            public void onChanged(Vector3D vector3D) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String p = uiLink.getValue().toString();
                        Log.d("Estimation", p);
                        textPosition.setText(p);
                    }
                });
            }
        });



        wifiScanReceiver.setUiLink(uiLink);


        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi está inativo... ativando ele agora!", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }



    }

    @Override
    protected void onPause() {
        super.onPause();
        wifiScanReceiver.stopScan();
        mHandler.removeCallbacks(mRunnable);
        btnStartStop.setChecked(false);
    }
}
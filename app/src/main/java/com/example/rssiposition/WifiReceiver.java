package com.example.rssiposition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.util.List;

public class WifiReceiver extends BroadcastReceiver {


    WifiManager manager;
    Context context;
    public WifiReceiver (WifiManager _manager, Context _context){
        super();
        manager = _manager;
        context = _context;
    }

    public boolean startScan(){
        if(!this.isOrderedBroadcast()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            context.registerReceiver(this, intentFilter); //registrar pedido para receber escaneamento wifi
        }
        return manager.startScan();
    }

    public void stopScan(){
        if(this.isOrderedBroadcast()) {
            context.unregisterReceiver(this);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
        if(success){
            List<ScanResult> scanResults = manager.getScanResults();
            /**
             * para cada resultado em scanresult
             * setar o ap correpondente com esse resultado
             * caso não seja de um ap cadastrado igrnorar
             * quando finalizar de setar todos
             * estrair aps que foram inicializados
             * obter estimativa de posição
             * fazer clear
             */

        }
        else {
            // Mostrar que algo deu errado
            Toast.makeText(context, "Error receiving WiFi broadcast", Toast.LENGTH_SHORT).show();
        }

    }
}

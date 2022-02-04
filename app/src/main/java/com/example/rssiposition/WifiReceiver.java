package com.example.rssiposition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;



public class WifiReceiver extends BroadcastReceiver {
    static APs apMap;
    static PositionEstimator estimator = new PositionEstimator();
    Vector3D estimation;
    WifiManager manager;
    Context context;
    MutableLiveData<Vector3D> uiLink = new MutableLiveData<Vector3D>();
    public WifiReceiver (WifiManager _manager, Context _context, APs _apMap){
        super();
        manager = _manager;
        context = _context;
        apMap = _apMap;
    }

    public void setUiLink(MutableLiveData<Vector3D> uiLink) {
        this.uiLink = uiLink;
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
            for(ScanResult scanResult: scanResults){
                Log.w("debug","ScanResult: "+scanResult.SSID + " -> "+scanResult.BSSID);


                String bssid = scanResult.BSSID;
                int rssi = scanResult.level;
                if(apMap.containsKey(bssid))
                    apMap.get(bssid).setRssi(rssi);
            }
            APs availableAps = apMap.getAvailableAPs();
            if (availableAps.size() > 3){
                estimation = estimator.getEstimation(availableAps);
                uiLink.postValue(estimation);
            }
            else{
                Log.d("Debug","It is no possible to get an estimation with "+String.valueOf(availableAps.size()) + " APs.");
                Toast.makeText(context, "Not enough APs available: 4 needed " + String.valueOf(availableAps.size()) +" provided", Toast.LENGTH_SHORT).show();
            }
            apMap.resetAps();

        }
        else {
            // Mostrar que algo deu errado
            Toast.makeText(context, "Error receiving WiFi broadcast", Toast.LENGTH_SHORT).show();
        }

    }


    public Vector3D getEstimation() {
        return estimation;
    }
}

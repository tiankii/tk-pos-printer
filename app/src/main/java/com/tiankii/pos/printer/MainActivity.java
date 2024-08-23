package com.tiankii.pos.printer;

import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.appcompat.app.AppCompatActivity;
import net.nyx.printerservice.print.IPrinterService;
import net.nyx.printerservice.print.PrintTextFormat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private IPrinterService printerService;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private boolean isPrinterServiceBound = false;
    private String[] pendingPrintData = null;
    private ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            printerService = null;
            isPrinterServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            printerService = IPrinterService.Stub.asInterface(service);
            isPrinterServiceBound = true;
            if (pendingPrintData != null) {
                dispatchPrintRequest(pendingPrintData);
                pendingPrintData = null;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService();
        handleSendText(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSendText(intent);
    }

   private void handleSendText(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri data = intent.getData();
            if (data != null) {
                String[] printData = extractPayData(data);
                if (isPrinterServiceBound) {
                    dispatchPrintRequest(printData);
                } else {
                    pendingPrintData = printData;
                }
                finish();
            }
        }
    }



    private String[] extractPayData(Uri data) {
        String storeName = data.getQueryParameter("storeName");
        String appName = data.getQueryParameter("appName");
        String invoiceId = data.getQueryParameter("invoiceId");
        String total = data.getQueryParameter("total");
        String date = data.getQueryParameter("date");
        String rate = data.getQueryParameter("rate");

        return new String[]{"pay", storeName, appName, invoiceId, date,total, rate};
    }

    private void dispatchPrintRequest(String[] printData) {
        printPayReceipt(printData[1], printData[2], printData[3], printData[4], printData[5], printData[6]);
    }

    private void printPayReceipt(String storeName, String appName, String invoiceId, String date, String total, String rate) {
        singleThreadExecutor.submit(() -> {
            try {
                PrintTextFormat dashedFormat = new PrintTextFormat();
                dashedFormat.setStyle(0);
                dashedFormat.setTextSize(27);
                dashedFormat.setAli(1);
                dashedFormat.setStyle(1);

                Bitmap originalBitmap = BitmapFactory.decodeStream(getAssets().open("header.png"));
                int maxWidthPixels = 360;
                double aspectRatio = (double) originalBitmap.getWidth() / originalBitmap.getHeight();
                int newHeight = (int) (maxWidthPixels / aspectRatio);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, maxWidthPixels, newHeight, true);
                printerService.printBitmap(resizedBitmap, 1, 1);

                //dashed line
                String dashedLine = new String(new char[32]).replace("\0", "-");

                // printerService.printText(dashedLine, dashedFormat);

                PrintTextFormat storeFormat = new PrintTextFormat();
                storeFormat.setAli(1);
                storeFormat.setTextSize(32);
                storeFormat.setStyle(1);
                storeFormat.setTopPadding(10);

                PrintTextFormat appFormat = new PrintTextFormat();
                appFormat.setAli(1);
                appFormat.setTextSize(27);
                // appFormat.setStyle(1);

                printerService.printText(storeName, storeFormat);
                printerService.printText(appName, appFormat);
                printDynamicKeyValue("Inv No:"," ", invoiceId);
                printDynamicKeyValue("Date:","   ", date);
                printerService.printText(dashedLine, dashedFormat);
                printDynamicKeyValue("Total:","  ", total);
                printDynamicKeyValue("Rate:","   ", rate);

                PrintTextFormat fooderFormat = new PrintTextFormat();
                fooderFormat.setAli(1);
                fooderFormat.setTextSize(20);
                fooderFormat.setTopPadding(20);
                printerService.printText("Powered and Secured by", fooderFormat);
                Bitmap originalBitmap2 = BitmapFactory.decodeStream(getAssets().open("footer.png"));
                int maxWidthPixels2 = 180;
                double aspectRatio2 = (double) originalBitmap2.getWidth() / originalBitmap2.getHeight();
                int newHeight2 = (int) (maxWidthPixels2 / aspectRatio2);
                Bitmap resizedBitmap2 = Bitmap.createScaledBitmap(originalBitmap2, maxWidthPixels2, newHeight2, true);
                printerService.printBitmap(resizedBitmap2, 1, 1);
                // printerService.printText("", appFormat);
           
                paperOut();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void printDynamicKeyValue(String key, String space ,String value) throws RemoteException {
        PrintTextFormat textFormat = new PrintTextFormat();
        textFormat.setStyle(0);
        textFormat.setTextSize(23);
        textFormat.setStyle(1);
        printerService.printText(key + space + value , textFormat);
    }


    //bind service-------------------------------------------------
    private void bindService() {
        Intent intent = new Intent();
        intent.setPackage("net.nyx.printerservice");
        intent.setAction("net.nyx.printerservice.IPrinterService");
        bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    private void paperOut() {
        singleThreadExecutor.submit(() -> {
            try {
                printerService.paperOut(80);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPrinterServiceBound) {
            unbindService(connService);
            isPrinterServiceBound = false;
        }
    }
}

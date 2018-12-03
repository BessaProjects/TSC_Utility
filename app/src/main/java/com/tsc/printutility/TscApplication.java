package com.tsc.printutility;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class TscApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}

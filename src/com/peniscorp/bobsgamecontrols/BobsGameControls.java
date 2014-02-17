package com.peniscorp.bobsgamecontrols;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class BobsGameControls implements IXposedHookLoadPackage {

	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        try {
        	if ( !lpparam.packageName.equals( "com.bobsgame.bg" ) ) return;

        	findAndHookMethod( "com.badlogic.gdx.controllers.android.AndroidController", lpparam.classLoader, "getName", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable
                {
                	param.setResult( "OUYA Game Controller" );
                }
        	});
	    } catch (Throwable e) {
	    	XposedBridge.log(e);
	    }		
	}
}
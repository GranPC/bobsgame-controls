package com.peniscorp.bobsgamecontrols;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class BobsGameControls implements IXposedHookLoadPackage
{
	private class Button
	{
		public int x;
		public int y;
		public int w;
		public int h;
		
		public String icon;
		public String field; // field in ControlsManager to set
		
		public Object renderer; 
	}
	
	public Button buttons[];
	public static String pkg = "com.bobsgame.bg";

	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable
	{
        try
        {
        	if ( !lpparam.packageName.equals( pkg ) ) return;
        	
        	buttons = new Button[ 6 ];
        	
        	int i = 0;
        	
        	{
        		Button tmp = new Button();
        		tmp.x = 155;
        		tmp.y = 390;
        		tmp.w = tmp.h = 100;

        		tmp.icon = "up";
        		tmp.field = "BUTTON_UP_HELD";
        		buttons[ i++ ] = tmp;
        	}
        	
        	{
        		Button tmp = new Button();
        		tmp.x = 155;
        		tmp.y = 590;
        		tmp.w = tmp.h = 100;

        		tmp.icon = "down";
        		tmp.field = "BUTTON_DOWN_HELD";
        		buttons[ i++ ] = tmp;
        	}

        	{
        		Button tmp = new Button();
        		tmp.x = 55;
        		tmp.y = 490;
        		tmp.w = tmp.h = 100;

        		tmp.icon = "left";
        		tmp.field = "BUTTON_LEFT_HELD";
        		buttons[ i++ ] = tmp;
        	}
        	
        	{
        		Button tmp = new Button();
        		tmp.x = 255;
        		tmp.y = 490;
        		tmp.w = tmp.h = 100;

        		tmp.icon = "right";
        		tmp.field = "BUTTON_RIGHT_HELD";
        		buttons[ i++ ] = tmp;
        	}
        	
        	{
        		Button tmp = new Button();
        		tmp.x = 1280 - 255 - 100;
        		tmp.y = 490;
        		tmp.w = tmp.h = 100;

        		tmp.icon = "a";
        		tmp.field = "BUTTON_LSHIFT_HELD";
        		buttons[ i++ ] = tmp;
        	}
        	
        	{
        		Button tmp = new Button();
        		tmp.x = 1280 - 55 - 150;
        		tmp.y = 490;
        		tmp.w = tmp.h = 100;

        		tmp.icon = "b";
        		tmp.field = "BUTTON_SPACE_HELD";
        		buttons[ i++ ] = tmp;
        	}

        	findAndHookMethod( "com.badlogic.gdx.controllers.android.AndroidController", lpparam.classLoader, "getName", new XC_MethodHook()
        	{
                @Override
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable
                {
                	param.setResult( "OUYA Game Controller" );
                }
        	} );
        	
        	findAndHookMethod( "com.bobsgame.bg.components.GameLogic", lpparam.classLoader, "waitForPressStart", new XC_MethodHook()
        	{
                @Override
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable
                {
                	Object caption = XposedHelpers.getObjectField( param.thisObject, "pressStartCaption" );
                	XposedHelpers.callMethod( caption, "replaceText", "TAP TO BEGIN" );
                }
        	} );
        	
        	findAndHookMethod( "com.bobsgame.bg.engine.ControlsManager", lpparam.classLoader, "update", new XC_MethodHook()
        	{
                @Override
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable
                {
                	boolean bTapped = false;
                	
                	try
                	{
                		Object Input = XposedHelpers.getStaticObjectField( XposedHelpers.findClass( "com.badlogic.gdx.Gdx", lpparam.classLoader ), "input" );

                		bTapped = ( Boolean ) XposedHelpers.callMethod( Input, "isButtonPressed", 0 );
                		XposedHelpers.setObjectField( param.thisObject, "BUTTON_SPACE_PRESSED", bTapped );
                		
                		for ( int i = 0; i < 4; i++ )
                		{
                			if ( ( Boolean ) XposedHelpers.callMethod( Input, "isTouched", i ) )
                			{
                				int x = ( Integer ) XposedHelpers.callMethod( Input, "getX", i );
                				int y = ( Integer ) XposedHelpers.callMethod( Input, "getY", i );
                				
                				for ( int j = 0; j < 6; j++ )
                				{
                					if ( buttons[ j ] != null )
                					{
                						Button butt = buttons[ j ];
                						if ( x > butt.x && x < butt.x + butt.w && y > butt.y && y < butt.y + butt.h )
                						{
                							XposedHelpers.setObjectField( param.thisObject, butt.field, true );
                						}
                					}
                				}
                			}
                		}
                	}
                	catch ( Throwable e )
                	{
                		XposedBridge.log( e );
                	}
                }
        	} );
        	
        	findAndHookMethod( "com.bobsgame.bg.components.GameLogic", lpparam.classLoader, "renderForeground", new XC_MethodHook()
        	{
                @Override
                protected void afterHookedMethod( MethodHookParam param ) throws Throwable
                {
                	Class GLUtils = XposedHelpers.findClass( "com.bobsgame.bg.GLUtils", lpparam.classLoader );
                	
    				for ( int i = 0; i < 6; i++ )
    				{
    					if ( buttons[ i ] != null )
    					{
    						Button butt = buttons[ i ];
    						
    						XposedHelpers.callStaticMethod( GLUtils, "drawFilledRectXYWH", butt.x, butt.y, butt.w, butt.h, 1, 0, 0, (float) 1 );
    						
    						if ( butt.renderer == null )
    						{
    							Object font = XposedHelpers.getStaticObjectField( XposedHelpers.findClass( "com.bobsgame.bg.engine.BobFont", lpparam.classLoader ), "font_32" );
    							Object color = XposedHelpers.getStaticObjectField( XposedHelpers.findClass( "com.bobsgame.bg.engine.BobColor", lpparam.classLoader ), "GREEN" );
    							Object bgcolor = XposedHelpers.getStaticObjectField( XposedHelpers.findClass( "com.bobsgame.bg.engine.BobColor", lpparam.classLoader ), "CLEAR" );
    							
    							Object caption = XposedHelpers.callMethod( XposedHelpers.callMethod( param.thisObject, "CaptionManager" ), "newManagedCaption", 
    									0, 0, -1, butt.icon.toUpperCase(), font, color, bgcolor, 1.0F, 0 );
    							
    							int w = XposedHelpers.getIntField( caption, "width" );
    							int h = XposedHelpers.getIntField( caption, "height" );
    							
    							XposedHelpers.setIntField( caption, "screenX", butt.x + ( butt.w / 2 - w / 2 ) );
    							XposedHelpers.setIntField( caption, "screenY", butt.y + ( butt.h / 2 - h / 2 ) );
    							
    							butt.renderer = caption;
    						}
    					}
        				else
        				{
        					Log.w( "bobsgame", "Missing button " + i + "!" );
        				}
    				}
                }
        	} );
	    }
        catch ( Throwable e )
        {
	    	XposedBridge.log( e );
	    }
	}
}

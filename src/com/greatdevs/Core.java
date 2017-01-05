package com.greatdevs;

import java.io.IOException;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import com.greatdevs.util.DisplayManager;
import com.greatdevs.renderer.BitmapFont;
import com.greatdevs.renderer.Renderer;
import com.greatdevs.screens.*;

public class Core {
	public static String FPSinfo = "updates: 0, fps: 0";
	
	public static Renderer renderer = new Renderer();
	public static BitmapFont font;
	
	private static Screen curScreen;
	
	public static void main(String[] args) {
		Core core = new Core();
		core.init("Sandbox");
	}
	
	public void init(String title) {
		DisplayManager.createWindow(title);
		font = new BitmapFont("font/font_small", "font/font_small");
		renderer.init();
		setScreen(new MainMenuScreen());
		run();
	}
	
	public static void setScreen(Screen screen){
		if (curScreen != null) curScreen.clear();
		curScreen = screen;
		curScreen.init();
	}
	
	private void run() {
		long lastTime = System.nanoTime();
		double unprocessed = 0;
		double nsPerTick = 1000000000.0 / Settings.TICK;
		int frames = 0;
		int updates = 0;
		long lastTimer1 = System.currentTimeMillis();
		
		while(!Display.isCloseRequested()){
		    long now = System.nanoTime();
			unprocessed += (now - lastTime) / nsPerTick;
			lastTime = now;
			boolean shouldRender = true;
			while (unprocessed >= 1) {
				updates++;
				//UPDATE
				if (curScreen != null) curScreen.update();
				
				unprocessed -= 1;
				shouldRender = true;
			}
						
			if (shouldRender) {
				frames++;
				//RENDER
				if (curScreen != null) curScreen.render();
				font.renderString("GAME PROTOTYPE", -1f, 1f, 3.5f, 0.85f, new Vector4f(1, 1, 1, 1), Core.renderer);
				font.renderString("" + Core.FPSinfo, -1f, 0.965f, 3f, 0.85f, new Vector4f(1, 1, 1, 1), Core.renderer);
				DisplayManager.render();
			}

			if (System.currentTimeMillis() - lastTimer1 > 1000) {
				lastTimer1 += 1000;
				FPSinfo = ("updates: " + updates + ", fps: " + frames);
				frames = 0;
				updates = 0;
			}
		}	
		
		if (curScreen != null) curScreen.clear();
		renderer.cleanUp();
		DisplayManager.dispose();
		System.exit(0);
	}
	
	public static void exit(){
		if (curScreen != null) curScreen.clear();
		renderer.cleanUp();
		DisplayManager.dispose();
		System.exit(0);
	}
}

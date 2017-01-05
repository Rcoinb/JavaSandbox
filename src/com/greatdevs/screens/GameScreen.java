package com.greatdevs.screens;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.greatdevs.Core;
import com.greatdevs.GameWorld;
import com.greatdevs.renderer.BitmapFont;
import com.greatdevs.renderer.Renderer;

public class GameScreen extends Screen{

	private GameWorld gameWorld;
	
	public static boolean pauseMode = false;
	
	
	//PAUSE ELEMETS --- SHOULD BE DONE IN BETTER WAY
	private static BitmapFont s_font, n_font;
	private float elementX = -0.965f;
	private float elementStartY = 0;
	String[] pauseElements = {"Continue", "Options", "Quit"};
	Vector3f[] pauseElementsColor = {new Vector3f(1, 1, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, 1)};

	
	@Override
	public void init() {
		s_font = new BitmapFont("font/font_small", "font/font_small");
		n_font = new BitmapFont("font/font_normal", "font/font_normal");
		pauseMode = false;
		gameWorld = new GameWorld();
		gameWorld.init();
	}

	@Override
	public void update() {
		if (!pauseMode) gameWorld.update();
		
		if (pauseMode){
			float mX = ((float) Mouse.getX() / (float) Display.getWidth()) * 2 - 1;
			float mY = ((float) Mouse.getY() / (float) Display.getHeight()) * 2 - 1;
			
			for (int i = 0; i < pauseElementsColor.length; i ++) pauseElementsColor[i].set(1, 1, 1);
			
			for (int i = 0; i < pauseElements.length; i ++){
				if (mX > elementX && mX < elementX + 0.25f && mY < (elementStartY - 0.15f * i) && mY > (elementStartY - 0.15f * i - 0.1f)){
					pauseElementsColor[i].set(0.7f, 0.4f, 0.4f);
				}
			}
			while (Mouse.next()) {
				if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
					for (int i = 0; i < pauseElements.length; i ++){
						if (mX > elementX && mX < elementX + 0.25f && mY < (elementStartY - 0.15f * i) && mY > (elementStartY - 0.15f * i - 0.1f)){
							if (i == 0) pauseMode = false;
							if (i == 2) Core.setScreen(new MainMenuScreen());
						}
					}
				}
			}
		}
	}

	@Override
	public void render() {
		Renderer.clearScreen(new Vector4f(0, 0, 0, 0));
		
		gameWorld.render();
		
		if (pauseMode){
			Core.renderer.renderGUI(Renderer.emptyTex, new Vector4f(0, 0, 0, 0.5f), new Vector2f(0, 0), new Vector2f(2, 2), 0, 0, new Vector2f(0, 0), new Vector2f(0, 0));
			Core.renderer.renderGUI(Renderer.emptyTex, new Vector4f(0, 0, 0, 0.5f), new Vector2f(-0.75f, 1), new Vector2f(0.25f, 2), 0, 0, new Vector2f(0, 0), new Vector2f(0, 0));
			for (int i = 0; i < pauseElements.length; i ++){
				s_font.renderString(pauseElements[i], elementX, elementStartY - 0.15f * i, 8f, 0.85f, new Vector4f(pauseElementsColor[i].x, pauseElementsColor[i].y, pauseElementsColor[i].z, 1), Core.renderer);
			}
			
			n_font.renderString("PAUSE", -0.965f, 0.8f, 6, 1f, new Vector4f(1, 1, 1, 1), Core.renderer);
		}
	}

	@Override
	public void clear() {
		gameWorld.clear();
	}

	public static void pause() {
		pauseMode = true;
	}
}

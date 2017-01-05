package com.greatdevs.screens;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.greatdevs.Core;
import com.greatdevs.renderer.BitmapFont;
import com.greatdevs.renderer.Renderer;

public class MainMenuScreen extends Screen{

	//MENU ELEMETS --- SHOULD BE DONE IN BETTER WAY
	private static BitmapFont s_font, n_font;
	private float elementX = -0.8f;
	private float elementStartY = 0;
	
	private Vector3f logoColor = new Vector3f(1, 1, 1);
	
	String[] menuElements = {"Singleplayer", "Multiplayer", "Options", "Exit"};
	Vector3f[] menuElementsColor = {new Vector3f(1, 1, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, 1)};
	
	@Override
	public void init() {
		s_font = new BitmapFont("font/font_small", "font/font_small");
		n_font = new BitmapFont("font/font_normal", "font/font_normal");
	}

	@Override
	public void update() {
		//RANDOM LOGO COLORING
		//logoColor.set(logoColor.x + Maths.random.nextFloat() / 10, logoColor.y + Maths.random.nextFloat() / 10, logoColor.z + Maths.random.nextFloat() / 10); 
		//logoColor.normalise(logoColor);
		
		float mX = ((float) Mouse.getX() / (float) Display.getWidth()) * 2 - 1;
		float mY = ((float) Mouse.getY() / (float) Display.getHeight()) * 2 - 1;
		
		for (int i = 0; i < menuElementsColor.length; i ++) menuElementsColor[i].set(1, 1, 1);
		
		for (int i = 0; i < menuElements.length; i ++){
			if (mX > elementX && mX < elementX + 0.25f && mY < (elementStartY - 0.15f * i) && mY > (elementStartY - 0.15f * i - 0.1f)){
				menuElementsColor[i].set(0.7f, 0.4f, 0.4f);
			}
		}
		while (Mouse.next()) {
			if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
				for (int i = 0; i < menuElements.length; i ++){
					if (mX > elementX && mX < elementX + 0.25f && mY < (elementStartY - 0.15f * i) && mY > (elementStartY - 0.15f * i - 0.1f)){
						if (i == 0) Core.setScreen(new GameScreen());
						if (i == 3) Core.exit();
					}
				}
			}
		}
	}

	@Override
	public void render() {
		Renderer.clearScreen(new Vector4f(0, 0, 0, 1));
		for (int i = 0; i < menuElements.length; i ++){
			s_font.renderString(menuElements[i], elementX, elementStartY - 0.15f * i, 8f, 0.85f, new Vector4f(menuElementsColor[i].x, menuElementsColor[i].y, menuElementsColor[i].z, 1), Core.renderer);
		}
		
		n_font.renderString("SANDBOX", -0.5f, 0.8f, 10, 1f, new Vector4f(logoColor.x, logoColor.y, logoColor.z, 1), Core.renderer);
	}

	@Override
	public void clear() {
		
	}
}

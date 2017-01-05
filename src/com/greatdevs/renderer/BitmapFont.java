package com.greatdevs.renderer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import com.greatdevs.util.DisplayManager;

public class BitmapFont {
	
	private List<character> charList = new ArrayList<character>();
	private Texture fontTex;
	
	public BitmapFont(String filePath, String picPath){
		FileReader fileReader = null;
		try{
			fileReader = new FileReader(new File("res/" + filePath + ".fnt"));
		} catch (FileNotFoundException e){
			System.err.println("Couldn't load file");
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(fileReader);
		String line;
		try {
			line = reader.readLine();	
			while (line != null){
				if (line.startsWith("char ")){
					int charId = Integer.parseInt(line.split("id=")[1].split(" ")[0]);
					int charX = Integer.parseInt(line.split("x=")[1].split(" ")[0]);
					int charY = Integer.parseInt(line.split("y=")[1].split(" ")[0]);
					int charWidth = Integer.parseInt(line.split("width=")[1].split(" ")[0]);
					int charHeight = Integer.parseInt(line.split("height=")[1].split(" ")[0]);
					int charXOffset = Integer.parseInt(line.split("xoffset=")[1].split(" ")[0]);
					int charYOffset = Integer.parseInt(line.split("yoffset=")[1].split(" ")[0]);
					int charXAdvance = Integer.parseInt(line.split("xadvance=")[1].split(" ")[0]);
					
					charList.add(new character(charId, charX, charY, charWidth, charHeight, charXOffset, charYOffset, charXAdvance));
				}
				
				line = reader.readLine();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			fontTex = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/" + picPath + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void renderString(String string, float x, float y, float size, float gap, Vector4f color, Renderer renderer){
		char[] stringChars = string.toCharArray();
		
		float curX = x;
		
		float curSizeWidth =  (DisplayManager.WIDTH * 1000) / size;
		float curSizeHeight = (DisplayManager.HEIGHT * 1000) / size;
		
		for (int i = 0; i < stringChars.length; i ++){
			int charId = 0;
			float curY = y;
			
			for (int ii = 0; ii < charList.size(); ii ++){
				if (charList.get(ii).id == (int) stringChars[i]) charId = ii;
			}
			
			character curChar = charList.get(charId);
			
			Vector2f scale = (new Vector2f((float) curChar.width / curSizeWidth, (float) curChar.height / curSizeHeight));
			curX = curX + (float) (curChar.width) / curSizeWidth;
			curY = curY - (float) (curChar.height) / curSizeHeight;
			curX = curX + (float) (curChar.xoffset * 2) / curSizeWidth;
			curY = curY - (float) (curChar.yoffset * 2) / curSizeHeight;
			
			Vector2f position = new Vector2f(curX, curY);
			
			curX = curX + (float) (curChar.xadvance * gap) / curSizeWidth;
			
			renderer.renderGUI(fontTex.getTextureID(), color, position, scale, 1, fontTex.getImageWidth(), new Vector2f(curChar.x, curChar.y), new Vector2f(curChar.width, curChar.height));
		}
	}
	
	class character{
		int id, x, y, width, height, xoffset, yoffset, xadvance;
		
		public character(int id, int x, int y, int width, int height, int xoffset, int yoffset, int xadvance){
			this.id = id;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.xoffset = xoffset;
			this.yoffset = yoffset;
			this.xadvance = xadvance;
		}
	}
}

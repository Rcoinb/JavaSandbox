package com.greatdevs.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector2f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class DisplayManager {
	
	public static int WIDTH = 16;
	public static int HEIGHT = 9;
	public static int SCALE = 100;

	public static int FPS_CAP = 2048;
	
	public static void createWindow(String title) {	
		try {
			Display.setDisplayMode(new DisplayMode(WIDTH * SCALE, HEIGHT * SCALE));
			//Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
			Display.setResizable(false);
			Display.setVSyncEnabled(false);
			Display.setIcon(loadIcon("res/textures/icon.png"));
			Display.setTitle(title);
			//Display.create(new PixelFormat(2, 2, 0, 2));
			Display.create(new PixelFormat().withDepthBits(24).withBitsPerPixel(32).withSamples(16));
			//Display.create();
			
			Keyboard.create();
			Mouse.create();
			Controllers.create();
			
			System.out.println("Found " + Controllers.getControllerCount() + " controllers");
			System.out.println("Mouse has " + Mouse.getButtonCount() + " button(s).");
			System.out.println("Keyboard has " + Keyboard.getKeyCount() + " key(s).");
			System.out.println("Other controllers:");
			int contWithNoButtonsOrAxis = 0;
			for(int i = 0; i < Controllers.getControllerCount(); i ++){
				Controller cont = Controllers.getController(i);
				if (cont.getAxisCount() > 0 || cont.getButtonCount() > 0) System.out.println("Controller " + cont.getName() + "(" + cont.getIndex() + ") has " + cont.getAxisCount() + " axis and " + cont.getButtonCount() + " button(s).");
				else contWithNoButtonsOrAxis ++;
			}
			if (contWithNoButtonsOrAxis > 0) System.out.println("And " + contWithNoButtonsOrAxis + " controllers has 0 axis and 0 buttons.");
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}
	
	public static ByteBuffer[] loadIcon(String filepath) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filepath));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        ByteBuffer[] buffers = new ByteBuffer[3];
        buffers[0] = loadIconInstance(image, 128);
        buffers[1] = loadIconInstance(image, 32);
        buffers[2] = loadIconInstance(image, 16);
        
        return buffers;
    }
     
    private static ByteBuffer loadIconInstance(BufferedImage image, int dimension) {
        BufferedImage scaledIcon = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledIcon.createGraphics();
        double ratio = 1;
       
        if(image.getWidth() > scaledIcon.getWidth()) {
            ratio = (double) (scaledIcon.getWidth()) / image.getWidth();
        } else {
            ratio = (int) (scaledIcon.getWidth() / image.getWidth());
        }
        if(image.getHeight() > scaledIcon.getHeight()) {
            double r2 = (double) (scaledIcon.getHeight()) / image.getHeight();
            if(r2 < ratio) {
                ratio = r2;
            }
        }
        else {
            double r2 =  (int) (scaledIcon.getHeight() / image.getHeight());
            if(r2 < ratio) {
                ratio = r2;
            }
        }
        double width = image.getWidth() * ratio;
        double height = image.getHeight() * ratio;
        g.drawImage(image, (int) ((scaledIcon.getWidth() - width) / 2), (int) ((scaledIcon.getHeight() - height) / 2),
                (int) (width), (int) (height), null);
        g.dispose();
         
        byte[] imageBuffer = new byte[dimension*dimension*4];
        int counter = 0;
        for(int i = 0; i < dimension; i++) {
            for(int j = 0; j < dimension; j++) {
                int colorSpace = scaledIcon.getRGB(j, i);
                imageBuffer[counter + 0] =(byte)((colorSpace << 8) >> 24 );
                imageBuffer[counter + 1] =(byte)((colorSpace << 16) >> 24 );
                imageBuffer[counter + 2] =(byte)((colorSpace << 24) >> 24 );
                imageBuffer[counter + 3] =(byte)(colorSpace >> 24 );
                counter += 4;
            }
        }
        
        return ByteBuffer.wrap(imageBuffer);
    }
	
	public static void render() {
		Display.sync(FPS_CAP);
		Display.update();
	}
	
	public static void dispose() {
		Display.destroy();
		Keyboard.destroy();
		Mouse.destroy();
	}
	
	public static void bindAsRenderTarget() {
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}
	
	public static boolean isCloseRequested() {
		return Display.isCloseRequested();
	}
	
	public static int getWidth() {
		return Display.getDisplayMode().getWidth();
	}
	
	public static int getHeight() {
		return Display.getDisplayMode().getHeight();
	}
	
	public static String getTitle() {
		return Display.getTitle();
	}

	public Vector2f getCenter() {
		return new Vector2f(getWidth()/2, getHeight()/2);
	}
}

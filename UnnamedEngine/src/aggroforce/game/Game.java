package aggroforce.game;




import java.io.File;
import java.util.logging.Logger;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import aggroforce.render.RenderEngine;
import aggroforce.audio.AudioEngine;
import aggroforce.block.Block;
import aggroforce.event.EventRegistry;
import aggroforce.event.tick.EntityTick;
import aggroforce.input.Input;

public class Game {

	private static final String version = "0.0.0.01";
	public static final Logger log = Logger.getGlobal();

	private static Game instance;
	private static final int scrW = 800;
	private static final int scrH = 600;
	public static DisplayMode screen = new DisplayMode(scrW,scrH);
	private long lastFPS = getTime();
	private long lastFrame;
	private static int fps;
	private int cfps;
	private static int delta;

	public static void main(String[] args){
		System.out.println("Starting SubRoute ver:"+version);
		System.setProperty("org.lwjgl.librarypath", new File("natives").getAbsolutePath()+File.separator+Game.getOSName());
		new Game(args);
	}

	public static Game instance(){
		return instance;
	}
	public Game(String[] args){
		Game.instance = this;
		try {
			Display.setDisplayMode(screen);
			Display.create();
			Display.setTitle("SubRoute [DEV BUILD] Ver:"+version);
			Display.setResizable(true);
//			Display.setVSyncEnabled(true);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		Block.setupBlocks();
		new AudioEngine();
		new RenderEngine();
		new Input();

		this.initOpenGL();
		updateDelta();
		//Main game loop that is exited when the display is closed
		while (!Display.isCloseRequested()) {
			//Update the time between frames
			updateDelta();
			//Recreate the veiwport if the window was resized
			if(Display.wasResized()){
				GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
			}
			//Check all input events for mouse and keyboard (Controller coming soon!)
			Input.checkEvents();
			//Update the entities by passing this event to the bus
			EventRegistry.EVENT_BUS.postEvent(new EntityTick());
			//Only render if the display is visible
			if(Display.isVisible()){
				RenderEngine.instance.renderLoop();
			}
			//Update the FPS counter
			updateFPS();
			AudioEngine.instance().loop();
			Display.update();
			Display.sync(60);
		}
		AL.destroy();
		Display.destroy();
	}
	public long getTime(){
		return (Sys.getTime()*1000)/Sys.getTimerResolution();
	}
	public void updateFPS(){
		if(getTime()-lastFPS > 1000){
			fps = cfps;
			cfps=0;
			lastFPS+=1000;
		}
		cfps++;
	}

	public static String getOSName(){
		String name = System.getProperty("os.name","generic").toLowerCase();
		if(name.indexOf("win") >= 0){
			return "windows";
		}else if(name.indexOf("mac") >= 0){
			return "macosx";
		}else if(name.indexOf("lin") >= 0){
			return "linux";
		}
		return "unknown";
	}

	private void updateDelta() {
		long time = getTime();
		int delta = (int) (time - lastFrame);
		lastFrame = time;
		Game.delta = delta;
	}

	//Returns delta in millisecs
	public static int getDelta(){
		return delta;
	}

	public static int getFps(){
		return fps;
	}

	public void initOpenGL(){
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(45.0f,((float)Game.instance().getScreenWidth()/(float)Game.instance().getScreenHeight()),0.1f,10000.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glClearDepth(1.0);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	public int getScreenWidth(){
		return Display.getWidth();
	}
	public int getScreenHeight(){
		return Display.getHeight();
	}

}

package aggroforce.render;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import aggroforce.game.Game;
import aggroforce.gui.GUIRenderer;
import aggroforce.input.KeyboardReader;
import aggroforce.menu.MainMenu;
import aggroforce.render.camera.Camera;
import aggroforce.texture.TextureRegistry;
import aggroforce.world.WorldLoader;
import aggroforce.world.storage.WorldStorage;

public class RenderEngine {
	public static RenderEngine instance;
	public static TextureRegistry textureMap;
	public static FontRenderer fontRenderer;
	public static RenderBlocks renderBlocks = new RenderBlocks();
	public RenderEngine(){
		instance = this;
		fontRenderer = new FontRenderer();
		textureMap = new TextureRegistry();
		textureMap.loadBaseTextures();
		new GUIRenderer();
		new Camera(0d,-500d,0d);
		this.initLighting();
		GUIRenderer.setCurrentGUI(new MainMenu());
	}

	private final Vector3f lpos = new Vector3f(0,0,0);
	float time;
	WorldStorage wldstor;
	public boolean bool = false;
	public void renderLoop(){


		GL11.glClearColor(0f,0f,0f,1f);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_COLOR_BUFFER_BIT);

		GL11.glPushMatrix();

		if(wldstor!=null){
			Camera.camTransform();
			Camera.thirdPersonOff();

			if(KeyboardReader.keysts[Keyboard.KEY_UP]){
				time += 0.1d*Game.instance().getDelta();
			}
			if(KeyboardReader.keysts[Keyboard.KEY_DOWN]){
				time -= 0.1d*Game.instance().getDelta();
			}
			if(KeyboardReader.keysts[Keyboard.KEY_LEFT]){
			}
			if(KeyboardReader.keysts[Keyboard.KEY_RIGHT]){
			}
			if(KeyboardReader.keysts[Keyboard.KEY_R]!=bool){
				bool=KeyboardReader.keysts[Keyboard.KEY_R];
				renderBlocks.upload();
			}

			lpos.y=1;
			if(time > 360){
				time=0;
			}else{
				time+=Game.instance().getDelta()/1000f;
			}
			double p = Math.sin(Math.toRadians(time))*1000;
			double p2 = Math.cos(Math.toRadians(time))*1000;
			double p3 = Math.sin(Math.toRadians(time+180))*1000;
			double p4 = Math.cos(Math.toRadians(time+180))*1000;
			FloatBuffer pos = BufferUtils.createFloatBuffer(4).put(new float[]{1f,(float)p3+256,(float)p4,0.2f});
			FloatBuffer pos2 = BufferUtils.createFloatBuffer(4).put(new float[]{1f,(float)p+256,(float)p2,0.2f});
			GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, (FloatBuffer) pos.flip());
			GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, (FloatBuffer) pos2.flip());

			FloatBuffer fcolor = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(new float[]{0.5f,0.5f,0.5f,1f}).flip();
			GL11.glFogf(GL11.GL_FOG_DENSITY, 100f);
			GL11.glFog(GL11.GL_FOG_COLOR, fcolor);
			GL11.glFogi(GL11.GL_FOG_END, 90);
			GL11.glFogi(GL11.GL_FOG_START, 80);
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
			GL11.glPointSize(100f);
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glColor4f(1f, 1f, 0f, 1f);
			GL11.glVertex3d(0, p+256, p2);
			GL11.glColor4f(1f, 1f, 1f, 1f);
			GL11.glVertex3d(0, p3+256, p4);
			GL11.glEnd();

		//				GL11.glEnable(GL11.GL_FOG);
			GL11.glEnable(GL11.GL_LIGHTING);
			if(time<180){
				GL11.glEnable(GL11.GL_LIGHT1);
				GL11.glDisable(GL11.GL_LIGHT0);
			}else{
				GL11.glEnable(GL11.GL_LIGHT0);
				GL11.glDisable(GL11.GL_LIGHT1);
			}
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glFrontFace(GL11.GL_CW);

			GL11.glColor4f(1f, 1f, 1f, 1f);
			GL11.glPushMatrix();
			renderBlocks.render();
			GL11.glPopMatrix();

			GL11.glFrontFace(GL11.GL_CCW);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHT1);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_FOG);
			renderBlocks.renderBlockOutline();
		}
		GUIRenderer.renderGUI();
		GL11.glPopMatrix();

	}

	public void initLighting(){
		FloatBuffer gamb = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(new float[]{1f,1f,1f,1f}).flip();
		FloatBuffer amb = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(new float[]{1f,1f,1f,1}).flip();
		FloatBuffer amb2 = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(new float[]{1f,1f,1f,1f}).flip();
		FloatBuffer diff = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(new float[]{0.2f,0.2f,0.2f,0.5f}).flip();
		FloatBuffer diff2 = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(new float[]{1f,1f,1f,0.5f}).flip();
		FloatBuffer spec = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(new float[]{0f,0f,0f,0f}).flip();
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, amb);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, diff);
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, diff2);
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, amb2);
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, spec);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, spec);
		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, gamb);
		GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 0f);
	}
	public void renderDebugAxis(){
		GL11.glLineWidth(5f);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor4f(1f, 0f, 0f, 1f);
		GL11.glVertex3i(0, 0, 0);
		GL11.glVertex3i(10, 0, 0);
		GL11.glColor4f(0f, 1f, 0f, 1f);
		GL11.glVertex3i(0, 0, 0);
		GL11.glVertex3i(0, 10, 0);
		GL11.glColor4f(0f, 0f, 1f, 1f);
		GL11.glVertex3i(0, 0, 0);
		GL11.glVertex3i(0, 0, 10);
		GL11.glEnd();
	}
	public void loadWorld(){
		wldstor = new WorldStorage(new WorldLoader());
	}
}

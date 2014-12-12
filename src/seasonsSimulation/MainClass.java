package seasonsSimulation;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.swing.JFrame;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class MainClass implements GLEventListener {
	private static int textureId;
	public static File img = new File("img/winter.jpg");
	public static GLProfile  profile = GLProfile.get(GLProfile.GL2);
	public static GLCapabilities capabilities = new GLCapabilities (profile);
	public static GLCanvas glcanvas = new GLCanvas(capabilities);
	
	
	@Override
	public void init(GLAutoDrawable drawable) {

	}
	@Override
	public void dispose(GLAutoDrawable drawable) {
	}
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,int height) {
	}
	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D,textureId);		
		gl.glPushMatrix();
		gl.glOrtho(0, 1, 0, 1, 0, 1);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glDepthMask(false);
		try{
			Texture t2 = TextureIO.newTexture(img,true);
			textureId = t2.getTextureObject(gl);			
		}
		catch(IOException e){
			e.printStackTrace();
		}		
		gl.glBegin(GL2GL3.GL_QUADS);
			gl.glTexCoord2f(0f,0f); gl.glVertex2f(0f,0f);
			gl.glTexCoord2f(0f,1f); gl.glVertex2f(0f,1f);
			gl.glTexCoord2f(1f,1f); gl.glVertex2f(1f,1f);
			gl.glTexCoord2f(1f,0f); gl.glVertex2f(1f,0f);
		gl.glEnd();		
		gl.glDepthMask(true);
		gl.glPopMatrix();;
	}
	
	public static void main(String[] args){
		MainClass mainClass = new MainClass();
		glcanvas.addGLEventListener(mainClass);
		glcanvas.setSize(873,699);		

		final JFrame frame = new JFrame ("Seasons Simulation");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(glcanvas);
			frame.setSize(frame.getContentPane().getPreferredSize());
			frame.setVisible(true);
			frame.setResizable(true);
			frame.setLocation(2,32);

		final JFrame control = new JFrame ("Control Panel");
			control.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			control.setSize(360, 120);
			control.setLocationRelativeTo(frame);
			control.setLocation(882,37);
			control.setContentPane(new controlPanelElements());
			control.setResizable(false);
			control.setVisible(true);
			
		final Animator animator = new Animator(glcanvas);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
			  // Run this on another thread than the AWT event queue to
			  // make sure the call to Animator.stop() completes before
			  // exiting
			new Thread(new Runnable() {
				@Override
				public void run() {
					animator.stop();
					System.exit(0);
					}
				}).start();
			}
		});
		frame.setVisible(true);
		animator.start();
	}
}

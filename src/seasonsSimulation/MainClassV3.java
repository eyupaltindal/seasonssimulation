/*
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 *
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package seasonsSimulation;

import seasonsSimulation.linalg.Vec2f;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import seasonsSimulation.common.SeasonsSimulation;
import seasonsSimulation.util.FPSCounter;
import seasonsSimulation.util.SystemTime;
import seasonsSimulation.util.Time;



/** Illustrates more advanced use of the TextRenderer class; shows how
    to do animated translated and rotated text as well as a drop
    shadow effect. */

public class MainClassV3 extends SeasonsSimulation {
	// Information about each piece of text
	private static class TextInfo {
		float angularVelocity;
	    Vec2f velocity;

	    float angle;
	    Vec2f position;

	    float h;
	    float s;
	    float v;

	    // Cycle the saturation
	    float curTime;

	    // Cache of the RGB color
	    float r;
	    float g;
	    float b;

	    String text;
	  }


	// Put a little physics on the text to make it look nicer
	private static final float INIT_ANG_VEL_MAG = 0.3f;
	private static final float INIT_VEL_MAG = 400.0f;
	private static final int   DEFAULT_DROP_SHADOW_DIST = 20;
	
	private final List<TextInfo> textInfo = new ArrayList<TextInfo>();
	private int dropShadowDistance = DEFAULT_DROP_SHADOW_DIST;
	private Time time;
	private Texture backgroundTexture;
	private TextRenderer renderer;
	private final Random random = new Random();
	private final GLU glu = new GLU();
	private int width;
	private int height;

	private int maxTextWidth;

	private FPSCounter fps;
	private File file = null;
	private Image image = null;
	private BufferedImage bgImage = null;
	private String mod = "'";
	private Color c = new Color(0,0,0);

	private static JFrame frame;
	
	public static void main(String[] args) {
		frame = new JFrame("Flying Text");
		frame.getContentPane().setLayout(new BorderLayout());

		GLCanvas canvas = new GLCanvas();
		final MainClassV3 mainClassV3 = new MainClassV3();

		canvas.addGLEventListener(mainClassV3);
		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		frame.getContentPane().add(mainClassV3.buildGUI(), BorderLayout.NORTH);

		DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

//    	frame.setSize((int) (0.75f * mode.getWidth()),
//                  (int) (0.75f * mode.getHeight()));
		frame.setSize(873,699);
		frame.setResizable(false);
		frame.setLocation(100,50);

		final Animator animator = new Animator(canvas);
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


	public Container buildGUI() {
		// Create gui
		JPanel panel = new JPanel();
		JButton button = new JButton("Spring");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				spring();
			}
		});
		panel.add(button);

		button = new JButton("Summer");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				summer();
			}
		});
		panel.add(button);

		final JSlider slider = new JSlider(JSlider.HORIZONTAL,
										   getMinDropShadowDistance(),
										   getMaxDropShadowDistance(),
										   getDropShadowDistance());
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
			  setDropShadowDistance(slider.getValue());
			}
		});
		panel.add(slider);

		button = new JButton("Fall");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fall();
			}
		});
		panel.add(button);

		button = new JButton("Winter");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				winter();
			}
		});
		panel.add(button);
		
		button = new JButton("Add");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moreText();
			}
		});
		panel.add(button);

		return panel;
	}

	public void spring() {
		file = null;
		bgImage = null;
		image = null;
		
		file = new  File("img/spring.jpg");
		c = new Color(0,0,0);
		mod = "'";
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bgImage = toBufferedImage(image);
		frame.setSize(873,700);
	}

	public void summer() {
		file = null;
		bgImage = null;
		image = null;
		
		file = new File("img/summer.jpg");
		c = new Color(0,0,0);
		mod = "";
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bgImage = toBufferedImage(image);
		frame.setSize(873,700);
	}

	public void fall() {
		file = null;
		bgImage = null;
		image = null;
		
		file = new File("img/fall.jpg");
		c = new Color(255,0,0);
		mod = "#";
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bgImage = toBufferedImage(image);
		frame.setSize(873,700);
	}

	public void winter() {
		file = null;
		bgImage = null;
		image = null;
		
		file = new File("img/winter.jpg");
		c = new Color(255,255,255);
		mod = "*";
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bgImage = toBufferedImage(image);
		frame.setSize(873,700);
	}
  
  
	public void moreText() {
		int numToAdd = (int) (textInfo.size() * 0.5f);
		if (numToAdd == 0)
			numToAdd = 1;
		for (int i = 0; i < numToAdd; i++) {
			textInfo.add(randomTextInfo());
		}
	}

	public void lessText() {
		if (textInfo.size() == 1)
			return;
		int numToRemove = textInfo.size() / 3;
		if (numToRemove == 0)
			numToRemove = 1;
		for (int i = 0; i < numToRemove; i++) {
			textInfo.remove(textInfo.size() - 1);
		}
	}

	public int getDropShadowDistance() {
		return dropShadowDistance;
	}

	public int getMinDropShadowDistance() {
		return 1;
	}

	public int getMaxDropShadowDistance() {
		return 30;
	}

	public void setDropShadowDistance(int dist) {
		dropShadowDistance = dist;
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		file = new  File("img/spring.jpg");
		bgImage = null;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bgImage = toBufferedImage(image);
		
		backgroundTexture = AWTTextureIO.newTexture(gl.getGLProfile(), bgImage, false);
		backgroundTexture.bind(gl);

		// Create the text renderer
		renderer = new TextRenderer(new Font("Serif", Font.PLAIN, 72), true, true);

		// Create the FPS counter
		fps = new FPSCounter(drawable, 36);

		width = drawable.getSurfaceWidth();
		height = drawable.getSurfaceHeight();

		// Compute maximum width of text we're going to draw to avoid
		// popping in/out at edges
		maxTextWidth = (int) renderer.getBounds("Java 2D").getWidth();
		maxTextWidth = Math.max(maxTextWidth, (int) renderer.getBounds("OpenGL").getWidth());

		// Create random text
		textInfo.clear();
		for (int i = 0; i < 100; i++) {
			textInfo.add(randomTextInfo());
		}

		time = new SystemTime();
		((SystemTime) time).rebase();

		// Set up properties; note we don't need the depth buffer in this demo
		gl.glDisable(GL2.GL_DEPTH_TEST);
		// Turn off vsync if we can
		gl.setSwapInterval(0);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		backgroundTexture = null;
		renderer = null;
		fps = null;
		time = null;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		time.update();

		// Update velocities and positions of all text
		float deltaT = (float) time.deltaT();
		Vec2f tmp = new Vec2f();
		for (Iterator<TextInfo> iter = textInfo.iterator(); iter.hasNext(); ){
			TextInfo info = iter.next();

			// Randomize things a little bit at run time
			if (random.nextInt(1000) == 0) {
				info.angularVelocity = INIT_ANG_VEL_MAG * (randomAngle() - 180);
				info.velocity = randomVelocityVec2f(INIT_VEL_MAG, INIT_VEL_MAG);
			}

			// Now update angles and positions
			info.angle += info.angularVelocity * deltaT;
			tmp.set(info.velocity);
			tmp.scale(deltaT);
			info.position.add(tmp);

			// Update color
			info.curTime += deltaT;
			if (info.curTime > 2 * Math.PI) {
				info.curTime -= 2 * Math.PI;
			}
			int rgb = Color.HSBtoRGB(info.h,
									(float) (0.5 * (1 + Math.sin(info.curTime)) * info.s),
									info.v);
			info.r = ((rgb >> 16) & 0xFF) / 255.0f;
			info.g = ((rgb >>  8) & 0xFF) / 255.0f;
			info.b = ( rgb        & 0xFF) / 255.0f;

			// Wrap angles and positions
			if (info.angle < 0) {
				info.angle += 360;
			} else if (info.angle > 360) {
				info.angle -= 360;
			}
			// Use maxTextWidth to avoid popping in/out at edges
			// Would be better to do oriented bounding rectangle computation
			if (info.position.x() < -maxTextWidth) {
				info.position.setX(info.position.x() + drawable.getSurfaceWidth() + 2 * maxTextWidth);
			} else if (info.position.x() > drawable.getSurfaceWidth() + maxTextWidth) {
				info.position.setX(info.position.x() - drawable.getSurfaceWidth() - 2 * maxTextWidth);
			}
			if (info.position.y() < -maxTextWidth) {
				info.position.setY(info.position.y() + drawable.getSurfaceHeight() + 2 * maxTextWidth);
			} else if (info.position.y() > drawable.getSurfaceHeight() + maxTextWidth) {
				info.position.setY(info.position.y() - drawable.getSurfaceHeight() - 2 * maxTextWidth);
			}
		}

		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluOrtho2D(0, drawable.getSurfaceWidth(), 0, drawable.getSurfaceHeight());
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		
		// Draw the background texture
		backgroundTexture.enable(gl);
		backgroundTexture.bind(gl);
		TextureCoords coords = backgroundTexture.getImageTexCoords();
		int w = drawable.getSurfaceWidth();
		int h = drawable.getSurfaceHeight();
		float fw = w / 100.0f;
		float fh = h / 100.0f;
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(coords.left(),coords.bottom());
		gl.glVertex3f(0, 0, 0);
		gl.glTexCoord2f(coords.right(),coords.bottom());
		gl.glVertex3f(w, 0, 0);
		gl.glTexCoord2f(coords.right(),coords.top());
		gl.glVertex3f(w, h, 0);
		gl.glTexCoord2f(coords.left(),coords.top());
		gl.glVertex3f(0, h, 0);
		gl.glEnd();
		backgroundTexture.disable(gl);

		// Render all text
		renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());

		// Note we're doing some slightly fancy stuff to position the text.
		// We tell the text renderer to render the text at the origin, and
		// manipulate the modelview matrix to put the text where we want.

		gl.glMatrixMode(GL2.GL_MODELVIEW);

		// First render drop shadows
		renderer.setColor(0, 0, 0, 0.5f);
		for (Iterator<TextInfo> iter = textInfo.iterator(); iter.hasNext(); ){
			TextInfo info = iter.next();
			gl.glLoadIdentity();
			gl.glTranslatef(info.position.x() + dropShadowDistance,
							info.position.y() - dropShadowDistance,
							0);
			gl.glRotatef(info.angle, 0, 0, 1);
			renderer.draw(info.text, 0, 0);
			// We need to call flush() only because we're modifying the modelview matrix
			renderer.flush();
		}

		// Now render the actual text
		for (Iterator<TextInfo> iter = textInfo.iterator(); iter.hasNext(); ){
			TextInfo info = iter.next();
			gl.glLoadIdentity();
			gl.glTranslatef(info.position.x(),
							info.position.y(),
							0);
			gl.glRotatef(info.angle, 0, 0, 1);
			renderer.setColor(info.r, info.g, info.b, 1);
			renderer.draw(info.text, 0, 0);
			// We need to call flush() only because we're modifying the modelview matrix
			renderer.flush();
		}

		renderer.endRendering();

		// Use the FPS renderer last to render the FPS
		fps.draw();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		this.width = width;
		this.height = height;
		GL gl = drawable.getGL();
		backgroundTexture = null;
		backgroundTexture = AWTTextureIO.newTexture(gl.getGLProfile(), bgImage, false);
		System.out.println("Frame Size ++, --");
		frame.setSize(873,699);
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
	}

	private TextInfo randomTextInfo() {
		TextInfo info = new TextInfo();
		info.text = mod;
		info.angle = randomAngle();
		info.position = randomVec2f(width, height);

		info.angularVelocity = INIT_ANG_VEL_MAG * (randomAngle() - 180);
		info.velocity = randomVelocityVec2f(INIT_VEL_MAG, INIT_VEL_MAG);
		
//		Color c = randomColor();
		float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);

//		float[] hsb = Color.RGBtoHSB(255, 255, 255, null);
		info.h = hsb[0];
		info.s = hsb[1];
		info.v = hsb[2];
		info.curTime = (float) (2 * Math.PI * random.nextFloat());
		return info;
	}

//	private String randomString() {
//		switch (random.nextInt(3)) {
//			case 0:
//				return "A";
//			case 1:
//				return "B";
//			default:
//				return "C";
//		}
//	}

	private float randomAngle() {
		return 360.0f * random.nextFloat();
	}

	private Vec2f randomVec2f(float x, float y) {
		return new Vec2f(x * random.nextFloat(),y * random.nextFloat());
	}

	private Vec2f randomVelocityVec2f(float x, float y) {
		return new Vec2f(x * (random.nextFloat() - 0.5f),y * (random.nextFloat() - 0.5f));
	}

//	private Color randomColor() {
//		// Get a bright and saturated color
//		float r = 255;
//		float g = 255;
//		float b = 255;
//		float s = 0;
//		do{
//			r = random.nextFloat();
//			g = random.nextFloat();
//			b = random.nextFloat();
//
//			float[] hsb = Color.RGBtoHSB((int) (255.0f * r), (int) (255.0f * g),(int) (255.0f * b), null);
//			s = hsb[1];
//		} while ((r < 0.8f && g < 0.8f && b < 0.8f) || s < 0.8f);
//		return new Color(r, g, b);
//	}
  
	public BufferedImage toBufferedImage(Image img){
		if (img instanceof BufferedImage){
			return (BufferedImage) img;
		}
		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}
}

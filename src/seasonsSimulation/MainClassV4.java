package seasonsSimulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import seasonsSimulation.common.SeasonsSimulation;
import seasonsSimulation.linalg.Vec2f;
import seasonsSimulation.util.SystemTime;
import seasonsSimulation.util.Time;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;



/** Illustrates more advanced use of the TextRenderer class; shows how
    to do animated translated and rotated text as well as a drop
    shadow effect. */

public class MainClassV4 extends SeasonsSimulation {
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

	
	private final List<TextInfo> textInfo = new ArrayList<TextInfo>();
	private int sliderDefaultValue = 5;
	private int sliderMinValue = 1;
	private int sliderMaxValue = 10;
	private int sliderValue = sliderDefaultValue;
	private Time time;
	private Texture backgroundTexture;
	private TextRenderer renderer;
	private final Random random = new Random();
	private final GLU glu = new GLU();
	private int width;
	private int height;
	private File file = null;
	private Image image = null;
	private BufferedImage bgImage = null;
	private String mod = "l";
	private Color c = new Color(150,150,150);
	private int itemCount = 200;
	private int itemAngle = -50;
	private int itemVelocity = -300;
	private int itemPAngle = -10;
	private static Font font;
	private String seasons = "spring";
	private static JFrame frame;
	
	public static void main(String[] args) {
		frame = new JFrame("Kocaeli Universitesi | BLM407 Bilgisayar Grafikleri");
		frame.getContentPane().setLayout(new BorderLayout());
		font = new Font("Arial", Font.PLAIN, 24);

		GLCanvas canvas = new GLCanvas();
		final MainClassV4 mainClassV4 = new MainClassV4();

		canvas.addGLEventListener(mainClassV4);
		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		frame.getContentPane().add(mainClassV4.buildGUI(), BorderLayout.NORTH);

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
		// Butonlar olusturuluyor
		JPanel panel = new JPanel();
		final JSlider slider = new JSlider(JSlider.HORIZONTAL, sliderMinValue, sliderMaxValue, sliderDefaultValue);
		
		JButton button = new JButton("Spring");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				spring();
				slider.setValue(sliderDefaultValue);
			}
		});
		panel.add(button);

		button = new JButton("Summer");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				summer();
				slider.setValue(sliderDefaultValue);
			}
		});
		panel.add(button);

		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(slider.getValue() > sliderValue){
					//art
					moreItem();
					sliderValue = slider.getValue();
					itemVelocity = itemVelocity - 50;
					System.out.println(slider.getValue());
					System.out.println(itemVelocity);
				}else if (slider.getValue() < sliderValue){
					//azal
					lessItem();
					sliderValue = slider.getValue();
					itemVelocity = itemVelocity + 50;
					System.out.println(slider.getValue());
					System.out.println(itemVelocity);
				}
				
			}
		});
		panel.add(slider);

		button = new JButton("Fall");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fall();
				slider.setValue(sliderDefaultValue);
			}
		});
		panel.add(button);

		button = new JButton("Winter");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				winter();
				slider.setValue(sliderDefaultValue);
			}
		});
		panel.add(button);

		return panel;
	}

	public void spring() {
		seasons = "spring";
		file = null;
		bgImage = null;
		image = null;
		itemAngle = -50;
		itemVelocity = -300;
		itemPAngle = -10;
		font = new Font("Arial", Font.PLAIN, 24);
		renderer = new TextRenderer(font, true, true);
		c = new Color(150,150,150);
		mod = "l";
		textInfo.clear();
		for (int i = 0; i < itemCount; i++) {
			textInfo.add(randomTextInfo());
		}

		file = new  File("img/spring.jpg");
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bgImage = toBufferedImage(image);
		frame.setSize(873,700);
		
	}

	public void summer() {
		seasons = "summer";
		file = null;
		bgImage = null;
		image = null;
		itemAngle = 0;
		itemVelocity = -300;
		itemPAngle = 0;
		font = new Font("Arial", Font.PLAIN, 24);
		renderer = new TextRenderer(font, true, true);
		
		c = new Color(0,0,0);
		mod = "";
		textInfo.clear();
		for (int i = 0; i < itemCount; i++) {
			textInfo.add(randomTextInfo());
		}

		file = new File("img/summer.jpg");
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bgImage = toBufferedImage(image);
		frame.setSize(873,700);
	}

	public void fall() {
		seasons = "fall";
		file = null;
		bgImage = null;
		image = null;
		itemAngle = 0;
		itemVelocity = -300;
		itemPAngle = 0;
		
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("Leafs.ttf"))).deriveFont(Font.PLAIN, 48);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		renderer = new TextRenderer(font, true, true);
		
		c = new Color(255,0,0);
		//mod = "B";
		textInfo.clear();
		for (int i = 0; i < itemCount - 100 ; i++) {
			textInfo.add(randomTextInfo());
		}
		
		file = new File("img/fall.jpg");
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bgImage = toBufferedImage(image);

		frame.setSize(873,700);
	}

	public void winter() {
		seasons = "winter";
		file = null;
		bgImage = null;
		image = null;
		itemAngle = 0;
		itemVelocity = -300;
		itemPAngle = 0;
		font = new Font("Arial", Font.PLAIN, 24);
		renderer = new TextRenderer(font, true, true);
		
		c = new Color(230,230,230);
		mod = "*";
		textInfo.clear();
		for (int i = 0; i < itemCount; i++) {
			textInfo.add(randomTextInfo());
		}
		
		file = new File("img/winter.jpg");
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bgImage = toBufferedImage(image);
		frame.setSize(873,700);
	}
  
  
	public void moreItem() {
		int numToAdd;
		if(seasons.equals("fall")){
			numToAdd = (int) (textInfo.size() * 0.25f);
		}else{
			numToAdd = (int) (textInfo.size() * 0.5f);
		}
		System.out.println(textInfo.size() + "-" + numToAdd);

		if (numToAdd == 0)
			numToAdd = 1;
		for (int i = 0; i < numToAdd; i++) {
			textInfo.add(randomTextInfo());
		}
	}

	public void lessItem() {
		if (textInfo.size() == 1)
			return;
		int numToRemove;
		if(seasons.equals("fall")){
			numToRemove = textInfo.size() / 5;			
		}else{
			numToRemove = textInfo.size() / 3;
		}
		if (numToRemove == 0)
			numToRemove = 1;
		for (int i = 0; i < numToRemove; i++) {
			textInfo.remove(textInfo.size() - 1);
		}
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		file = new  File("img/spring.jpg");
		bgImage = null;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bgImage = toBufferedImage(image);
		
		backgroundTexture = AWTTextureIO.newTexture(gl.getGLProfile(), bgImage, false);
		backgroundTexture.bind(gl);
				
		// Create the text renderer
		renderer = new TextRenderer(font, true, true);
		
		width = drawable.getSurfaceWidth();
		height = drawable.getSurfaceHeight();

		// Create random item
		textInfo.clear();
		for (int i = 0; i < itemCount; i++) {
			textInfo.add(randomTextInfo());
		}

		time = new SystemTime();
		((SystemTime) time).rebase();

		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.setSwapInterval(0);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		backgroundTexture = null;
		renderer = null;
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
			if(seasons.equals("fall")){
				if (random.nextInt(1000) == 0) {
					info.angularVelocity = 0.3f * (randomAngle() - 180);
					info.velocity = randomVelocityVec2f(400f, 400f);
				}		
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
			if (info.position.x() < -10) {
				info.position.setX(info.position.x() + drawable.getSurfaceWidth() + 0.9f * 50);
			} else if (info.position.x() > drawable.getSurfaceWidth() + 50) {
				info.position.setX(info.position.x() - drawable.getSurfaceWidth() - 0.9f * 50);
			}
			if (info.position.y() < -50) {
				info.position.setY(info.position.y() + drawable.getSurfaceHeight() + 0.9f * 50);
			} else if (info.position.y() > drawable.getSurfaceHeight() + 50) {
				info.position.setY(info.position.y() - drawable.getSurfaceHeight() - 0.9f * 50);
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
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		// First render drop shadows
//		renderer.setColor(0, 0, 0, 0.5f);
//		for (Iterator<TextInfo> iter = textInfo.iterator(); iter.hasNext(); ){
//			TextInfo info = iter.next();
//			gl.glLoadIdentity();
//			gl.glTranslatef(info.position.x() + 1,
//							info.position.y() - 1,
//							0);
//			gl.glRotatef(info.angle, 0, 0, 1);
//			renderer.draw(info.text, 0, 0);
//			// We need to call flush() only because we're modifying the modelview matrix
//			renderer.flush();
//		}

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
		if(seasons.equals("fall")){
			info.text = randomString();
			info.angle = randomAngle();
			info.position = randomVec2f(width, height);
			info.velocity = randomVelocityVec2f(400f, 400f);
		}else{
			info.text = mod;
			info.angle = itemPAngle;
			info.position = randomVec2f(width, height);
			info.velocity = new Vec2f(itemAngle,itemVelocity);
		}

		info.angularVelocity = 0;
		
		float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		info.h = hsb[0];
		info.s = hsb[1];
		info.v = hsb[2];
		info.curTime = (float) (2 * Math.PI * random.nextFloat());
		return info;
	}

	private String randomString() {
		switch (random.nextInt(8)) {
			case 0:		return "B";
			case 1:		return "C";
			case 2:		return "W";
			case 3:		return "R";
			case 4:		return "Y";
			case 5:		return "L";
			case 6:		return "K";
			case 7:		return "F";
			default:	return "C";
		}
	}

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
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		return bimage;
	}
}
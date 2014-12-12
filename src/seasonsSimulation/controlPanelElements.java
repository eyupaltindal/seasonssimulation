package seasonsSimulation;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JSlider;

public class controlPanelElements extends Container {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public controlPanelElements() {
		super();
	    this.setLayout(new FlowLayout());
		JButton winterJButton = new JButton("Winter");
		winterJButton.addActionListener(new ActionListener() {
				 
	            public void actionPerformed(ActionEvent e)
	            {
	            	MainClass.img = new File("img/winter.jpg");
	            	System.out.println(MainClass.img.getName());
	            	MainClass.glcanvas.repaint();
	                return;
	            }
	        });
		JButton springJButton = new JButton("Spring");
		springJButton.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e)
            {
            	MainClass.img = new File("img/spring.jpg");
            	System.out.println(MainClass.img.getName());
            	MainClass.glcanvas.repaint();
                return;
            }
        });
		JButton summerJButton = new JButton("Summer");
		summerJButton.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e)
            {
            	MainClass.img = new File("img/summer.jpg");
            	System.out.println(MainClass.img.getName());
            	MainClass.glcanvas.repaint();
                return;
            }
        });
		JButton fallJButton = new JButton("Fall");
		fallJButton.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e)
            {
            	MainClass.img = new File("img/fall.jpg");
            	System.out.println(MainClass.img.getName());
            	MainClass.glcanvas.repaint();
                return;
            }
        });
			
		JSlider sliderSpeed = new JSlider(JSlider.HORIZONTAL, 0, 50, 25);
			sliderSpeed.setMajorTickSpacing(10);
			sliderSpeed.setMinorTickSpacing(1);
			sliderSpeed.setPaintTicks(true);
			sliderSpeed.setPaintLabels(true);
			sliderSpeed.setLabelTable(sliderSpeed.createStandardLabels(10));
		add(winterJButton);
		add(springJButton);
		add(summerJButton);
		add(fallJButton);
	    add(sliderSpeed);
	  }
}

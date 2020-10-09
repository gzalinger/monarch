// simple component used to manipulate layouts

package gui;
import java.awt.*;
import javax.swing.*;


public class GUIRectangle extends JComponent {

    private Color color;
    private String text;
    private static final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 14);


    public GUIRectangle(int w, int h) {
	setPreferredSize(new Dimension(w, h));
    } //end constructor1

    public GUIRectangle(int w, int h, Color c) {
	this(w, h);
	color = c;
    } //end constructor2

    public GUIRectangle(int w, int h, Color c, String s) {
	this(w, h, c);
	text = s;
    } //end constructor3


    public void paintComponent(Graphics g) {
	if(color != null) {
	    g.setColor(color);
	    g.fillRect(0, 0, getWidth(), getHeight());
	}
	if(text != null) {
	    g.setColor(Color.BLACK);
	    g.drawString(text, 4, 16);
	}
    } //end paintComponent


} //end class

package bremoswing;
import java.awt.*;
import javax.swing.*;
/**
 * This icon can be built with another icon and will render it with a Rollover effect
 * where the icon is darkened and has its blue intensity increased
 */
public class RolloverIcon implements Icon {
	protected Icon fIcon;
/**
 * Construct us with the icon we will create paint a rollover icon for
 */
public RolloverIcon(Icon anIcon) {
	fIcon = anIcon;
}
public int getIconHeight() {
	return fIcon.getIconHeight();
}
public int getIconWidth() {
	return fIcon.getIconWidth();
}
public void paintIcon(Component c, Graphics g, int x, int y) {

	Graphics2D g2D = (Graphics2D)g;
	Composite oldComposite = g2D.getComposite();
	g2D.setComposite(RolloverComposite.DEFAULT);
	fIcon.paintIcon(c,g,x,y);
	g2D.setComposite(oldComposite);
	
}
}
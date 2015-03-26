package bremoswing.util;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class TextPaneOutputStream extends OutputStream {
	private final JTextPane textPane;
	private Style style;

	public TextPaneOutputStream(JTextPane textPane) {

		this.textPane = textPane;

		// Get the default style
		StyleContext sc = StyleContext.getDefaultStyleContext();
		Style defaultContextStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);

		// Add some styles to the document, to retrieve and use later
		StyledDocument document = textPane.getStyledDocument();
		Style normalStyle = document.addStyle("normal", defaultContextStyle);

		// Create a blue color style
		Style blueColorStyle = document.addStyle("blue", normalStyle);
		StyleConstants.setForeground(blueColorStyle, Color.BLUE);

		// Create a red color style
		Style redColorStyle = document.addStyle("red", normalStyle);
		StyleConstants.setForeground(redColorStyle, Color.RED);

		// Create a yellow color style
		Style yellowColorStyle = document.addStyle("yellow", normalStyle);
		StyleConstants.setForeground(yellowColorStyle, new Color(204, 102, 0));

	}

	public void SetTextColor(Color color) {

		StyledDocument document = textPane.getStyledDocument();

		if (color.equals(Color.BLUE)) {

			style = document.getStyle("blue");

		} else if (color.equals(Color.RED)) {

			style = document.getStyle("red");

		} else if (color.equals(new Color(204, 102, 0))) {

			style = document.getStyle("yellow");
		}
	}

	@Override
	public void write(int b) throws IOException {
		append(String.valueOf((char) b));
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		append(new String(b, off, len));
	}

	private synchronized void append(final String text) {

		Document doc = textPane.getDocument();
		try {
			doc.insertString(doc.getLength(), text, style);
		} catch (BadLocationException e) {
		}
		textPane.setCaretPosition(doc.getLength() - 1);

	}

}

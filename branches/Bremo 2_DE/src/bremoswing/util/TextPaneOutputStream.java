package bremoswing.util;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

public class TextPaneOutputStream extends OutputStream
{
	private JTextPane textPane;
	private Style style;
	private Color colorText;

	public TextPaneOutputStream(JTextPane textPane)
	{
		this.textPane = textPane;
		SetTextColor(Color.red);
		textPane.validate();
		
	}
	
	public void  SetTextColor(Color color) {
		style = textPane.addStyle(null, null);
		colorText = color;
		StyleConstants.setForeground(style, colorText);
		textPane.validate();
	}

	@Override
	public void write(int b) throws IOException
	{
		append(String.valueOf((char) b));
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		append(new String(b, off, len));
	}

	private void append(final String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				Document doc = textPane.getDocument();
				try
				{
					doc.insertString(doc.getLength(), text, style);
				} catch (BadLocationException e)
				{}
				textPane.setCaretPosition(doc.getLength() - 1);
			}
		});
	}

}

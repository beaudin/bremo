package funktionenTests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import bremoswing.util.BremoSwingUtil;

public class ShowTable extends JFrame {

	JTable table;
	Vector<String> header;
	Vector<Vector<String>> dataModel;

	public ShowTable(File file) {

		try {
			header = new Vector<String>();
			dataModel = new Vector<Vector<String>>();
			getData1(file);
			// getData1("D2T-MORPHEE.txt");
			// getData2("IFKM-LABVIEW-DAQ.txt");
			// getData2("IFKM-LABVIEW-DAQ.txt");

			table = new JTable(dataModel, header);

		} catch (IOException e) {
			e.printStackTrace();
		}

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		int gapWidth = 10;
		int gapHeight = 0;
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowHeight(table.getRowHeight() + 5);
		table.setIntercellSpacing(new Dimension(gapWidth, gapHeight));
		table.setSelectionBackground(Color.cyan);
		table.setSelectionForeground(Color.red);

		JScrollPane scrollPane = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		int width = table.getPreferredSize().width;
		int height = table.getPreferredSize().height;

		if (width >= 1280) {
			width = 1280;
		}
		if (height >= 720) {
			height = 720;
		}
		table.setPreferredScrollableViewportSize(new Dimension(width, height));
		table.setFillsViewportHeight(true);

		add(scrollPane);
		JPanel panel = new JPanel();
		panel.add(new JButton("OK"));
		add(panel, BorderLayout.SOUTH);
		// setPreferredSize(new Dimension(1920, 830));
		pack();
		BremoSwingUtil.placeFrame(this);
		setVisible(true);
	}

	public void getData1(File file) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(file));
		String zeile = "";
		while ((zeile = br.readLine()) != null) {
			if (zeile.contains("\t"))
				break;
		}
		String[] tmp = zeile.split("\t");

		header = new Vector<String>(Arrays.asList(tmp));

		while ((zeile = br.readLine()) != null)
			if (zeile.contains("\t")) {
				tmp = zeile.split("\t");
				Vector<String> row = new Vector<String>(Arrays.asList(tmp));
				dataModel.addElement(row);
			}
		br.close();
	}

	public void getData2(File file) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(file));
		String zeile = "";
		while ((zeile = br.readLine()) != null) {
			if (zeile.contains("\t"))
				break;
		}
		String[] tmp = zeile.split("\t");
		for (String elt : tmp) {
			header.addElement(elt);
		}

		while ((zeile = br.readLine()) != null)
			if (zeile.contains("\t")) {
				tmp = zeile.split("\t");
				Vector<String> row = new Vector<String>();
				for (String elt : tmp) {
					row.addElement(elt);
				}
				dataModel.addElement(row);
			}
		br.close();
	}

	public static void main(String[] arg) {

		// new ShowTable();

	}

}

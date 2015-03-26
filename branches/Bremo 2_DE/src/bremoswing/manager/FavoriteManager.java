package bremoswing.manager;

import io.AusgabeSteurung;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bremoswing.SwingBremoModel;

public class FavoriteManager {

	HashMap<String, List<String>> favorite;
	File file;

	public FavoriteManager() {
		favorite = new HashMap<String, List<String>>();
		File f = new File("src/bremoswing/utilFile/bremo.favs");
		if (f.exists()) { // check favs file im Eclipse
			file = f;
			readFavsFile();
		} else { // Check favs file wenn es ein Applet ist
			f = new File(SwingBremoModel.BremoAppletDirectory.getAbsoluteFile()
					+ File.separator + "bremo.favs");
			if (SwingBremoModel.BremoAppletDirectory.exists() && f.exists()) {
				file = f;
				readFavsFile();
			} else {
				f = new File("src/bremoswing/utilFile/bremo.favs");
				try {
					f.createNewFile();
					file = f;
				} catch (IOException e) {
					if (!SwingBremoModel.BremoAppletDirectory.exists()) {
						SwingBremoModel.BremoAppletDirectory.mkdir();
					}
					file = new File(
							SwingBremoModel.BremoAppletDirectory
									.getAbsoluteFile()
									+ File.separator
									+ "bremo.favs");
				}
			}
		}
	}

	public void readFavsFile() {

		readFavsFile(file);
	}

	private void readFavsFile(File f) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String zeile = "";
			String key = "";
			String index = "";
			List<String> element = new ArrayList<String>();
			while ((zeile = br.readLine()) != null) {
				if (zeile.contains("FAV_")) {
					key = zeile.substring(zeile.indexOf("F"),
							zeile.indexOf("_") + 2);
				} else if (zeile.contains("Log")) {
					index = zeile.split(" := ")[1];
					element.add(index);
				} else if (zeile.contains("x_index")) {
					index = zeile.split(" := ")[1];
					element.add(index);
				} else if (zeile.contains("y_index")) {
					index = zeile.split(" := ")[1];
					element.add(index);
				} else if (zeile.contains("#") && !zeile.contains("FAV_")) {
					List<String> e = new ArrayList<String>(element);
					element.clear();
					favorite.put(key, e);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			AusgabeSteurung.Error("Favorite file Not Found !");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Save the Contains of the HashMap Favorite to the FAVS File
	 */
	public void saveToFavsFile() {
		saveToFavsFile(file);

	}

	/**
	 * Save the Contains of the HashMap Favorite to the File
	 */
	private void saveToFavsFile(File file) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
			for (Entry<String, List<String>> entry : favorite.entrySet()) {
				String favs = entry.getKey();
				List<String> value = entry.getValue();
				bw.write("########## " + favs + " ##########");
				bw.newLine();
				bw.write("Log := " + value.get(0));
				bw.newLine();
				bw.write("x_index := " + value.get(1));
				bw.newLine();
				for (int i = 2; i < value.size(); i++) {
					bw.write("y_index_" + (i - 1) + " := " + value.get(i));
					bw.newLine();
				}
				bw.write("###########################");
				bw.newLine();
			}
			bw.close();
		} catch (FileNotFoundException e) {
			AusgabeSteurung.Error("Favs File no Found to save !");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<String> getFavNummer(int nbr) {

		return favorite.get("FAV_" + nbr);

	}

	public void addFavsNummer(int nbr, List<String> entry) {
		if (getFavNummer(nbr) != null)
			favorite.remove("FAV_" + nbr);
		favorite.put("FAV_" + nbr, entry);
		saveToFavsFile();
	}

	public String getDirectoryFavsFile() {
		if (file.exists())
			return file.getParent() + File.separator;
		else
			return null;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return favorite.isEmpty();
	}

	public HashMap<String, List<String>> getFavsList() {
		return favorite;
	}

}

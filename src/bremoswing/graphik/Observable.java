package bremoswing.graphik;

import java.util.Observer;

public interface Observable {

	public void notifyObserver(String str);
	public void notifyObserver(String [] head);
	void addObserver(Observer obs);
	void deleteObserver();
	Observer getObserver();

}

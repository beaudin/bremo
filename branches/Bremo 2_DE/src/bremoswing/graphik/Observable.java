package bremoswing.graphik;

import java.util.Observer;

public interface Observable {

	void addObserver(Observer obs);

	void deleteObserver();

	Observer getObserver();

}

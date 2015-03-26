package bremoswing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class SwingBremoController {

	private SwingBremoModel model;

	private SwingBremo view;

	private final int delay = 500; // milliseconds

	private final ActionListener checkBremoState = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!SwingBremoModel.isBremoRunning()) {
				SwingBremoModel.calcul_end();
			}
		}
	};
	private final Timer checkTimer = new Timer(delay, checkBremoState);

	public SwingBremoController(SwingBremo swBr) {
		SetView(swBr);
		setModel(new SwingBremoModel(this));

	}

	public void SetView(SwingBremo SwBr) {

		view = SwBr;
	}

	public SwingBremo getView() {

		return view;
	}

	public void setModel(SwingBremoModel Model) {

		model = Model;
	}

	public SwingBremoModel getModel() {

		return model;
	}

	public void berechnen() {
		model.startCalcul();
	}

	public void wahl() {

		model.chooseFile();

	}

	public void stop() {
		model.stopCalcul();
	}

	public void graphic() {
		model.showGraphicFrame();
	}

	public void table() {
		model.showTableFrame();
	}

	public void help() {
		model.showHelp();
	}

	public void checkTimerstart() {
		checkTimer.start();
	}

	public void checkTimerstop() {
		checkTimer.stop();
	}

}

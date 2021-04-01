package GUI;

import javax.swing.JButton;

public class Helper {
	/**
	 * disable all buttons from parameter
	 * @param buttons
	 */
	public static void turnAllButtonsOff(JButton[] buttons) {
		for(JButton button : buttons) {
			button.setEnabled(false);
		}
	}
	/**
	 * enable all buttons from parameter
	 * @param buttons
	 */
	public static void turnAllButtonsOn(JButton[] buttons) {
		for(JButton button : buttons) {
			button.setEnabled(true);
		}
	}
}

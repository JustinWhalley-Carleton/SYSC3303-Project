package GUI;

import javax.swing.JTextArea;

public class TextManager {
	
	private static final JTextArea textPanel = GUI.textPanel;
	/**
	 * print text to scroll panel
	 * @param text
	 */
	public static synchronized void print(String text) {
		textPanel.append(text+"\n");
	}
}

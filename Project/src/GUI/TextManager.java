package GUI;

import javax.swing.JTextArea;

public class TextManager {
	
	private static final JTextArea textPanel = GUI.textPanel;
	
	public static synchronized void print(String text) {
		textPanel.append(text+"\n");
	}
}

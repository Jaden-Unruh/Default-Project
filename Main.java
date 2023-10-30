package us.akana.tools.maximoIds;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

enum InfoText {
	SELECT_PROMPT, ERROR, DONE
}

public class Main {
	
	static JFrame options;
	static JLabel info = new JLabel();
	
	static InfoText infoText;
	
	static File[] selectedFiles = new File[2];

	public static void main(String[] args) {
		openWindow();
	}
	
	private static void openWindow() {
		options = new JFrame(Messages.getString("Main.Window.Title")); //$NON-NLS-1$
		options.setSize(800, 700);
		options.setLayout(new GridBagLayout());
		options.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		options.add(new JLabel(Messages.getString("Main.Window.ASPxPrompt")), //$NON-NLS-1$
				new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(0, 0, 0, 0), 0, 0));

		JButton selectASPx = new SelectButton(0);
		options.add(selectASPx,
				new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(0, 0, 0, 0), 0, 0));

		options.add(new JLabel(Messages.getString("Main.Window.CAPrompt")), //$NON-NLS-1$
				new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(0, 0, 0, 0), 0, 0));

		JButton selectCa = new SelectButton(1);
		options.add(selectCa,
				new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(0, 0, 0, 0), 0, 0));

		JButton cancel = new JButton(Messages.getString("Main.Window.Close")); //$NON-NLS-1$
		options.add(cancel,
				new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(0, 0, 0, 0), 0, 0));

		final JButton run = new JButton(Messages.getString("Main.Window.Open")); //$NON-NLS-1$
		options.add(run,
				new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(0, 0, 0, 0), 0, 0));

		options.add(info,
				new GridBagConstraints(0, 5, 2, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(0, 0, 0, 0), 0, 0));

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (checkCorrectSelections()) {
					SwingWorker<Boolean, Void> sw = new SwingWorker<Boolean, Void>() {
						@Override
						protected Boolean doInBackground() throws Exception {
							//RUN PROGRAM
							updateInfo(InfoText.DONE);
							run.setEnabled(true);
							return true;
						}

						@Override
						protected void done() {
							try {
								get();
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.getCause().printStackTrace();
								String[] choices = { Messages.getString("Main.Window.Error.Close"), //$NON-NLS-1$
										Messages.getString("Main.Window.Error.More") }; //$NON-NLS-1$
								updateInfo(InfoText.ERROR);
								run.setEnabled(true);
								if (JOptionPane.showOptionDialog(options,
										String.format(Messages.getString("Main.Window.Error.ProblemLabel"), //$NON-NLS-1$
												e.getCause().toString()),
										Messages.getString("Main.Window.Error.Error"), JOptionPane.DEFAULT_OPTION, //$NON-NLS-1$
										JOptionPane.ERROR_MESSAGE, null, choices, choices[0]) == 1) {
									StringWriter sw = new StringWriter();
									e.printStackTrace(new PrintWriter(sw));
									JTextArea jta = new JTextArea(25, 50);
									jta.setText(String.format(Messages.getString("Main.Window.Error.FullTrace"), //$NON-NLS-1$
											sw.toString()));
									jta.setEditable(false);
									JOptionPane.showMessageDialog(options, new JScrollPane(jta),
											Messages.getString("Main.Window.Error.Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
								}
							}
						}
					};
					run.setEnabled(false);
					sw.execute();
				} else
					updateInfo(InfoText.SELECT_PROMPT);
			}
		});

		options.pack();
		options.setVisible(true);
	}
	
	/**
	 * Checks if the {@link Main#selectedFiles} are not null and are .xlsx files
	 * 
	 * @return true if both files are .xlsx
	 */
	static boolean checkCorrectSelections() {
		try {
			return isXLSX(selectedFiles[0]) && isXLSX(selectedFiles[1]);
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	/**
	 * Checks if the given file is of type XLSX (a microsoft excel workbook)
	 * 
	 * @param file the file to check
	 * @return true if the file is .xlsx
	 * @throws NullPointerException if the File is null
	 */
	static boolean isXLSX(File file) throws NullPointerException {
		return file.getName().toLowerCase().endsWith(".xlsx"); //$NON-NLS-1$
	}
	
	/**
	 * Updates the text of {@link Main#info}
	 * 
	 * @param text the new text
	 */
	static void updateInfo(InfoText text) {
		infoText = text;
		info.setText(getInfoText());
		options.pack();
	}
	
	/**
	 * The String associated with {@link Main#infoText}
	 * 
	 * @return the String currently showing in {@link Main#info}
	 * @see Main#infoText
	 */
	static String getInfoText() {
		switch (infoText) {
		case SELECT_PROMPT:
			return Messages.getString("Main.Info.SelectPrompt"); //$NON-NLS-1$
		case ERROR:
			return Messages.getString("Main.Info.Error"); //$NON-NLS-1$
		case DONE:
			return Messages.getString("Main.Info.Done"); //$NON-NLS-1$
		}
		return null;
	}

}

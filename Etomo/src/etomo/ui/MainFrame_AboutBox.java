package etomo.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

import etomo.ApplicationManager;
import etomo.process.SystemProgram;

/**
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 *
 * @author $Author$
 *
 * @version $Revision$
 *
 * <p> $Log$
 * <p> Revision 2.6  2003/11/04 20:56:11  rickg
 * <p> Bug #345 IMOD Directory supplied by a static function from ApplicationManager
 * <p>
 * <p> Revision 2.5  2003/10/31 00:00:39  rickg
 * <p> Bug# 260
 * <p>
 * <p> Revision 2.4  2003/09/09 17:15:45  rickg
 * <p> Upped version number to 0.95
 * <p>
 * <p> Revision 2.3  2003/08/20 21:59:30  rickg
 * <p> Changed version number to 0.9
 * <p>
 * <p> Revision 2.2  2003/05/14 23:33:58  rickg
 * <p> Change BETA number to 2
 * <p>
 * <p> Revision 2.1  2003/04/25 23:42:56  rickg
 * <p> Updated about box text
 * <p>
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.2.2.1  2003/01/24 18:43:37  rickg
 * <p> Single window GUI layout initial revision
 * <p>
 * <p> Revision 1.2  2002/10/07 22:31:18  rickg
 * <p> removed unused imports
 * <p> reformat after emacs trashed it
 * <p>
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */
public class MainFrame_AboutBox extends JDialog {
	public static final String rcsid =
		"$Id$";

	String vers3dmod = "unknown";
	String cpyrght3dmodLine1 = "";
	String cpyrght3dmodLine2 = "";
	JPanel pnlAbout = new JPanel();

	JButton btnOK = new JButton("OK");

	public MainFrame_AboutBox(Frame parent) {
		super(parent);
		get3dmodVersion();
		JPanel pnlRoot = (JPanel) getContentPane();
		JPanel pnlText = new JPanel();
		JPanel pnlButton = new JPanel();
		pnlRoot.setLayout(new BorderLayout());
		setTitle("About");
		setResizable(false);

		pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
		pnlAbout.setLayout(new BoxLayout(pnlAbout, BoxLayout.Y_AXIS));

		JLabel lblEtomo = new JLabel("eTomo: The IMOD Tomography GUI");
		JLabel lblVersion = new JLabel("Version 1.0.0");
		JLabel lblAuthors = new JLabel("Written by: Rick Gaudette & Sue Held");
		JLabel lbl3dmodVersion = new JLabel("IMOD Version: " + vers3dmod);
		JLabel lblCopyright1 = new JLabel(cpyrght3dmodLine1);
		JLabel lblCopyright2 = new JLabel(cpyrght3dmodLine2);

		btnOK.addActionListener(new AboutActionListener(this));

		pnlAbout.add(Box.createRigidArea(FixedDim.x0_y10));
		pnlText.add(lblEtomo);
		pnlText.add(Box.createRigidArea(FixedDim.x0_y5));
		pnlText.add(lblVersion);
		pnlText.add(Box.createRigidArea(FixedDim.x0_y10));
		pnlText.add(lblCopyright1);
		pnlText.add(Box.createRigidArea(FixedDim.x0_y5));
		pnlText.add(lblCopyright2);
		pnlText.add(Box.createRigidArea(FixedDim.x0_y10));
		pnlText.add(lblAuthors);
		pnlText.add(Box.createRigidArea(FixedDim.x0_y20));
		pnlText.add(lbl3dmodVersion);
		pnlText.add(Box.createRigidArea(FixedDim.x0_y20));
		pnlButton.add(btnOK);

		pnlAbout.add(pnlText);
		pnlAbout.add(pnlButton);
		pnlRoot.add(pnlAbout, BorderLayout.CENTER);
		pnlRoot.add(Box.createRigidArea(FixedDim.x20_y0), BorderLayout.WEST);
		pnlRoot.add(Box.createRigidArea(FixedDim.x20_y0), BorderLayout.EAST);
		pack();
	}

	/**Overridden so we can exit when window is closed*/
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}
	/**Close the dialog*/
	void cancel() {
		dispose();
	}
	/**Close the dialog on a button event*/
	public void buttonAction(ActionEvent e) {
		if (e.getSource() == btnOK) {
			cancel();
		}
	}

	/**
	 * Run 3dmod -h to version and copyright information.
	 */
	private void get3dmodVersion() {
		String imodBinPath =
			ApplicationManager.getIMODDirectory().getAbsolutePath()
				+ File.separator
				+ "bin"
				+ File.separator;
		String command = imodBinPath + "3dmod -h";
		SystemProgram threeDmod_h = new SystemProgram(command);

		threeDmod_h.run();

		String[] stdout = threeDmod_h.getStdOutput();
		if (stdout.length >= 1) {
			int idxVersion = stdout[0].indexOf("Version");
			if (idxVersion > 0) {
				String noPath = stdout[0].substring(idxVersion);
				String[] tokens = noPath.split(" ");
				if (tokens.length > 1) {
					vers3dmod = tokens[1];
				}
			}
		}
		if (stdout.length > 3) {
			cpyrght3dmodLine1 = stdout[1];
			cpyrght3dmodLine2 = stdout[2];
		}
	}

	class AboutActionListener implements ActionListener {

		MainFrame_AboutBox adaptee;

		AboutActionListener(MainFrame_AboutBox adaptee) {
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent event) {
			adaptee.buttonAction(event);
		}
	}
}

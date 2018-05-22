package medicalsystem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyInputSourceException;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class MedicalExpertMainFrame{

	// GUI components
	private static JFrame frame;
	private static OWLOntology ontology;
	private static ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private static JList<String> list; 
	private static boolean ontologyLoaded = true;

	public static void main(String[] args) {
		try {
			ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File("resources/diseases.owl"));
		}
		catch (OWLOntologyInputSourceException ooise) {
			ontologyLoaded = false;

		} catch (OWLOntologyCreationException ooce) {
			ontologyLoaded = false;
		}

		try {
			MedicalExpertMainFrame window = new MedicalExpertMainFrame();
			window.frame.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}

	} // End of main

	public MedicalExpertMainFrame() {
		initialize();
		if (!ontologyLoaded) {
			JOptionPane.showMessageDialog(frame, "Could not load ontology.");
		}
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		screenSize.getWidth();
		screenSize.getHeight();

		frame = new JFrame("Maastricht University MedExpert");
		frame.setSize(600, 400);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationByPlatform(true);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBackground( Color.WHITE );
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBackground(Color.WHITE);
		buttonPanel.setOpaque(true);
		buttonPanel.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 20));
		buttonPanel.setForeground(Color.WHITE);

		JButton dcBtn = new JButton("Disease Classification");
		dcBtn.setBackground(Color.BLACK);
		dcBtn.setOpaque(true);
		dcBtn.setBorderPainted(false);
		dcBtn.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 24));
		dcBtn.setForeground(Color.WHITE);
		dcBtn.addActionListener(new DiseaseClassificationButtonHandler());

		JButton praBtn = new JButton("Disease Comparison");
		praBtn.setBackground(Color.BLACK);
		praBtn.setOpaque(true);
		praBtn.setBorderPainted(false);
		praBtn.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 24));
		praBtn.setForeground(Color.WHITE);
		praBtn.addActionListener(new DiseaseComparisonButtonHandler());
		//praBtn.setEnabled(false);
		
		if (!ontologyLoaded) {
			dcBtn.setEnabled(false);
			praBtn.setEnabled(false);
		}

		buttonPanel.add(dcBtn);
		buttonPanel.add(praBtn);

		mainPanel.add(createTitlePanel());
		mainPanel.add(buttonPanel);

		frame.getContentPane().add(mainPanel);
		frame.pack();
		frame.setVisible(true);
	}

	private JPanel createTitlePanel() {
		JPanel panel = new JPanel();
		panel.setBackground( Color.WHITE );

		ImageIcon logo = new ImageIcon("resources/logo.png");
		JLabel lblLogo = new JLabel(logo);

		panel.add(lblLogo);
		return panel;
	}

	private class DiseaseComparisonButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			MedicalExpertComparisonResultsFrame f = new MedicalExpertComparisonResultsFrame(ontology);
			try {
				f.results_screen();
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class DiseaseClassificationButtonHandler implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			MedicalExpertClassificationResultsFrame f = new MedicalExpertClassificationResultsFrame(ontology);
			try {
				f.results_screen();
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}

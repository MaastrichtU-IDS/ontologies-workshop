package medicalsystem;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class MedicalExpertComparisonResultsFrame {
	// GUI components
	private static JFrame frame;
	private static HashMap<File, String> diseaseAnnotations = new HashMap<File, String>();
	private static File disease1;
	private static File disease2;

	public static boolean openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static void results_screen() throws OWLOntologyCreationException {
		try {
			frame.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}

	} // End of main

	public MedicalExpertComparisonResultsFrame(OWLOntology ontology) {
		initialize();
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		frame = new JFrame("UM MedExpert - Disease Comparison");
		frame.setSize(600, 400);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationByPlatform(true);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
		frame.getContentPane().setBackground(Color.WHITE);
		JPanel titlePanel = createTitlePanel();
		titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.getContentPane().add(titlePanel);
		JPanel catPanel = createDiseaseListboxes();
		catPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.getContentPane().add(catPanel);
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

	private JPanel createDiseaseListboxes() {
		JPanel panel = new JPanel(new FlowLayout());
		//panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel threeComponentsInARow = new JPanel(new FlowLayout());
		threeComponentsInARow.setBackground(Color.WHITE);

		JPanel disease1Panel = new JPanel();
		disease1Panel.setBackground(Color.WHITE);
		JPanel disease2Panel = new JPanel();
		disease2Panel.setBackground(Color.WHITE);
		JPanel compareButtonPanel = new JPanel();
		compareButtonPanel.setBackground(Color.WHITE);

		JLabel lblCategoriesBox = new JLabel("Disease 1:");
		lblCategoriesBox.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 30));
		lblCategoriesBox.setBackground(Color.WHITE);
		disease1Panel.add(lblCategoriesBox);

		JLabel lblCategoriesBox2 = new JLabel("Disease 2:");
		lblCategoriesBox2.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 30));
		lblCategoriesBox2.setBackground(Color.WHITE);
		disease2Panel.add(lblCategoriesBox2);

		// Get all diseases first
		List<File> diseases = new ArrayList<File>();
		File folder = new File("resources/data");
		File[] listOfFiles = folder.listFiles();
		try {
			if (listOfFiles.length > 0) {
				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()) {
						diseases.add(listOfFiles[i]);
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame, "There are no disease files in the directory.");
			}
		}
		catch(NullPointerException npe) {
			JOptionPane.showMessageDialog(frame, "Could not find directory with disease files.");
		}

		File [] diseasesAsFileArray = new File[diseases.size()];
		Object [] tmp = diseases.toArray();

		for (int i = 0; i < tmp.length;i++) {
			diseasesAsFileArray[i] = (File)tmp[i];
		}

		JComboBox<File> disease1List = new JComboBox<File>(diseasesAsFileArray);
		disease1Panel.add(disease1List);
		JComboBox<File> disease2List = new JComboBox<File>(diseasesAsFileArray);
		disease2Panel.add(disease2List);
		disease1List.setPreferredSize(new Dimension(250, 35));
		disease2List.setPreferredSize(new Dimension(250, 35));
		disease1List.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 24));
		disease1List.setRenderer(new MyDiseaseCategoryCellRenderer());
		disease1 = (File)disease1List.getSelectedItem();
		disease1List.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					disease1 = (File)e.getItem();
				}
			}
		});

		disease2List.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 24));
		disease2List.setRenderer(new MyDiseaseCategoryCellRenderer());
		disease2 = (File)disease2List.getSelectedItem();
		disease2List.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					disease2 = (File)e.getItem();
				}
			}
		});

		JButton compareButton = new JButton("Compare");
		compareButton.setBackground(Color.BLACK);
		compareButton.setOpaque(true);
		compareButton.setBorderPainted(false);
		compareButton.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 24));
		compareButton.setForeground(Color.WHITE);
		compareButton.addActionListener(new CompareDiseasesButtonHandler());
		compareButtonPanel.add(compareButton);

		threeComponentsInARow.add(disease1Panel);
		threeComponentsInARow.add(disease2Panel);
		threeComponentsInARow.add(compareButtonPanel);
		panel.add(threeComponentsInARow);
		panel.setBackground(Color.WHITE);
		return panel;
	}

	private static class MyDiseaseCategoryCellRenderer implements ListCellRenderer {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		private final static Dimension preferredSize = new Dimension(0, 20);
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof File) {
				File file = (File)value;
				renderer.setText(file.getName().replace(".html", ""));
			}
			renderer.setPreferredSize(preferredSize);
			return renderer;
		}
	}

	private static class MyDiseaseResultCellRenderer implements ListCellRenderer {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			renderer.setHorizontalAlignment(JLabel.LEFT);  
			renderer.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 24));
			if (value instanceof File) {
				File file = (File)value;
				String annos = diseaseAnnotations.get(file);
				if (annos.length() < 4) {
					annos = "";
				}
				String result = "<html><table border = \"0\">\r\n" + 
						"  <tr>\r\n" + 
						"    <td align=\"center\">" + (index+1) + "</td>\r\n" + 
						"    <td align=\"center\">" + file.getName().replace(".html", "") + "</td> \r\n" + 
						"    <td align=\"center\">" + annos + "</td>\r\n" + 
						"  </tr></table></html>";
				renderer.setText(result);
			}
			return renderer;
		}
	}

	private List<String> getSymptomsClasses(File f) throws IOException{
		List<String> currentTags = new ArrayList<String>();
		File input = new File("resources/data/"+f.getName());
		Document doc = Jsoup.parse(input, "UTF-8");
		Elements dataTags = doc.getElementsByTag("data");
		for (Element dataTag : dataTags) {
			Attributes attrs = dataTag.attributes();
			for (Attribute attr: attrs.asList()) {
				currentTags.add(attr.getValue());
			}
		}
		currentTags.remove(0); // Remove the disease term (just leaving the terms)
		return currentTags;
	}

	private List<String> getSymptomsOriginal(File f) throws IOException{
		List<String> currentTags = new ArrayList<String>();
		File input = new File("resources/data/"+f.getName());
		Document doc = Jsoup.parse(input, "UTF-8");
		Elements dataTags = doc.getElementsByTag("data");
		for (Element dataTag : dataTags) {
			String dataTagText = dataTag.text();
			currentTags.add(dataTagText);
		}
		currentTags.remove(0); // Remove the disease term (just leaving the terms)
		return currentTags;
	}

	private class CompareDiseasesButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			// Get symptoms of disease 1 standardised name
			List<String> symptomsDisease1Classes = new ArrayList<String>();
			List<String> symptomsDisease2Classes = new ArrayList<String>();
			new ArrayList<String>();
			new ArrayList<String>();
			try {
				symptomsDisease1Classes = getSymptomsClasses(disease1);
				// Get symptoms of disease 2 standardised name
				symptomsDisease2Classes = getSymptomsClasses(disease2);
				getSymptomsOriginal(disease1);
				getSymptomsOriginal(disease2);
			}
			catch (IOException e) {
				JOptionPane.showMessageDialog(frame, "There are no disease files in the directory.");
			}
			// Get intersection of symptoms (classes)
			symptomsDisease1Classes.retainAll(symptomsDisease2Classes);
			if (symptomsDisease1Classes.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "These diseases don't have any symptoms in common.", "Common Symptoms", JOptionPane.INFORMATION_MESSAGE, null);
			}
			else {
				String output = "<HTML><BODY BGCOLOR=#87CEFA><ul>";
				for (String symptom: symptomsDisease1Classes) {
					output += "<li>" + symptom.replace("http://www.maastrichtuniversity.nl/ids/ontologies/diseases/diseases.owl#", "") + "</li>";
				}
				output += "</ul></BODY></HTML>";
				JOptionPane.showMessageDialog(frame, output, "Common Symptoms", JOptionPane.INFORMATION_MESSAGE, null);
			}
		}
	}
}

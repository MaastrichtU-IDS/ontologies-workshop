package medicalsystem;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class MedicalExpertClassificationResultsFrame{

	// GUI components
	private static ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private static JFrame frame;
	private static OWLOntology ontology;
	//	private static ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private static OWLDataFactory df = OWLManager.getOWLDataFactory();
	private static OWLClass diseaseClass;
	private static JPanel diseaseListPanel;
	private static DefaultListModel<File> diseaseLinksListModel;
	private static DefaultListModel<File> cachedDiseaseLinksListModel;
	private static JList<File> diseaseLinksList;  
	private static HashMap<File, String> diseaseAnnotations = new HashMap<File, String>();

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

	public MedicalExpertClassificationResultsFrame(OWLOntology ontology) {
		MedicalExpertClassificationResultsFrame.ontology = ontology; 
		diseaseClass = df.getOWLClass(IRI.create(ontology.getOntologyID().getOntologyIRI().get()+"#Disease"));
		diseaseLinksListModel = new DefaultListModel<File>();
		cachedDiseaseLinksListModel = new DefaultListModel<File>();
		initialize();
	}

	private boolean isATypeOf(File f, OWLClass selectedDiseaseCategory) {
		List<String> fileOntologyTags = new ArrayList<String>();
		File input = new File("resources/data/"+f.getName());
		Document doc;
		try {
			doc = Jsoup.parse(input, "UTF-8");
			Elements dataTags = doc.getElementsByTag("data");
			for (Element dataTag : dataTags) {
				Attributes attrs = dataTag.attributes();
				for (Attribute attr: attrs.asList()) {
					fileOntologyTags.add(attr.getValue());
				}
			}

			OWLClass theDiseaseInFile = df.getOWLClass(IRI.create(fileOntologyTags.get(0)));
			Reasoner reasoner = new Reasoner(new Configuration(), ontology);
			OWLSubClassOfAxiom testSubClassOfAxiom = df.getOWLSubClassOfAxiom(theDiseaseInFile, selectedDiseaseCategory); 
			if (reasoner.isEntailed(testSubClassOfAxiom)) {
				return true;
			}
			else {
				return false;
			}

		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "Could not load disease file.");
			return true;
		}

	}

	private void getDiseasesNotUnderCategory(OWLClass selectedDiseaseCategory) {
		diseaseLinksListModel.clear();
		File folder = new File("resources/data");
		File[] listOfFiles = folder.listFiles();
		try {
			if (listOfFiles.length > 0) {
				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()) {
						if (isATypeOf(listOfFiles[i], selectedDiseaseCategory)) {
							diseaseLinksListModel.addElement(listOfFiles[i]);
						}
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
		
		//System.out.println(diseaseLinksListModel.getSize());
		if (diseaseLinksListModel.isEmpty())
			JOptionPane.showMessageDialog(frame, "Could not find any diseases in this category.");			
	}

	private String getAnnotations(File f) {
		//System.out.println(f.getName());
		String result = "[ ";
		// First get class
		if (f.isFile()) {
			OWLClass className;
			List<String> currentTags = new ArrayList<String>();
			File input = new File("resources/data/"+f.getName());
			Document doc;
			try {
				doc = Jsoup.parse(input, "UTF-8");
				Elements dataTags = doc.getElementsByTag("data");
				for (Element dataTag : dataTags) {
					String dataTagText = dataTag.text();
					Attributes attrs = dataTag.attributes();
					for (Attribute attr: attrs.asList()) {
						currentTags.add(attr.getValue());
					}
				}
				className = df.getOWLClass(IRI.create(currentTags.get(0)));
				// Check for equivalent classes
				Reasoner reasoner = new Reasoner(new Configuration(), ontology);
				Set<OWLClass> names = reasoner.getEquivalentClasses(className).getEntities();
				names.remove(className);
				names.remove(df.getOWLNothing());
				names.remove(df.getOWLThing());
				if (names.size() > 0) {
					// There are other names
					result += "NAMES: ";
					for (OWLClass ctmp: names) {
						result += man.render(ctmp) + " ";
					}
					result += "| ";
				}
				// Check if it is a cancer
				OWLClass cancerClass = df.getOWLClass(IRI.create(ontology.getOntologyID().getOntologyIRI().get()+"#Cancer"));
				OWLSubClassOfAxiom testAxiom = df.getOWLSubClassOfAxiom(className, cancerClass);
				boolean cancerTrue = false;
				if (reasoner.isEntailed(testAxiom)) {
					cancerTrue = true;
					// It is a cancer
					result += "CANCER TYPE: ";
					// Check what oma it is 
					OWLClass omaClass = df.getOWLClass(IRI.create(ontology.getOntologyID().getOntologyIRI().get()+"#Oma"));
					// Get direct subclasses of oma 
					Set<OWLClass> omaSubClasses = reasoner.getSubClasses(omaClass, true).getFlattened();
					for (OWLClass c: omaSubClasses) {
						OWLSubClassOfAxiom testAxiom2 = df.getOWLSubClassOfAxiom(className, c);
						if (reasoner.isEntailed(testAxiom2)) {
							// It is this oma
							result += man.render(c) + " ";
						}
					}

				}
				// Check which organ affected
				OWLObjectProperty originatesInProperty = df.getOWLObjectProperty(IRI.create(ontology.getOntologyID().getOntologyIRI().get()+"#originatesIn"));
				OWLClass organClass = df.getOWLClass(IRI.create(ontology.getOntologyID().getOntologyIRI().get()+"#Organ"));
				Set<OWLClass> organs = reasoner.getSubClasses(organClass, false).getFlattened();
				Set<OWLClass> affectedOrgans = new HashSet<OWLClass>();
				for (OWLClass organ: organs) {
					OWLClassExpression rhs = df.getOWLObjectIntersectionOf(diseaseClass, df.getOWLObjectSomeValuesFrom(originatesInProperty, organ));
					OWLSubClassOfAxiom testAx = df.getOWLSubClassOfAxiom(className, rhs);
					if (reasoner.isEntailed(testAx)) {
						affectedOrgans.add(organ);
					}
				}

				if (affectedOrgans.size() > 0) {
					if (cancerTrue) {
						result += "| AFFECTS: ";
						for (OWLClass org: affectedOrgans) {
							result += man.render(org) + " ";
						}
					}
					else {
						result += "AFFECTS: ";
						for (OWLClass org: affectedOrgans) {
							result += man.render(org) + " ";
						}
					}
				}
				result += "]";
			} catch (IOException e) {
				result += "]";
				JOptionPane.showMessageDialog(frame, "Could not find the data file.");
			}
		}
		return result;
	}

	private JPanel populateDiseaseLinks() {
		diseaseListPanel = new JPanel();
		diseaseListPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		diseaseListPanel.setBackground(Color.WHITE );
		diseaseLinksList = new JList<File>(diseaseLinksListModel);
		diseaseLinksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		diseaseLinksList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				if (! evt.getValueIsAdjusting()) {
					openWebpage(diseaseLinksList.getSelectedValue().toURI());
				}
			}
		});

		diseaseLinksList.setCellRenderer(new MyDiseaseResultCellRenderer());
		File folder = new File("resources/data");
		File[] listOfFiles = folder.listFiles();
		try {
			if (listOfFiles.length > 0) {
				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()) {
						diseaseLinksListModel.addElement(listOfFiles[i]);
						cachedDiseaseLinksListModel.addElement(listOfFiles[i]);
						diseaseAnnotations.put(listOfFiles[i], getAnnotations(listOfFiles[i]));
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
		
		
		if (diseaseLinksListModel.isEmpty())
			JOptionPane.showMessageDialog(frame, "No diseases in this category.");
		else
			diseaseListPanel.add(diseaseLinksList);
		
		return diseaseListPanel;
	}
	/**
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		frame = new JFrame("UM MedExpert - Disease Classification");
		frame.setSize(600, 400);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationByPlatform(true);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
		frame.getContentPane().setBackground(Color.WHITE);

		JPanel titlePanel = createTitlePanel();
		titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.getContentPane().add(titlePanel);
		JPanel catPanel = createCategoriesBox();
		catPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.getContentPane().add(catPanel);
		JPanel dlPanel = populateDiseaseLinks();
		dlPanel.setPreferredSize(new Dimension(1200, 800));
		dlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.getContentPane().add(dlPanel);

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

	private Set<OWLClass> getDiseaseCategoriesFromOntology(){
		Set<OWLClass> result = new HashSet<OWLClass>();
		Reasoner reasoner = new Reasoner(new Configuration(), ontology);

		//System.out.println(diseaseClass.getIRI());

		Set<OWLClass> direct_subclasses_of_disease = reasoner.getSubClasses(diseaseClass, true).getFlattened();
		result.addAll(direct_subclasses_of_disease);

		for (OWLClass c: direct_subclasses_of_disease) {
			Set<OWLClass> tmp = reasoner.getSubClasses(c, true).getFlattened();

			// If these classes are leaf nodes, we do not want to add them so we have to check this
			for (OWLClass c2: tmp) {
				Set<OWLClass> tmp2 = reasoner.getSubClasses(c2, true).getFlattened();
				if (tmp2.size() > 1)
					result.add(c2);
			}

			//result.addAll(tmp);
		}

		result.remove(df.getOWLNothing());
		result.remove(diseaseClass);
		result.add(diseaseClass);

		return result;
	}

	private JPanel createCategoriesBox() {
		JPanel panel = new JPanel(new FlowLayout());
		//panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JLabel lblCategoriesBox = new JLabel("Disease Categories:");
		lblCategoriesBox.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 30));
		lblCategoriesBox.setBackground(Color.WHITE);
		panel.add(lblCategoriesBox);

		Set<OWLClass> diseaseCategories = getDiseaseCategoriesFromOntology();
		OWLClass [] diseasesAsClassArray = new OWLClass[diseaseCategories.size()];
		Object [] tmp = diseaseCategories.toArray();

		for (int i = 0; i < tmp.length;i++) {
			diseasesAsClassArray[i] = (OWLClass)tmp[i];
		}

		JComboBox<OWLClass> diseaseCategoriesList = new JComboBox<OWLClass>(diseasesAsClassArray);
		diseaseCategoriesList.setPreferredSize(new Dimension(250, 35));
		diseaseCategoriesList.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 24));
		diseaseCategoriesList.setRenderer(new MyDiseaseCategoryCellRenderer());
		diseaseCategoriesList.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// Filter out diseases not under the selected category
					getDiseasesNotUnderCategory((OWLClass)e.getItem());
				}
			}
		});

		panel.add(diseaseCategoriesList);
		panel.setBackground(Color.WHITE);
		return panel;
	}

	private static class MyDiseaseCategoryCellRenderer implements ListCellRenderer {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		private final static Dimension preferredSize = new Dimension(0, 20);
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof OWLClass) {
				String val = man.render((OWLClass)value);
				if (val.equals("Disease")) {
					renderer.setText(val);
				}
				else {
					val = val.replace("Disease","");
					renderer.setText(val);
				}
			}
			renderer.setPreferredSize(preferredSize);
			return renderer;
		}
	}

	private static class MyDiseaseResultCellRenderer implements ListCellRenderer {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		//private final static Dimension preferredSize = new Dimension(0, 20);
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			//defaultRenderer.setBackground(Color.BLACK);
			
			JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			renderer.setHorizontalAlignment(JLabel.LEFT);  
			renderer.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 24));
			if (value instanceof File) {
				//System.out.println(value);
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
						"  </tr></table></html>";// + file.getName();
				//result = result.replace(".html","");
				//result = index+1 + ". " + result;
				//for (int i = 1; i <= (60 - result.length()); i++) {
			//		result += " ";
			//	}
			//	result += annos;
			//	System.out.println(result);
				renderer.setText(result);
				//renderer.setText(value.toString());
			}
			//renderer.setPreferredSize(preferredSize);
			return renderer;
		}
	}

	/*private class AddToListButtonHandler implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			List<String> selectedValues = list.getSelectedValuesList();
			for (String s: selectedValues) {
				toppingListModel.removeElement(s);
			}
		}

	}*/

} // End of HomeStylePizza class














//package pizzaexpert;
//
//import java.awt.EventQueue;
//import javax.swing.JFrame;
//
//public class PizzaExpertFrame {
//
//	private JFrame frame;
//
//	/**
//	 * @wbp.parser.entryPoint
//	 */
//
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					PizzaExpertFrame window = new PizzaExpertFrame();
//					window.frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}
//
//	/**
//	 * Create the application.
//	 */
//	public PizzaExpertFrame() {
//		initialize();
//	}
//
//	/**
//	 * Initialize the contents of the frame.
//	 */
//	private void initialize() {
//		frame = new JFrame();
//		frame.setBounds(100, 100, 840, 1000);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	}
//
//}
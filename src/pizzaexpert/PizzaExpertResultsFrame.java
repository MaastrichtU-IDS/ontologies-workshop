package pizzaexpert;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionListener;

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

public class PizzaExpertResultsFrame{

	private static final Insets bottomInsets = new Insets(10, 10, 10, 10);
	private static final Insets normalInsets = new Insets(10, 10, 0, 10);

	// GUI components
	private static ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private JRadioButton rbSmall, rbMedium, rbLarge, rbThinCrust, rbMediumCrust, rbPan;
	private JTextArea textArea;
	private static JFrame frame;
	private static List<String> toppings;
	private static String size;
	private static String base;
	private static OWLOntology ontology;
	private Set<OWLClass> superClasses;
//	private static int logicalAxiomCount;
//	private static ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
//	private static String [] unsats;
	private static OWLDataFactory df = OWLManager.getOWLDataFactory();
//	private static DefaultListModel<String> toppingListModel; 
	private static DefaultListModel<String> inferenceListModel;
//	private static JList<String> list;  
	private static JList<String> inferenceList;  
	private static OWLClassExpression toppingsIntersection;

	public static void results_screen() throws OWLOntologyCreationException {
		try {
			frame.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}

	} // End of main

	public PizzaExpertResultsFrame(OWLOntology ontology, String size, String base, List<String> toppings) {
		inferenceListModel = new DefaultListModel<String>();
		this.ontology = ontology;
		this.toppings = toppings;
		this.size = size;
		this.base = base;
		
		System.out.println("size: " + this.size);
		System.out.println("base: " + this.base);
		System.out.println();
		for (int i = 0; i < toppings.size(); i++) {
			System.out.println("topping " + i + ": " + toppings.get(i));
		}
		System.out.println();
		
		Reasoner reasoner = new Reasoner(new Configuration(), ontology);
		
		// First create expression: 
		
		OWLClass pizza = df.getOWLClass(IRI.create(ontology.getOntologyID().getOntologyIRI().get()+"#Pizza"));
		OWLObjectProperty hasTopping = df.getOWLObjectProperty(IRI.create(ontology.getOntologyID().getOntologyIRI().get()+"#hasTopping"));
		Set<OWLClass> toppingClasses = new HashSet<OWLClass>();
		Set<OWLClassExpression> hasToppingClasses = new HashSet<OWLClassExpression>();
		for (int i = 0; i < toppings.size(); i++) {
			OWLClassExpression tmp = null;
			String tmpStr = toppings.get(i) + "Topping";
			OWLClass toppingClass = df.getOWLClass(IRI.create(ontology.getOntologyID().getOntologyIRI().get()+"#"+tmpStr));
			toppingClasses.add(toppingClass);
			tmp = df.getOWLObjectSomeValuesFrom(hasTopping, toppingClass);
			hasToppingClasses.add(tmp);
		} 
		
		hasToppingClasses.add(pizza);
		
		// Now compute closure class
		OWLClassExpression closureClass = null;
		OWLClassExpression disjunctionOfToppings = df.getOWLObjectUnionOf(toppingClasses);
		closureClass = df.getOWLObjectAllValuesFrom(hasTopping, disjunctionOfToppings);
		hasToppingClasses.add(closureClass);
		
		toppingsIntersection = df.getOWLObjectIntersectionOf(hasToppingClasses);
		System.out.println("class: " + toppingsIntersection);
		superClasses = reasoner.getSuperClasses(toppingsIntersection, true).getFlattened();
		
		System.out.println("SuperClasses:");
		System.out.println("-----------");
		for (OWLClass c: superClasses) {
			System.out.println(man.render(c));
		}
		
		initialize();
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		
		double x = 0.0;
		double y = 0;
		
		x = width * 0.328125;
		y = height * 0.694444444444444444444444444444;
		
		System.out.println((int)x);
		System.out.println((int)y);
		
		frame = new JFrame("Luigi's Pizzeria Maastricht");
		frame.setSize(600, 400);
		//frame.setBounds(10, 10, 600, 1100);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationByPlatform(true);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBackground( Color.WHITE );
		int gridy = 0;

		addComponent(mainPanel, createTitlePanel(), 0, gridy++, 2, 1,
				normalInsets, GridBagConstraints.LINE_START,
				GridBagConstraints.HORIZONTAL);

		addComponent(mainPanel, createTextAreaPanel(), 0, gridy++, 2, 1,
				bottomInsets, GridBagConstraints.LINE_START,
				GridBagConstraints.HORIZONTAL);

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

	private JPanel createTextAreaPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		List<String> superClassesList = new ArrayList<String>();
		for (OWLClass c: superClasses) {
			superClassesList.add(man.render(c));
			inferenceListModel.addElement(man.render(c));
		}
		
		JLabel lblYourOrder = new JLabel("Order information:");
		lblYourOrder.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 20));
		lblYourOrder.setBackground(Color.WHITE);
		panel.add(lblYourOrder, BorderLayout.NORTH);
		panel.setBackground(Color.WHITE);
		inferenceList = new JList<String>(inferenceListModel);
		inferenceList.setBackground(Color.BLACK);
		inferenceList.setForeground(Color.WHITE);
		inferenceList.setFont(new Font("Calibri", Font.TRUETYPE_FONT, 18));
		JScrollPane scrollPane = new JScrollPane(inferenceList);
		
		inferenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		panel.add(scrollPane, BorderLayout.CENTER);		
		JButton explainButton = new JButton("Explain");
		explainButton.addActionListener(new ExplanationComputerHandler());

		explainButton.setBackground(Color.BLACK);
		explainButton.setFont(new Font("Calibri", Font.BOLD | Font.TRUETYPE_FONT, 20));
		explainButton.setForeground(Color.WHITE);
		
		panel.add(explainButton, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	private void addComponent(Container container, Component component,
			int gridx, int gridy, int gridwidth, int gridheight, Insets insets,
			int anchor, int fill) {
		GridBagConstraints gbc = new GridBagConstraints(gridx, gridy,
				gridwidth, gridheight, 1.0D, 1.0D, anchor, fill, insets, 0, 0);
		container.add(component, gbc);
	}

	private class ExplanationComputerHandler implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			// Need all the toppings and choices for base and size
			// 1. Size
			String value = inferenceList.getSelectedValue();
			OWLSubClassOfAxiom entailment = df.getOWLSubClassOfAxiom(toppingsIntersection, 
					df.getOWLClass(IRI.create(ontology.getOntologyID().getOntologyIRI().get()+"#"+value)));
			PizzaExpertExplanationFrame f = new PizzaExpertExplanationFrame(ontology, entailment);
			try {
				f.explanation_screen();
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
				
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
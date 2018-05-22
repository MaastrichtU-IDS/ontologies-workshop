package medicalsystem;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import javax.swing.DefaultListModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;


public class PatientDataGenerator {
	
	private static OWLDataFactory df;
	private static OWLOntology ontology;
	
	private static int generateRandomInteger(int min, int max){
		int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
		return randomNum;
	}
	
	private static double generateRandomDouble(int min, int max){
		double randomNum = ThreadLocalRandom.current().nextDouble(min, max);
		return randomNum;
	}
	
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

	public static boolean openWebpage(URL url) {
	    try {
	        return openWebpage(url.toURI());
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	private static Set<String> getAllDiseasesInWebpages() {
		Set<String> result = new HashSet<String>();
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		/*System.out.print("Enter disease category:");
		String category;
		Scanner scanner = new Scanner(System.in);
		category = scanner.nextLine();*/
//		System.out.println(qq);
		
		File folder = new File("resources/data");
		File[] listOfFiles = folder.listFiles();
		List<List<String>> totalTags = new ArrayList<List<String>>();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				List<String> currentTags = new ArrayList<String>();
				File input = new File("resources/data/"+listOfFiles[i].getName());
				Document doc = Jsoup.parse(input, "UTF-8");
				Elements dataTags = doc.getElementsByTag("data");
				for (Element dataTag : dataTags) {
					String dataTagText = dataTag.text();
					Attributes attrs = dataTag.attributes();
					for (Attribute attr: attrs.asList()) {
						currentTags.add(attr.getValue());
					}
				}
				totalTags.add(currentTags);
			}
		}
		
		try {
			df = OWLManager.getOWLDataFactory();
			ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File("resources/diseases.owl"));
			//OWLClass categoryClass = df.getOWLClass(IRI.create(ontology.getOntologyID().getOntologyIRI().get()+"#"+category));
			//System.out.println(categoryClass.getIRI());
			//System.out.println();
			//Set<OWLClass> itemsBelongingToCategory = new HashSet<OWLClass>();
			
			//Reasoner reasoner = new Reasoner(new Configuration(), ontology);
			Set<String> uniqueSymptoms = new HashSet<String>();
			for (List<String> tagsInThisDoc: totalTags) {
				for (int i = 2; i < tagsInThisDoc.size();i++) {
					uniqueSymptoms.add(tagsInThisDoc.get(i));
				}
				
				/*String diseaseName = tagsInThisDoc.get(0);
				OWLClass diseaseClass = df.getOWLClass(IRI.create(diseaseName));
				//System.out.println(diseaseClass);
				//System.out.println("checking...");
				OWLSubClassOfAxiom sub = df.getOWLSubClassOfAxiom(diseaseClass, categoryClass);
				if (reasoner.isEntailed(sub)) {
					itemsBelongingToCategory.add(diseaseClass);
				}*/
			}
			
			for (String c: uniqueSymptoms) {
				System.out.println(c);
			}
			
			/*for (OWLClass c: itemsBelongingToCategory) {
				System.out.println(c);
			}*/
			//Set<OWLClass> direct_classes = reasoner.getSubClasses(pizzaTopping, true).getFlattened();
			//classes.removeAll(direct_classes);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

	}
}

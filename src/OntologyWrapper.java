import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import www.ontologyutils.refinement.Covers;
import www.ontologyutils.refinement.InformationContent;
import www.ontologyutils.refinement.RefinementOperator;
import www.ontologyutils.toolbox.Utils;


public class OntologyWrapper {


	static OWLOntology ontology;
	static Covers covers;
	static RefinementOperator specialisation;
	static RefinementOperator generalisation;
	static InformationContent informationContentManager;

//	private static final OWLClassExpression TOP = new OWLDataFactoryImpl().getOWLThing();
//	private static final OWLClassExpression BOTTOM = new OWLDataFactoryImpl().getOWLNothing();
//	static Set<OWLClassExpression> leaves;
	static Set<OWLClassExpression> concepts;


	
	public static void main(String[] args){

		if (args.length < 2)  {
			System.out.println("Usage: computeInformationContent filename.onto ontology_filename.owl");
			System.exit(-1);
			
		}
		else {
			String filename_onto = args[0];
			String ontology_filename = args[1];
			System.out.println(filename_onto);
			computeInformationContent(filename_onto, ontology_filename);
		// computeInformationContent("loan_dataset.onto.new", "loan_dataset_ontology.owl");
		// computeInformationContent("paper-example.onto", "loan_example_paper.owl");
		}

	}


	//	public static int square(int input){
	//		int output = input * input;
	//		return output;
	//	}
	//	public static int power(int input, int exponent){
	//		int output,i;
	//		output=1;
	//		for(i=0;i<exponent;i++){
	//			output *= input;
	//		}
	//		return output;
	//	}

	public static void computeInformationContent(String ontologyTrepanFile, String ontologyPath)  {

		System.out.println("Welcome to computeInformationContent");
		System.out.println("ontologyTrepanFile is: " +ontologyTrepanFile);
		System.out.println("ontologyPath is: " +ontologyPath);

		ontology = Utils.newOntology(ontologyPath);

		concepts = Utils.getSubOfTBox(ontology);
		System.out.println(concepts.size());

		covers = new Covers(ontology);
		specialisation = new RefinementOperator( 
				covers.getDownCoverOperator(), 
				covers.getUpCoverOperator());

		generalisation = new RefinementOperator( 
				covers.getUpCoverOperator(), 
				covers.getDownCoverOperator());

		informationContentManager = new InformationContent(ontology, generalisation, specialisation);

		processOntologyTrepanFile(ontologyTrepanFile);

		//		Utils.getSubOfTBox(ontology).forEach(c -> {
		//
		//			double content = informationContent.informationContent(c);
		//			System.out.println("Information content of "+Utils.pretty(c.toString())+" is: "+content);
		//		});

	}

	private static void processOntologyTrepanFile(String ontologyTrepanFile) {

//		File file = new File(ontologyTrepanFile);
		Path path = Paths.get(ontologyTrepanFile);
//		
		String newContent = "";
		String strCurrentLine;
		try (BufferedReader reader = Files.newBufferedReader(path))
		{
//			reader = Files.newBufferedReader(path);
//			input = new Scanner(file);

			while((strCurrentLine = reader.readLine()) != null) {
				
//				
				String[] lineSplits = strCurrentLine.split(" ");
				String concept = lineSplits[0];
				//				String flag = lineSplits[1];
				//				String frequency = lineSplits[2];
				System.out.println("Processing....."+strCurrentLine);


				OWLClassExpression conceptInOntology;

				String computedFrequency;
				if ((computedFrequency = isRole(concept)) != null) {
//					String computedFrequency = isRole(concept);
					
					newContent += concept+" "+computedFrequency+"\n";

				}
				else {
					
					
					String nominal;
					if ((nominal = isNominal(concept)) != null) {
						conceptInOntology = isConceptInOntology(nominal);
					}
					else {
						System.out.println("here");
						conceptInOntology = isConceptInOntology(concept);
					}

					if (conceptInOntology != null) {
						System.out.println("here");

						double content = informationContentManager.informationContent(conceptInOntology);
						//					System.out.println("Information content of "+Utils.pretty(conceptInOntology.toString())+" is: "+content);
						System.out.println("Concept "+Utils.pretty(conceptInOntology.toString())+" processed...."+content);
						newContent += concept+" 1 "+content+"\n";
					}
					else {
						System.out.println("Concept not found in ontology...");
						newContent += concept+" 0 0.00\n";
					}
					
				}

			}

			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//write
//		Path path = Paths.get(ontologyTrepanFile+".new");
		try (BufferedWriter writer = Files.newBufferedWriter(path))
		{

			writer.write(newContent);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static String isRole(String concept) {

		if (concept.startsWith("is") || concept.startsWith("has")) { 

			String[] roleSplit = concept.split("_");

			//if is of the form "hasLoanAmountTerm"
			//			if (roleSplit.length==1) {

			//if role a dataproperty or an objectproperty 
			String role = roleSplit[0];
			
			double[] informationContent;
			int[] nrConceptsInDomain;
			int[] nrConceptsInRange;
			
//			System.out.println("Role is "+role);
			OWLDataProperty dataProperty = ontology.getDataPropertiesInSignature().stream().
					filter(e -> Utils.pretty(e.toString()).equals(role)).findAny().orElse(null);

			//information content of a dataproperty is the mean of the information content of the classes in its domain
			if (dataProperty != null) {

				System.out.println("DATA PROPERTY ROLE FOUND " +Utils.pretty(dataProperty.toString()));
				informationContent = new double[1];
				nrConceptsInDomain = new int[1];
				ontology.getDataPropertyDomainAxioms(dataProperty).parallelStream().forEach(a-> { 
//					System.out.println("CLASS IS "+Utils.pretty(a.getDomain().toString()));
					informationContent[0] += informationContentManager.informationContent(a.getDomain());
					nrConceptsInDomain[0]++;

				});
				informationContent[0] = informationContent[0] / (double) nrConceptsInDomain[0];
				System.out.println("Role "+Utils.pretty(dataProperty.toString())+" processed...."+informationContent[0]);
				return "1 "+informationContent[0];
			}
			
			OWLObjectProperty objectProperty = ontology.getObjectPropertiesInSignature().stream().
					filter(e -> Utils.pretty(e.toString()).equals(role)).findAny().orElse(null);
			
			if (objectProperty != null) {

//				System.out.println("OBJECT PROPERY ROLE FOUND " +Utils.pretty(objectProperty.toString()));
				informationContent = new double[1];
				nrConceptsInDomain = new int[1];
				nrConceptsInRange = new int[1];
				ontology.getObjectPropertyDomainAxioms(objectProperty).parallelStream().forEach(a-> { 
//					System.out.println("CLASS IS "+Utils.pretty(a.getDomain().toString()));
					informationContent[0] += informationContentManager.informationContent(a.getDomain());
					nrConceptsInRange[0]++;

				});
				ontology.getObjectPropertyRangeAxioms(objectProperty).parallelStream().forEach(a-> { 
//					System.out.println("CLASS IS "+Utils.pretty(a.getRange().toString()));
					informationContent[0] += informationContentManager.informationContent(a.getRange());
					nrConceptsInDomain[0]++;

				});
				informationContent[0] = informationContent[0] / ((double) nrConceptsInRange[0] + (double) nrConceptsInDomain[0] );
				System.out.println("Role "+Utils.pretty(objectProperty.toString())+" processed...."+informationContent[0]);
				return "1 "+informationContent[0];
			}
			if (dataProperty == null && objectProperty == null)
				System.out.println("Role not found in ontology....");
				return "0 0.00";
		}

		return null;
	}


	private static String isNominal(String concept) {


		String[] nominalSplit = concept.split("_");

		if (nominalSplit.length>1) {
			String nominalConcept = nominalSplit[2];
			return nominalConcept;
		}

		return null;
	}


	private static OWLClassExpression isConceptInOntology(String concept) {


		for (OWLClassExpression c : concepts) {
			if (concept.equals(Utils.pretty(c.toString())))
				return c;
		}

		return null;

	}
}
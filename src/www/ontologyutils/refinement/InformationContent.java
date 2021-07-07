package www.ontologyutils.refinement;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import www.ontologyutils.toolbox.Utils;


public class InformationContent {

//	protected static final int MAX_GENERALISATION_NR = 999;
	protected RefinementOperator up, down;

//	protected CommonGeneraliser cg; 

	protected int num_subconcepts;
//	protected ArrayList<OWLClassExpression> subconcepts; 
	protected HashMap<OWLClassExpression, Double> ICs; 
	protected final OWLClassExpression TOP = new OWLDataFactoryImpl().getOWLThing();
	protected final OWLClassExpression BOTTOM = new OWLDataFactoryImpl().getOWLNothing();

	

	public InformationContent(OWLOntology ontology, RefinementOperator up, RefinementOperator down){
		this.up = up;
		this.down = down;
		this.num_subconcepts = Utils.getSubOfTBox(ontology).size();
//		System.out.println("Number of subconcepts: " + this.num_subconcepts);
//		this.subconcepts = new ArrayList<OWLClassExpression>(this.num_subconcepts);
//
//		for (OWLClassExpression e: up.getSubConcepts()){
//			this.subconcepts.add(e);
//		}
//		similarities = new double[this.num_subconcepts][this.num_subconcepts];
//		cg = new CommonGeneraliser(up, MAX_GENERALISATION_NR);
		
		
	}

	
	
	private int count_descendants(OWLClassExpression c){
		System.out.println("Counting descendants of " + Utils.pretty(c.toString()));
		
		HashSet<OWLClassExpression> descendants = new HashSet<OWLClassExpression>();
		HashSet<OWLClassExpression> to_consider = new HashSet<OWLClassExpression>();
		to_consider.add(c);

		while (to_consider.size()>0){ 
			System.out.println("Considering  " + to_consider.size() + " descendants");
			HashSet<OWLClassExpression> to_add = new HashSet<OWLClassExpression>();
			
			for (OWLClassExpression e : to_consider) {
				Set<OWLClassExpression> spec_e;
				
//				if (down instanceof SpecialisationOperator) {
//					spec_e = ((SpecialisationOperator)down).specialise(e);
//				}
//				else { 
//					spec_e = ((DownCoverOperator)down).specialise(e);
//				}
				spec_e = down.refine(e);
				
				spec_e.remove(e); // we want all _true_ descendants of e (non-reflexive)
				spec_e.remove(c); // even in the case of two equivalent concepts, we do not want to add c to the set of the descendants of c
				
				spec_e.stream().forEach(f -> System.out.println(Utils.pretty(f.toString())));
				
				if (spec_e.contains(c))
					System.out.println(Utils.pretty(c.toString()) + " is in the downcover of " +  Utils.pretty(e.toString()));
				to_add.addAll(spec_e);
			}
			
			to_add.removeAll(descendants);
			
			descendants.addAll(to_add);
			to_consider = to_add;
		}
		
		if (descendants.contains(c)){
			System.out.println("Descendants is reflexive");
		}
		else
			System.out.println("Descendants not reflexive");
		
//		System.out.println("descendants are " + descendants.size());
		return descendants.size();
	}
	
	
	/** IC of a concept C can be defined as = 1 - log(|S(C)| + 1) / nr_subconcepts 
	 * @param c
	 * @return
	 */
	public double informationContent(OWLClassExpression c){
		return Math.log(count_descendants(c))/Math.log(count_descendants(TOP));
		// return Math.log(count_descendants(c))/Math.log(num_subconcepts);
		// return 1 - Math.log(count_descendants(c))/Math.log(count_descendants(TOP));
	}
	
//	private void compute_ICs(){
//		ICs= new HashMap<OWLClassExpression, Double>();
//		for (OWLClassExpression e: this.subconcepts){
//			double ic = this.informationContent(e);
//			System.out.println("Expression " + Utils.pretty(e.toString()) + ": IC = " + ic);
//			ICs.put(e, ic);
//		}
//	}






}

package www.ontologyutils.refinement;


import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import org.semanticweb.owlapi.model.OWLClassExpression;


public class RefinementTracker {
	protected Map<OWLClassExpression, Integer> concepts;
	protected Set<OWLClassExpression> new_concepts;
	protected int num_steps;
	protected int nearest_distance; 
	
	protected RefinementOperator refinementOperator;
	
	public RefinementTracker(Map<OWLClassExpression, Integer> concepts, RefinementOperator refinementOperator){
		this.refinementOperator=refinementOperator;
		this.concepts = concepts;
		this.new_concepts = concepts.keySet(); 
		if (this.new_concepts.size() > 0)
			this.num_steps = Collections.max(concepts.values());
		else 
			this.num_steps = 0;
	}
	
	public RefinementTracker(OWLClassExpression concept, RefinementOperator refinementOperator){
		this.refinementOperator=refinementOperator;
		this.concepts = new HashMap<OWLClassExpression, Integer>();
		this.concepts.put(concept, 0);
		this.new_concepts = new HashSet<OWLClassExpression>();
		//System.out.println("Concept added: " + this.generalisationOperator.pretty(concept) + ", " + this.concepts.get(concept));
		this.new_concepts.add(concept);
		this.num_steps = 0;
	}
	
	public void do_generalisation_step(){
		Set<OWLClassExpression> next_new = new HashSet<OWLClassExpression>();
		this.num_steps+=1;
		for (OWLClassExpression c: new_concepts){
			//System.out.println("Generalising " + this.generalisationOperator.pretty(c));
			//Set<OWLClassExpression> gen_c = generalisationOperator.generalise(c);
			Set<OWLClassExpression> gen_c;
//			if (refinementOperator instanceof GeneralisationOperator) {
//				gen_c = ((GeneralisationOperator)refinementOperator).generalise(c);
//			}
//			else { 
//				gen_c = ((UpCoverOperator)refinementOperator).generalise(c);
//			}
			gen_c = refinementOperator.refine(c);
			
			Set<OWLClassExpression> to_remove = new HashSet<OWLClassExpression>();
			
			for (OWLClassExpression c1 : gen_c)
				for (OWLClassExpression c2: concepts.keySet())
					if (c1.compareTo(c2)==0)
						to_remove.add(c1);
					
			gen_c.removeAll(to_remove);
			
			next_new.addAll(gen_c);
			for (OWLClassExpression c2 : gen_c){
				//System.out.println("Generalisation added: " + this.generalisationOperator.pretty(c2) + ", " + num_steps);
				this.concepts.put(c2, num_steps);
			}
			
		}
		
		this.new_concepts = next_new;
	}
	
	
	
	
	public boolean empty(){
		return (this.concepts.size() == 0);
	}
	
	public Integer nearest_distance(){
		return Collections.min(this.concepts.values());
	}
	
	public Set<OWLClassExpression> concepts_at_distance(Integer d){
		Set <OWLClassExpression> at_d = new HashSet<OWLClassExpression>();
		for (OWLClassExpression c : this.concepts.keySet()){ 
			if (this.concepts.get(c) == d){
				at_d.add(c);
			}
		}
		return at_d;
	}
	
	public boolean containsTop(){
		for (OWLClassExpression c : this.concepts.keySet()) {
			if (c.isTopEntity()) 
				return true;
		}
		return false;					
	}
	
	public boolean containsBottom(){
		for (OWLClassExpression c : this.concepts.keySet()) {
			if (c.isBottomEntity()) 
				return true;
		}
		return false;					
	}
	
	
	public int distanceToTop(){
		//return distanceToTop_depthFirst();
		return distanceToTop_breadthFirst();
	}
	
	public int distanceToTop_breadthFirst(){
		int i = 0;
		//System.out.println("Computing distance to top");
		while (!containsTop()){
			do_generalisation_step();
			//System.out.println("Number of concept in generalization: " + this.concepts.size());
			i+=1;
		}
		return i;
	}
	
//	public int distanceToTop_depthFirst(){
//		int to_top = 10;
//		for (OWLClassExpression c: new_concepts){
//			to_top = distanceToTop_aux(c, to_top, 0); 
//		}
//		return to_top;
//	}
	
//	public int distanceToTop_aux(OWLClassExpression c, int max_dist, int cur_dist){
//		if (cur_dist >= max_dist)
//			return max_dist; 
//		
//		if (c.isTopEntity())
//			return cur_dist; 
//		
//		int d_to_top = max_dist;
//		
//		Utils.waitForEnter("Called distanceToTop_aux(" + c + ", " + max_dist + ", "  + cur_dist + ")");
//		
//		Set<OWLClassExpression> gen_c;
//		
//		gen_c = refinementOperator.refine(c)
//		
//		for (OWLClassExpression new_c : gen_c){
//			int new_c_to_top = distanceToTop_aux(new_c, d_to_top, cur_dist+1);
//			if (new_c_to_top< d_to_top){
//				d_to_top = new_c_to_top;
//			}
//		}
//		return d_to_top;
//		
//	}

	public Set<OWLClassExpression> get_concepts_to_top() {
		
		Set<OWLClassExpression> concepts_to_top = new HashSet<OWLClassExpression>();
		//System.out.println("Computing distance to top");
		while (!containsTop()){
			concepts_to_top.addAll(concepts_from_generalisation_step());
			
		}
		
		return concepts_to_top;
	}
	
	/*public Set<OWLClassExpression> get_concepts_to_bottom() {
		
		Set<OWLClassExpression> concepts_to_bottom = new HashSet<OWLClassExpression>();
		//System.out.println("Computing distance to top");
		while (!containsBottom()){
			concepts_to_bottom.addAll(concepts_from_specialization_step());
			
		}
		
		return concepts_to_bottom;
	}*/

	private Set<OWLClassExpression> concepts_from_generalisation_step() {
		Set<OWLClassExpression> next_new = new HashSet<OWLClassExpression>();
		this.num_steps+=1;
		for (OWLClassExpression c: new_concepts){
			//System.out.println("Generalising " + this.generalisationOperator.pretty(c));
			//Set<OWLClassExpression> gen_c = generalisationOperator.generalise(c);
			Set<OWLClassExpression> gen_c;
//			if (refinementOperator instanceof GeneralisationOperator) {
//				gen_c = ((GeneralisationOperator)refinementOperator).generalise(c);
//			}
//			else { 
//				gen_c = ((UpCoverOperator)refinementOperator).generalise(c);
//			}
			
			gen_c = refinementOperator.refine(c);
			
			Set<OWLClassExpression> to_remove = new HashSet<OWLClassExpression>();
			
			for (OWLClassExpression c1 : gen_c)
				for (OWLClassExpression c2: concepts.keySet())
					if (c1.compareTo(c2)==0)
						to_remove.add(c1);
					
			gen_c.removeAll(to_remove);
			
			next_new.addAll(gen_c);
			for (OWLClassExpression c2 : gen_c){
				//System.out.println("Generalisation added: " + this.generalisationOperator.pretty(c2) + ", " + num_steps);
				this.concepts.put(c2, num_steps);
			}
			
		}
		
		this.new_concepts = next_new;
		return new_concepts;
	}
	
}

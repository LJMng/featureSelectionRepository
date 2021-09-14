package featureSelection.repository.entity.alg.liuQuickHash;

import java.util.Collection;
import java.util.HashSet;

import featureSelection.basic.model.universe.instance.Instance;

/**
 * Rough Equivalence Class entity for Liu Quick Hash Algorithm
 * 
 * @author Benjamin_L
 */
public class RoughEquivalenceClass {
	private static int num;
	private final int ID = ++num;
	
	private boolean cons;
	private int decision;
	private int instanceSize;
	private Collection<EquivalenceClass> equClasses;
	
	public RoughEquivalenceClass() {
		cons = true;
		decision = -1;
		instanceSize = 0;
		equClasses = new HashSet<>();
	}

	/**
	 * Add an {@link EquivalenceClass} into the rough equivalence class
	 * 
	 * @param e
	 * 		An Equivalence Class to be added.
	 */
	public void addAnEquivalenceClass(EquivalenceClass e) {
		if (instanceSize ==0) {
			decision = e.decision();
			cons = e.cons();
		}else {
			if (cons && (decision!=e.decision() || !e.cons()))
				cons = false;
		}
		equClasses.add(e);
		instanceSize += e.instanceSize();
	}
	
	/**
	 * Get if all {@link Instance}s in the set share the same decision value
	 * 
	 * @return true if the rough equivalence class is consistent.
	 */
	public boolean cons() {
		return cons;
	}
	
	/**
	 * Get the decision value. (if {@link #cons()} returns false, return one of the values.)
	 * 
	 * @return Decision value in int.
	 */
	public int decision() {
		return decision;
	}

	/**
	 * Get the {@link EquivalenceClass}es in this rough equivalence class.
	 * 
	 * @return A collection of {@link EquivalenceClass}es.
	 */
	public Collection<EquivalenceClass> equivalenceClasses() {
		return equClasses;
	}
	
	/**
	 * Get the size of {@link Instance}s.
	 * 
	 * @return An int value.
	 */
	public int instanceSize() {
		return instanceSize;
	}
	
	public String toString() {
		return "RoughEqu-"+ID;
	}
}
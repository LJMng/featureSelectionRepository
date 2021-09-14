package featureSelection.repository.algorithm.alg.conflictDecrease;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.conflictDecreaseReduction.SignificancePack;
import featureSelection.repository.entity.alg.conflictDecreaseReduction.InstanceRepresentative;

import java.util.*;

/**
 * Algorithm repository of ConflictDecrease, implemented based on papers:
 * <ul>
 *     <li><a href="http://cjc.ict.ac.cn/eng/qwjse/view.asp?id=3504">"An Efficient Attribute
 *     		Reduction Algorithm Based on Conflict Region"</a> by Ge Hao, Li Longshu, Yang
 *     		Chuanjian
 *     </li>
 *     <li><a href="http://www.sysengi.com/EN/abstract/abstract110260.shtml">"Attribute reduction
 *     		Algorithm based on Conflict Region Decreasing"</a> by Ge Hao, Li Longshu, Yang
 *     		Chuanjian
 *     </li>
 * </ul>
 * 
 * @author Benjamin_L
 */
public class ConflictRegionDecreaseAlgorithm {
	
	/* ----------------------------------------- Complete ----------------------------------------- */
	
	/**
	 * Obtain Equivalence Classes induced by the given attributes.
	 * 
	 * @param <Ins>
	 * 		Type of {@link Instance}. In this case, {@link InstanceRepresentative}.
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param attributes
	 * 		All attributes of {@link Instance} (Starts from 1) or <code>null</code> if
	 * 		auto-analyzing.
	 * @return A {@link Set} of equivalent class {@link Ins} {@link Set}.
	 */
	@SuppressWarnings("unchecked")
	public static <Ins extends Instance> Set<Set<Ins>> equivalenceClass(
			Collection<Ins> instances, int[] attributes
	){
		if(instances.size()==0){
			return Collections.emptySet();
		}
		
		Object[] tempInstances = instances.toArray();
		if (attributes==null) {
			// Use all attributes.
			attributes = new int[((Ins)tempInstances[0]).getValueLength()-1];
			for (int i=0; i<attributes.length; i++){
				attributes[i] = i+1;
			}
		}else if (attributes.length==0) {
			Set<Set<Ins>> equClass = new HashSet<>();
			Set<Ins> sub = new HashSet<>();
			sub.addAll(instances);
			equClass.add(sub);
			return equClass;
		}
		
		int pos, attrValue;
		Object[] sortedInstances = null;
		Map<Integer,Integer> equCounter = new HashMap<>(), countPos = new HashMap<>();
		// Loop over all attributes
		for (int attr : attributes) {
			// Initiate countPos
			countPos.clear();
			// Count the Equivalence Classes based on the current attribute
			if (equCounter.size()!=0)	equCounter.clear();
			for (Object ins : tempInstances) {
				attrValue = ((Ins) ins).getAttributeValue(attr);
				if (equCounter.containsKey(attrValue))	equCounter.replace(attrValue, equCounter.get(attrValue)+1);
				else									equCounter.put(attrValue, 1);
			}
			// Confirm the beginning positions of each attribute value in SortedInstances
			pos = 0;
			for (int key : equCounter.keySet()) {
				countPos.put(key, pos);
				pos += equCounter.get(key);
			}
			// Loop over instances in verse and allocate into sortedInstances
			sortedInstances = new Object[instances.size()];
			for (int i=tempInstances.length-1; i>=0; i--) {
				// Find pos in countPos base on the attribute value of instance[i]
				attrValue = ((Ins) tempInstances[i]).getAttributeValue(attr);
				// Find the begining of the position in SortedUniverse
				pos = countPos.get(attrValue);
				// Locate the precise position and save into SortedUniverses
				sortedInstances[pos] = (Ins) tempInstances[i];
				countPos.put(attrValue, countPos.get(attrValue)+1);
			}
			tempInstances = sortedInstances;
		}
		countPos =  null;		// Release space
		equCounter = null;		// Release space
		tempInstances = null;	// Release space
		// Initiate
		int s=0;
		Set<Ins> equClass = new HashSet<>();
		Ins instance = (Ins) sortedInstances[s];
		equClass.add(instance);
		// Sort instances into Equivalence Classes
		Ins cmp = instance;
		Set<Set<Ins>> equClasses = new HashSet<>();
		for (int i=1; i<sortedInstances.length; i++) {
			instance = (Ins) sortedInstances[i];
			for (int attr : attributes) {
				if (cmp.getAttributeValue(attr)!=instance.getAttributeValue(attr)) {
					equClasses.add(equClass);
					equClass = new HashSet<>();
					cmp = instance;
					break;
				}
			}
			equClass.add(instance);
		}
		equClasses.add(equClass);
		cmp = null;			// Release space
		instance = null;	// Release space
		equClass = null;	// Release space
		// return Equivalence Classes
		return equClasses;
	}
	/**
	 * Obtain Equivalence Classes induced by the given attributes.
	 * 
	 * @param <Ins>
	 * 		Type of {@link Instance}. In this case, {@link InstanceRepresentative}.
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param attributes
	 * 		All attributes of {@link Instance} (Starts from 1) or <code>null</code> if
	 * 		auto-analyzing.
	 * @return A {@link Set} of equivalent class {@link Ins} {@link Set}.
	 */
	@SuppressWarnings("unchecked")
	public static <Ins extends Instance> Set<Set<Ins>> equivalenceClassByAttributeList(
			Collection<Ins> instances, List<Integer> attributes
	){
		if(instances.size()==0){
			return Collections.emptySet();
		}
		
		Object[] tempInstances = instances.toArray();
		if (attributes==null) {
			// Use all attributes.
			attributes = new ArrayList<>(((Ins)tempInstances[0]).getValueLength()-1);
			for (int i=0; i<attributes.size(); i++){
				attributes.add(i+1);
			}
		}else if (attributes.size()==0) {
			Set<Set<Ins>> equClasses = new HashSet<>();
			Set<Ins> equClass = new HashSet<>();
			equClass.addAll(instances);
			equClasses.add(equClass);
			return equClasses;
		}
		
		int pos, attrValue;
		Object[] sortedInstances = null;
		Map<Integer,Integer> equCounter = new HashMap<>(), countPos = new HashMap<>();
		// Go through attributes
		for ( int attr : attributes ) {
			// Initiate countPos
			countPos.clear();
			// Count the Equivalence Classes based on the current attribute
			if (equCounter.size()!=0)	equCounter.clear();
			for (Object u : tempInstances) {
				attrValue = ((Ins)u).getAttributeValue(attr);
				if (equCounter.containsKey(attrValue))	equCounter.replace(attrValue, equCounter.get(attrValue)+1);
				else									equCounter.put(attrValue, 1);
			}
			// Confirm the beginning positions of each attribute value in sortedInstances
			pos = 0;
			for (int key : equCounter.keySet()) {
				countPos.put(key, pos);
				pos += equCounter.get(key);
			}
			// Go through universes in verse and allocate into sortedInstances
			sortedInstances = new Object[instances.size()];
			for ( int i=tempInstances.length-1; i>=0; i-- ) {
				// Find pos in countPos base on the attribute value of universes[i]
				attrValue = ((Ins)tempInstances[i]).getAttributeValue(attr);
				// Find the begining of the position in SortedUniverse
				pos = countPos.get(attrValue);
				// Locate the precise position and save into SortedUniverses
				sortedInstances[pos] = (Ins) tempInstances[i];
				countPos.put(attrValue, countPos.get(attrValue)+1);
			}
			tempInstances = sortedInstances;
		}
		countPos =  null;		// Release space
		equCounter = null;		// Release space
		tempInstances = null;	// Release space
		// Initiate
		int s=0;
		Set<Ins> equClass = new HashSet<>();
		Ins instance = (Ins) sortedInstances[s];
		equClass.add(instance);
		// Sort universes into Equivalent Classes
		Ins cmp = instance;
		Set<Set<Ins>> equClasses = new HashSet<>();
		for (int i=1; i<sortedInstances.length; i++) {
			instance = (Ins) sortedInstances[i];
			for (int attr : attributes) {
				if (cmp.getAttributeValue(attr)!=instance.getAttributeValue(attr)) {
					equClasses.add(equClass);
					equClass = new HashSet<>();
					cmp = instance;
					break;
				}
			}
			equClass.add(instance);
		}
		equClasses.add(equClass);
		cmp = null;			// Release space
		instance = null;	// Release space
		equClass = null;	// Release space
		// return Equivalence Classes
		return equClasses;
	}

	/**
	 * Obtain the conflict region in {@link Instance}s induced by attributes
	 * 
	 * @param <Ins>
	 * 		Type of {@link Instance}. In this case, {@link InstanceRepresentative}.
	 * @param instances
	 * 		A list of {@link Instance}.
	 * @param attributes
	 * 		All attributes of {@link Instance} (Starts from 1) or <code>null</code> if
	 * 		auto-analyzing.
	 * @return A {@link Set} of {@link Instance} as conflict region.
	 */
	public static <Ins extends Instance> Set<Set<Ins>> conflictRegion(
			Collection<Ins> instances, int[] attributes
	){
		if( instances.size()==0 ) {
			return Collections.emptySet();
		}else if (attributes==null) {
			// Use all attributes.
			attributes = new int[instances.iterator().next().getValueLength()-1];
			for (int i=0; i<attributes.length; i++){
				attributes[i] = i+1;
			}
		}

		Set<Set<Ins>> conflict, equClasses;
		// Initiate ConSet(P).
		conflict = new HashSet<>();
		// Get the Equivalence Class
		equClasses = equivalenceClass(instances, attributes);
		// Check if instances in class are equal in Decision value, combine into conflict if not.
		int decision;
		EquLoop : 
		for (Set<Ins> equClass : equClasses) {
			if (equClass.size()==1)	continue;
			decision = -2;
			for (Ins ins : equClass) {
				if (ins instanceof InstanceRepresentative) {
					if (decision!=-2){
						if ( ((InstanceRepresentative) ins).getDecision()!=decision) {
							conflict.add(equClass);
							continue EquLoop;
						}
					}else {
						decision=((InstanceRepresentative) ins).getDecision();
						continue;
					}
				}else {
					if (decision!=-2){
						if ( ins.getAttributeValue(0)!=decision) {
							conflict.add(equClass);
							continue EquLoop;
						}
					}else {
						decision=ins.getAttributeValue(0);
						continue;
					}
				}
			}
		}
		return conflict;
	}
	/**
	 * Obtain the conflict region in {@link Instance}s induced by attributes
	 * 
	 * @param <Ins>
	 * 		Type of {@link Instance}. In this case, {@link InstanceRepresentative}.
	 * @param instances
	 * 		A list of {@link Instance}.
	 * @param attributes
	 * 		All attributes of {@link Instance} (Starts from 1) or <code>null</code> if
	 * 		auto-analyzing.
	 * @return A {@link Set} of {@link Instance} as conflict region.
	 */
	public static <Ins extends Instance> Set<Set<Ins>> conflictRegion(
			Collection<Ins> instances, List<Integer> attributes
	){
		if( instances.size()==0 ) {
			return Collections.emptySet();
		}else if (attributes==null) {
			attributes = new ArrayList<>(instances.iterator().next().getValueLength()-1);
			for (int i=0; i<attributes.size(); i++){
				attributes.add(i+1);
			}
		}
		
		Set<Set<Ins>> conflict, equClasses;
		// Initiate ConSet(P).
		conflict = new HashSet<>();
		// Get the Equivalent Class
		equClasses = equivalenceClassByAttributeList(instances, attributes);
		// Check if universes in class are equal in Decison value, combine into conflict if not.
		int decision;
		EquLoop : 
		for (Set<Ins> equClass : equClasses) {
			if (equClass.size()==1)	continue;
			decision = -2;
			for (Ins ins : equClass) {
				if ( ins instanceof InstanceRepresentative) {
					if (decision!=-2){
						if ( ((InstanceRepresentative) ins).getDecision()!=decision) {
							conflict.add(equClass);
							continue EquLoop;
						}
					}else {
						decision=((InstanceRepresentative) ins).getDecision();
						continue;
					}
				}else {
					if (decision!=-2){
						if ( ins.getAttributeValue(0)!=decision) {
							conflict.add(equClass);
							continue EquLoop;
						}
					}else {
						decision=ins.getAttributeValue(0);
						continue;
					}
				}
			}
		}
		return conflict;
	}

	/**
	 * Obtain the conflict region in {@link Instance}s by conflict region of P and C.
	 * ( Red < R <= C )
	 * 
	 * @param <Ins>
	 * 		Type of {@link Instance}. In this case, {@link InstanceRepresentative}.
	 * @param conflictRedClass
	 * 		The Equivalence Class of conflict region of P sorted by reduct attributes.
	 * @param conflictC
	 * 		The conflict region of C.
	 * @param incAttr
	 * 		An attribute array whose attributes are in R - Red . ( Red < R <= C ) / incremental
	 * 		attribute.
	 * @return A set of Universes as Conflict Region.
	 */
	public static <Ins extends Instance> Set<Set<Ins>> conflictRegionAdvanced(
			Set<Set<Ins>> conflictRedClass, Set<Set<Ins>> conflictC, int...incAttr
	){
		Set<Set<Ins>> conflictR = new HashSet<>(), increment = new HashSet<>();
		if (conflictC!=null) {
			// ConSet(Red out of C) = ConSet(Red) - ConSet(C){x|ConSet(Red).containsAll(x)}	( ConSet'(Red) )
			// ConSet(R) = ConSet(Red) - ConSet(Red out of C) = ConSet(Red) - ConSet'(Red)
			Set<Set<Ins>>  conflictRedFilter = new HashSet<>();
			if(conflictC.size()==0) {
				conflictRedFilter.addAll(conflictRedClass);
			}else {
				OuterLoop:
				for (Set<Ins> each : conflictRedClass) {
					InstanceLoop:
					for (Ins ins : each) {
						for ( Set<Ins> c_sub : conflictC ) {
							if (c_sub.contains(ins)) {
								continue InstanceLoop;
							}else {
								conflictRedFilter.add(each);	// ConSet'(Red)
								continue OuterLoop;
							}
						}
					}
					conflictR.add(each);	// ConSet(R)
				}
			}
			// Obtain the Equivalence Class of (R out of Red, R-Red) based on the incAttr
			for (Set<Ins> sub : conflictRedFilter){
				increment.addAll(equivalenceClass(sub, incAttr));
			}
		}else {
			for (Set<Ins> sub : conflictRedClass){
				increment.addAll(equivalenceClass(sub, incAttr));
			}
		}
		// Loop over Equivalence Class of (R out of Red, R-Red) and update ConSet(R)
		int decision;
		EquLoop : 
		for (Set<Ins> sub : increment) {
			if (sub.size()==1)	continue;
			decision = -2;
			for (Instance ins : sub) {
				if (ins instanceof InstanceRepresentative) {
					if (decision!=-2){
						if (((InstanceRepresentative) ins).getDecision()!=decision) {
							conflictR.add(sub);
							continue EquLoop;
						}
					}else {
						decision=((InstanceRepresentative) ins).getDecision();
						continue;
					}
				}else {
					if (decision!=-2){
						if ( ins.getAttributeValue(0)!=decision) {
							conflictR.add(sub);
							continue EquLoop;
						}
					}else {
						decision=ins.getAttributeValue(0);
						continue;
					}
				}
			}
		}
		// Return ConSet(R)
		return conflictR;
	}
	/**
	 * Obtain the conflict region in {@link Instance}s by conflict region of P and C.
	 * ( Red < R <= C )
	 * 
	 * @param <Ins>
	 * 		Type of {@link Instance}. In this case, {@link InstanceRepresentative}.
	 * @param conflictRedClass
	 * 		The Equivalent Class of conflict region of P sorted by reduct attributes.
	 * @param conflictC
	 * 		The conflict region of C.
	 * @param incAttr
	 * 		An attribute {@link List} whose attributes are in R - Red . ( Red < R <= C ) /
	 * 		incremental attribute.
	 * @return A {@link Set} of {@link Instance} as Conflict Region.
	 */
	public static <Ins extends Instance> Set<Set<Ins>> conflictRegionAdvanced(
			Set<Set<Ins>> conflictRedClass, Set<Set<Ins>> conflictC, List<Integer> incAttr
	){
		Set<Set<Ins>> conflictR = new HashSet<>(), increment = new HashSet<>();
		if (conflictC!=null) {
			// ConSet(Red out of C) = ConSet(Red) - ConSet(C){x|ConSet(Red).containsAll(x)}	( ConSet'(Red) )
			// ConSet(R) = ConSet(Red) - ConSet(Red out of C) = ConSet(Red) - ConSet'(Red)
			Set<Set<Ins>>  conflictRedFilter = new HashSet<>();
			if(conflictC.size()==0) {
				conflictRedFilter.addAll(conflictRedClass);
			}else {
				OuterLoop :
				for (Set<Ins> sub : conflictRedClass) {
					InstanceLoop :
					for (Ins ins : sub) {
						for (Set<Ins> c_sub : conflictC) {
							if (c_sub.contains(ins)) {
								continue InstanceLoop;
							}else {
								conflictRedFilter.add(sub);	// ConSet'(Red)
								continue OuterLoop;
							}
						}
					}
					conflictR.add(sub);	// ConSet(R)
				}
			}
			// Obtain the Equivalence Class of (R out of Red, R-Red) based on the incAttr
			for (Set<Ins> sub : conflictRedFilter){
				increment.addAll(equivalenceClassByAttributeList(sub, incAttr));
			}
		}else {
			for (Set<Ins> sub : conflictRedClass){
				increment.addAll(equivalenceClassByAttributeList(sub, incAttr));
			}
		}
		// Loop over Equivalence Class of (R out of Red, R-Red) and update ConSet(R)
		int decision;
		EquLoop : 
		for (Set<Ins> sub : increment) {
			if (sub.size()==1)	continue;
			decision = -2;
			for (Instance ins : sub) {
				if (ins instanceof InstanceRepresentative) {
					if (decision!=-2){
						if (((InstanceRepresentative) ins).getDecision()!=decision) {
							conflictR.add(sub);
							continue EquLoop;
						}
					}else {
						decision=((InstanceRepresentative) ins).getDecision();
						continue;
					}
				}else {
					if (decision!=-2){
						if ( ins.getAttributeValue(0)!=decision) {
							conflictR.add(sub);
							continue EquLoop;
						}
					}else {
						decision=ins.getAttributeValue(0);
						continue;
					}
				}
			}
		}
		// Return ConSet(R)
		return conflictR;
	}

	
	/**
	 * Get the core.
	 * 
	 * @param <Ins>
	 * 		Type of {@link Instance}. In this case, {@link InstanceRepresentative}.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param conflictC
	 * 		A {@link Set} of {@link Instance} {@link Set}. Global conflict region.
	 * @param attributes
	 * 		All attributes of {@link Instance} (Starts from 1).
	 * @return A {@link List} of int value as core.
	 */
	public static <Ins extends Instance> List<Integer> core(
			Collection<Ins> instances, Set<Set<Ins>> conflictC, int[] attributes
	){
		// Initiate
		List<Integer> core = new LinkedList<>();
		// Get the conflict region of C : conflictC
		Set<Set<Ins>> subConflict;
		int[] examAttr = new int[attributes.length-1];
		for (int i=0; i<examAttr.length; i++)	examAttr[i] = attributes[i+1];
		for (int i=0; i<attributes.length; i++) {
			// Obtain the sub conflict region
			subConflict = conflictRegion(instances, examAttr);
			// If |Sub Conflict| > |conflict|, add attribute into core
			if (ConflictRegionDecreaseUtils.instanceSize(conflictC) !=
				ConflictRegionDecreaseUtils.instanceSize(subConflict)
			)	core.add(attributes[i]);
			// Update exam attributes
			if (i!=attributes.length-1)	examAttr[i] = attributes[i];
		}
		// Return core
		return core;
	}

	
	/**
	 * Get the most significant attribute.
	 * 
	 * @param <Ins>
	 * 		Type of {@link Instance}. In this case, {@link InstanceRepresentative}.
	 * @param red
	 * 		The reduct attributes.
	 * @param attributes
	 * 		All attributes of {@link Instance} (Starts from 1).
	 * @param conflictRed
	 * 		The reduct conflict region.
	 * @param conflictC
	 * 		The Global conflict region.
	 * @return A {@link SignificancePack}.
	 */
	public static <Ins extends Instance> SignificancePack<Ins> mostSignificance(
			List<Integer> red, int[] attributes, Set<Set<Ins>> conflictRed,
			Set<Set<Ins>> conflictC
	) {
		int sigAttr = -1, sig, maxSize = 0;
		Set<Set<Ins>> conflictSig, maxConflict = null;
		// Loop over every attributes that are not in Red, calculate its sig :
		//  |ConSet(R)|-|ConSet(R U a)|
		for (int attr : attributes) {
			if (red.contains(attr))	continue;
			conflictSig = conflictRegionAdvanced(conflictRed, conflictC, attr);
			int conRedSize = ConflictRegionDecreaseUtils.instanceSize(conflictRed),
				conSigSize = ConflictRegionDecreaseUtils.instanceSize(conflictSig);
			sig = conRedSize - conSigSize;
			if ( maxConflict==null || sig > maxSize ) {
				maxConflict = conflictSig;
				maxSize =  sig;
				sigAttr = attr;
			}
		}
		return new SignificancePack<Ins>().setAttribute(sigAttr).setConflictR(maxConflict);
	}

	
	/**
	 * Reduction Procedures
	 * 
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param attributes
	 * 		All attributes of {@link Instance}.(Starts from 1)
	 * @return A {@link Integer} {@link} as reduct attributes.
	 */
	public static List<Integer> reduction(List<Instance> instances, int[] attributes){
		int attributeLength = instances.iterator().next().getValueLength()-1;
		if (attributes==null) {
			attributes = new int[attributeLength];
			for (int i=0; i<attributes.length; i++)	{
				attributes[i] = i+1;
			}
		}
		
		// Get Core.
		Set<Set<Instance>> conflictC = conflictRegion(instances, attributes);
		List<Integer> core = core(instances, conflictC, attributes), red = new LinkedList<Integer>(core);
		if (red.size()!=0 && red.size()==attributeLength){
			return red;
		}
		
		// Get ConSet(R)
		Set<Set<Instance>> conflictRed;
		if (red.size()==0) {
			Set<Instance> sub = new HashSet<>(instances);
			conflictRed = new HashSet<>();
			conflictRed.add(sub);
		}else {
			conflictRed = conflictRegion(instances, red);
		}
		// Loop and get sig.
		SignificancePack<Instance> pack;
		int sigAttr;
		while (true) {
			if (ConflictRegionDecreaseUtils.instanceSize(conflictC) ==
				ConflictRegionDecreaseUtils.instanceSize(conflictRed)
			) {
				break;
			}
			// Get sig
			pack = mostSignificance(red, attributes, conflictRed, conflictC);
			sigAttr = pack.getAttribute();
			// Update Red
			red.add(sigAttr);
			// Update ConSet(R)
			conflictRed = pack.getConflictR();
		}
		
		// Inspection
		int examAttr;
		Set<Set<Instance>> conflictR;
		Set<Set<Instance>> conflictCore = conflictRegion(instances, core);
		for (int i=0; i<red.size(); i++) {
			if (core.contains(red.get(i)))	continue;
			examAttr = red.get(i);
			red.remove(i);
			conflictR = conflictRegionAdvanced(conflictCore, conflictC, red);
			if ( ConflictRegionDecreaseUtils.instanceSize(conflictR) !=
				 ConflictRegionDecreaseUtils.instanceSize(conflictC)
			) red.add(i, examAttr);
		}//*/
		return red;//*/
	}

	
	/* ---------------------------------------------- Compress ---------------------------------------------- */
	
	public static final int CONFLICT_DECISION = -1;
	
	/**
	 * Obtain compressed instance representatives
	 * 
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param attributes
	 * 		All attributes of {@link Instance} (Starts from 1).
	 * @return A {@link Set} of {@link InstanceRepresentative}.
	 */
	public static Set<InstanceRepresentative> compressedInstances(
			Collection<Instance> instances, int[] attributes
	){
		Set<InstanceRepresentative> reps = new HashSet<>();
		Set<Set<Instance>> equClasses = equivalenceClass(instances, attributes);
		
		int decision;
		InstanceRepresentative rep;
		Iterator<Instance> iterator;
		for (Set<Instance> equClass : equClasses) {
			iterator = equClass.iterator();
			rep = new InstanceRepresentative(null).setInstanceRep(iterator.next());
			decision = rep.getUniverse().getAttributeValue(0);
			while ( iterator.hasNext() ) {
				if ( iterator.next().getAttributeValue(0)!=decision ) {
					decision = ConflictRegionDecreaseAlgorithm.CONFLICT_DECISION;
					break;
				}
			}
			rep.setDecision(decision);
			reps.add(rep);
		}
		return reps;
	}
	
	
	/**
	 * Get the core
	 * 
	 * @param <Ins>
	 * 		Type of {@link Instance}. In this case, {@link InstanceRepresentative}.
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param attributes
	 * 		All attributes of {@link Instance} (Starts from 1).
	 * @return A {@link Integer} {@link List} as core attributes.
	 */
	public static <Ins extends Instance> List<Integer> coreOfCompressed(
			Collection<Ins> instances, int[] attributes
	){
		// Initiate
		List<Integer> core = new LinkedList<>();
		// Get the conflict region of C : conflictC
		Set<Set<Ins>> subConflict;
		int[] examAttr = new int[attributes.length-1];
		for (int i=0; i<examAttr.length; i++)	examAttr[i] = attributes[i+1];
		for (int i=0; i<attributes.length; i++) {
			// Obtain the sub conflict region
			subConflict = conflictRegion(instances, examAttr);
			// If |Sub Conflict| != 0, add attribute into core
			if (subConflict.size()!=0)	core.add(attributes[i]);
			
			// Update exam attributes
			if (i!=attributes.length-1)	examAttr[i] = attributes[i];
		}
		// Return core
		return core;
	}

	
	/**
	 * Reduction Procedures by Compressed Universe reps.
	 * 
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @return A {@link Integer} {@link} as reduct attributes.
	 */
	public static List<Integer> reductionByCompression(List<Instance> instances, int[] attributes){
		int attributeLength = instances.iterator().next().getValueLength()-1;
		if (attributes==null) {
			attributes = new int[attributeLength];
			for (int i=0; i<attributes.length; i++){
				attributes[i] = i+1;
			}
		}
		
		// Get compressed universe representatives
		Set<InstanceRepresentative> reps = compressedInstances(instances, attributes);
		
		// Get Core
		List<Integer> core = coreOfCompressed(reps, attributes), 
					  red = new LinkedList<>(core);
		if (red.size()!=0 && red.size()==attributeLength){
			return red;
		}
		
		// Get ConSet(Red)
		Set<Set<InstanceRepresentative>> conflictRed;
		if (red.size()==0 ) {
			Set<InstanceRepresentative> sub = new HashSet<>(reps);
			conflictRed = new HashSet<>();
			conflictRed.add(sub);
		}else {
			conflictRed = conflictRegion(reps, red);
		}
		
		// Loop and get sig.
		SignificancePack<InstanceRepresentative> pack;
		int sigAttr;
		while (true) {
			if (conflictRed.size() == 0)	break;
			// Get sig
			pack = mostSignificance(red, attributes, conflictRed, null);
			sigAttr = pack.getAttribute();
			// Update Red
			red.add(sigAttr);
			// Update ConSet(R)
			conflictRed = pack.getConflictR();
		}//*/
		
		// Inspection
		int examAttr;
		Set<Set<InstanceRepresentative>> conflictR;
		Set<Set<InstanceRepresentative>> conflictCore = conflictRegion(reps, core);
		for ( int i=0; i<red.size(); i++) {
			if ( core.contains(red.get(i)) )	continue;
			examAttr = red.get(i);
			red.remove(i);
			conflictR = conflictRegionAdvanced(conflictCore, null, red);
			if (ConflictRegionDecreaseUtils.instanceSize(conflictR) != 0) red.add(i, examAttr);
		}
		return red;
	}
}
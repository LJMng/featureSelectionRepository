package featureSelection.repository.algorithm.alg.xu;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.xu.LinkableDeque;
import featureSelection.repository.entity.alg.xu.PositiveNegativePackage;
import featureSelection.repository.entity.alg.xu.SignificancePackage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Algorithm repository of Xu-tAlgorithm, which based on the paper
 * <a href="http://www.cnki.com.cn/Article/CJFDTotal-JSJX200603005.htm">
 * A Quick Attribute Reduction Algorithm with Complexity of max(O(|C||U|), O(|C|^2|U/C|))</a> by 
 * Xu Zhangyan, Liu Zuopeng, Yang Bingru, Song Wei.
 * 
 * @author Benjamin_L
 */
public class XuAlgorithm {
	
	/**
	 * Obtain equivalence classes with {@link Instance}s sorted by the given attributes.
	 * 
	 * @param instances
	 * 		A {@link List} of {@link Instance}s.
	 * @param attributes
	 * 		Attributes of {@link Instance}.(Starts from 1) or <code>null</code> if using all
	 * 	    conditional attributes.
	 * @return A {@link Collection} of {@link Instance}s {@link Collection} as equivalence classes.
	 */
	public static Collection<Collection<Instance>> getEquivalenceClassByAttributes(
			Collection<Instance> instances, int...attributes
	) {
		// Use the 1st instance as instance representative of equivalence class.
		Instance insRep = instances.iterator().next();
		attributes = XuUtils.filterAttributes(insRep, attributes);
		if (attributes==null) {
			return new HashSet<>();
		}else if (attributes.length==0) {
			//If attribute is empty, combine all instances into a set.
			Collection<Collection<Instance>> equClass = new HashSet<>(1);
			equClass.add(instances instanceof Set? instances : new HashSet<>(instances));
			return equClass;
		}else{
			// Loop over to obtain min & max attribute value to get (min,max) of each attribute
			int[][] min_max = getMinMax(instances, attributes);
			// Loop over |p| attributes
			LinkableDeque<Instance> deque = null;
			// Build an attribute-based sorted collector
			LinkableDeque<Instance>[] allocater;
			try {
				allocater = getAllocatedUniverseDeques(instances.iterator(),attributes[0],
														min_max[0][0],min_max[0][1]);
				deque = linkAllocatedDeques(allocater);
				for (int i=1; i<attributes.length; i++) {
					allocater = getAllocatedUniverseDeques(
									deque.iterator(),attributes[i],
									min_max[i][0],min_max[i][1]);
					// Transmit into an Instance List sorted in order;
					deque = linkAllocatedDeques(allocater);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			// Finalise the equivalence class by deque
			return getEquivalentClassByDeque(deque, attributes);
		}
	}
	/**
	 * Obtain equivalence classes with {@link Instance}s sorted by the given attributes.
	 *
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param attributes
	 * 		Attributes of {@link Instance}. or <code>null</code> if auto-analyzing.
	 * @return A {@link Set} of the {@link Instance} {@link Set} in the same type.
	 */
	public static Collection<Collection<Instance>> getEquivalenceClassByAttributesByAttributeList(
			Collection<Instance> instances, List<Integer> attributes
	) {
		// Use the 1st instance as instance representative of equivalence class.
		Instance uRep = instances.iterator().next();
		attributes = XuUtils.filterAttributes(uRep, attributes);
		if (attributes==null) {
			return new HashSet<>();
		}else if (attributes.size()==0) {
			//If attribute is empty, combine all instances into a set.
			Collection<Collection<Instance>> equClass = new HashSet<>();
			equClass.add(instances instanceof Set ? instances : new HashSet<>(instances));
			return equClass;
		}else{
			// Loop over to obtain min & max attribute value to get (min,max) of each attribute
			int[][] min_max = getMinMax(instances, attributes);
			// Loop over |p| attributes
			LinkableDeque<Instance> deque = null;
			// Build a attribute-based sorted collector
			LinkableDeque<Instance>[] allocater;
			try {
				allocater = getAllocatedUniverseDeques(
							instances.iterator(),attributes.get(0),
							min_max[0][0],min_max[0][1]);
				deque = linkAllocatedDeques(allocater);
				for (int i=1; i<attributes.size(); i++) {
					allocater = getAllocatedUniverseDeques(
									deque.iterator(),attributes.get(i),
									min_max[i][0],min_max[i][1]);
					// Transmit into an Instance List sorted in order;
					deque = linkAllocatedDeques(allocater);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			// Finalise the equivalence class by deque
			return getEquivalentClassByDeque(deque, attributes);
		}
	}
	

	/**
	 * Check if all {@link Instance}s are consistent: share the same <code>decision attribute
	 * value</code>.
	 * 
	 * @param instances
	 * 		A {@link Iterator} of {@link Instance}s.
	 * @return <code>true</code> if all {@link Instance} have the same <code>decision attribute
	 * 		value</code>.
	 * @throws IllegalArgumentException if the {@link Instance} is <code>null</code> or empty.
	 */
	public static boolean equalsInDecisionAttributeValue(Iterator<Instance> instances) {
		if (instances==null || !instances.hasNext()){
			throw new IllegalArgumentException("Illegal instances.");
		}
		
		//Initiate the decision value
		int decision = instances.next().getAttributeValue(0);
		//Compare the rest of the instance's decision attribute value
		while(instances.hasNext()){
			if (decision!=instances.next().getAttributeValue(0)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check if all {@link Instance}s are consistent: share the same <code>decision attribute
	 * value</code>.
	 * 
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}s.
	 * @return <code>true</code> if all {@link Instance} have the same <code>decision attribute
	 * 		value</code>.
	 * @throws IllegalArgumentException if the {@link Instance} is <code>null</code> or empty.
	 */
	@SuppressWarnings("unchecked")
	public static boolean equalsInDecisionAttributeValue(Collection<Instance> instances) {
		if (instances==null || instances.size()==0) {
			throw new IllegalArgumentException("Illegal instances.");
		}

		if (instances instanceof LinkedList) {
			return equalsInDecisionAttributeValue((List<Instance>) instances.iterator());
		}else {
			Instance[] array = instances.toArray(new Instance[instances.size()]);
			for ( int i=1; i<array.length; i++ ) {
				if (array[i].getAttributeValue(0) != array[0].getAttributeValue(0)) {
					return false;
				}
			}
		}
		return true;
	}
	
	
	/**
	 * Wrap the equivalent classes which consist of positive & negative representatives.
	 * 
	 * @param equClasses
	 * 		A {@link Collection} of {@link Instance} {@link Collection} as equivalent classes.
	 * @return a {@link PositiveNegativePackage} of the streamlined {@link Instance}.
	 */
	public static PositiveNegativePackage globalPositiveNegativePackage(
			Collection<Collection<Instance>> equClasses
	){
		Instance insRep;
		PositiveNegativePackage posNegPack = new PositiveNegativePackage();
		for (Collection<Instance> equClass : equClasses) {
			insRep = equClass.iterator().next();
			//Check if all instances in the set equals in decision value
			if (equalsInDecisionAttributeValue(equClass.iterator())) {
				//equals in decision value, add the 1st universe into positive set
				posNegPack.addPositive(insRep);
			}else {
				//else add the 1st instance into negative set
				posNegPack.addNegative(insRep);
			}
		}
		return posNegPack;
	}


	/**
	 * Calculate the {@link Instance}s' positive & negative region.
	 * 
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}s.
	 * @param globalRegion
	 * 		A {@link PositiveNegativePackage}.
	 * @return Results wrapped in {@link SignificancePackage}.
	 */
	public static SignificancePackage positiveNegativeRegion(
			Collection<Collection<Instance>> instances, PositiveNegativePackage globalRegion
	) {
		Set<Instance> positiveRegion = new HashSet<>(), negativeRegion = new HashSet<>();
		Collection<Instance> globalPos = globalRegion.getPositiveSet(),
							globalNeg = globalRegion.getNegativeSet();
		
		//Check the given positive & negative regions.
		for (Collection<Instance> typeSet : instances) {
			if (globalPos.containsAll(typeSet)) {
				if (equalsInDecisionAttributeValue(typeSet)) {
					// if instances in a non-empty type equals in Decision value,
					//	and U'pos.containsAll(non-empty instances)
					positiveRegion.addAll(typeSet);
				}
			}else if (globalNeg.containsAll(typeSet)){
				// else, if all non-empty instances contained by U'neg
				negativeRegion.addAll(typeSet);	
			}
		}
		return new SignificancePackage(instances, positiveRegion, negativeRegion);
	}

	
	/**
	 * Ori: Get the significance when adding an extra attribute.
	 * 
	 * @param insReps
	 * 		A {@link Collection} of {@link Instance} {@link Collection}s as representatives.
	 * @param attribute
	 * 		Attributes of {@link Instance}.(Starts from 1)
	 * @param globalRegion
	 * 		{@link PositiveNegativePackage} as the global positive negative regions.
	 * @return Results wrapped in {@link SignificancePackage}.
	 */
	@SuppressWarnings("unchecked")
	public static SignificancePackage significance(
			Collection<Collection<Instance>> insReps, int attribute,
			PositiveNegativePackage globalRegion
	) {
		// Check the extra attribute.
		if (insReps.iterator().next().iterator().next().getAttributeValue(attribute)==-1)
			return null;
		// Initiate.
		Collection<Collection<Instance>> advance_class = new HashSet<>();
		Set<Instance> pos = new HashSet<>(), neg = new HashSet<>();
		// Sort universes by the current universe representatives and the attribute.
		int[][] min_max;
		Set<Instance>[] type = null;
		int attr;
		for ( Collection<Instance>  rep_type : insReps ) {
			// Build |max-min+1| pairs set as types.
			min_max = getMinMax(rep_type, new int[] {attribute});
			type = new Set[min_max[0][1]-min_max[0][0]+1];	
			for ( int j=0; j<type.length; j++ )	type[j] = new HashSet<>();
			// Go through universe representatives, put into allocator[ attr-min ]
			for ( Instance u : rep_type ) {
				attr = u.getAttributeValue(attribute);
				type[attr-min_max[0][0]].add(u);
			}
			// For non-empty sets, add into advance class as a type.
			for (Set<Instance> each : type ) {
				if ( each.size()!=0 )	advance_class.add(each);
				else 					continue;
			}
		}
		//Check the given positive & negative regions.
			
		// Go through non-empty type universes
		for (Collection<Instance> non_empty : advance_class) {
			if ( globalRegion.getPositiveSet().containsAll(non_empty) ) {
				if ( equalsInDecisionAttributeValue(non_empty) ) {
					// if universes in a non-empty type equals in Decision value,
					//	and U'pos.containsAll(non-empty universes)
					pos.addAll(non_empty);
				}
			}else if ( globalRegion.getNegativeSet().containsAll(non_empty) ){
				// else, if all non-empty universes contained by U'neg
				neg.addAll(non_empty);
			}
		}
		
		// Return a significance package
		return new SignificancePackage(advance_class, pos, neg).setAttribute(attribute);
	}
	/**
	 * Adv : Get the significance when adding an extra attribute.
	 * 
	 * @param insReps
	 * 		A {@link Collection} of {@link Instance} {@link Collection}s as universe representatives.
	 * @param attribute
	 * 		Attributes of {@link Instance}.(Starts from 1)
	 * @param globalPack
	 * 		{@link PositiveNegativePackage} as the global positive negative regions.
	 * @return {@link SignificancePackage}.
	 */
	public static SignificancePackage significanceAdvance(
			Collection<Collection<Instance>> insReps, int attribute,
			PositiveNegativePackage globalPack
	) {
		// Initiate.
		SignificancePackage sig;
		Collection<Collection<Instance>> subClass, advanceClass = new HashSet<>();
		// Loop over instances in the representatives.
		for (Collection<Instance> types : insReps) {
			// Get Instances/{attribute}, each non-empty is an equivalence class of Instances/PU{a}.
			subClass = getEquivalenceClassByAttributes(types, new int[] {attribute});
			for (Collection<Instance> type : subClass){
				if (type!=null && type.size()!=0){
					advanceClass.add(type);
				}
			}
		}
		// Get positive negative region.
		sig = positiveNegativeRegion(advanceClass, globalPack).setAttribute(attribute);
		return sig;
	}

	
	/**
	 * Obtain the current most significant attribute
	 * 
	 * @param roughClasses
	 * 		A {@link Collection} of {@link Instance} {@link Collection} as rough equivalence
	 * 		classes.
	 * @param reduct
	 * 		Reduct attributes.
	 * @param attributeLength
	 * 		The number of the condition attributes
	 * @param globalRegion
	 * 		The global positive negative regions.
	 * @param originalAlgorithm
	 * 		Use original algorithm to seek significant attribute.
	 * @return a SignificancePackage instance.
	 */
	public static SignificancePackage mostSignificance(
			Collection<Collection<Instance>> roughClasses,
			Collection<Integer> reduct, int attributeLength,
			PositiveNegativePackage globalRegion, boolean originalAlgorithm
	) {
		// Initiate
		int max=0, sigAttr = -1;
		Collection<Collection<Instance>> sigClass = null;
		SignificancePackage sigPack = null;
		Collection<Instance> sigPositive = null, sigNegative = null;
		// Loop over potential attributes
		for (int attr=1; attr<=attributeLength; attr++) {
			if (reduct.contains(attr))	continue;
			// Calculate significance.
			sigPack = originalAlgorithm ? significance(roughClasses, attr, globalRegion) :
						significanceAdvance(roughClasses, attr, globalRegion) ;
			// If Sig p(a) > max, update.
			if (sigPack==null){
				continue;
			}else {
				if (sigAttr==-1) {
					max = sigPack.getSignificance();
					sigAttr = attr;
					sigClass = sigPack.getSignificanceClass();
					sigPositive = sigPack.getPositiveRegion();
					sigNegative = sigPack.getNegativeRegion();
				}else  {
					if (sigPack.getSignificance()>max) {
						max = sigPack.getSignificance();
						sigAttr = attr;
						sigClass = sigPack.getSignificanceClass();
						sigPositive = sigPack.getPositiveRegion();
						sigNegative = sigPack.getNegativeRegion();
					}
				}
			}
		}
		return new SignificancePackage(sigClass, sigPositive, sigNegative).setAttribute(sigAttr);
	}

	
	/**
	 * Reduction procedures
	 * 
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param original
	 * 		<code>true</code> to use the original algorithm 6 ({@link XuAlgorithm#significance(
	 * 		Collection, int, PositiveNegativePackage)}) to seek significant attributes.
	 * @param attributes
	 * 		Attributes of {@link Instance}.
	 * @return a {@link Set} of {@link Integer} as reduct. (Starts from 1)
	 */
	@SuppressWarnings("unchecked")
	public static Set<Integer> reduction(
			List<Instance> instances, boolean original, int[] attributes
	) {
		int attributeLength = instances.get(0).getValueLength()-1;
		
		// Obtain equivalence classes induced by All Attributes: U/C.
		Collection<Collection<Instance>> equClass = getEquivalenceClassByAttributes(instances, attributes);
		// Obtain compressed global decision table and positive negative regions.
		PositiveNegativePackage globalPack = globalPositiveNegativePackage(equClass);
		
		// Initiate reduced attributes
		Set<Integer> red = new HashSet<>();
		// Initiate instance representatives
		Collection<Instance> insReps = new HashSet<>();
		insReps.addAll(globalPack.getMix());
				
		SignificancePackage sigPack;
		Collection<Collection<Instance>> roughClass, posEquClass, negEquClass;
		// Loop over attributes until the instance rep is empty.
		while (insReps.size()!=0) {
			roughClass = getEquivalenceClassByAttributes(insReps, ArrayCollectionUtils.getIntArrayByCollection(red));
			
			// Get the most significant attribute.
			sigPack = mostSignificance(roughClass, red, attributeLength, globalPack, original);
			if (sigPack==null){
				break;
			}
			
			red.add(sigPack.getAttribute());
			//	Update instance representatives
			insReps = XuUtils.removeItemsFromUniverses(insReps, sigPack.getPositiveRegion(),
								sigPack.getNegativeRegion());
			
			// If representative is empty, return reduced attributes.
			if (insReps.size()==0)		break;
			
			// Remove positive/negative regions.
			globalPack.removeAllFromPositive(sigPack.getPositiveRegion());
			globalPack.removeAllFromNegative(sigPack.getNegativeRegion());
			
			// Sort positive region and negative region by the sig attribute.
			posEquClass = sigPack.getPositiveRegionSize()>0 ? 
					getEquivalenceClassByAttributes(sigPack.getPositiveRegion(), new int[] {
							sigPack.getAttribute()}): new HashSet<>();
			negEquClass = sigPack.getNegativeRegionSize()>0 ?
					getEquivalenceClassByAttributes(sigPack.getNegativeRegion(), new int[] {
							sigPack.getAttribute()}): new HashSet<>();
					
			// Update instance representatives.
			insReps = XuUtils.removeItemsFromUniverses(insReps, posEquClass
							.toArray(new Set[posEquClass.size()]));
			insReps = XuUtils.removeItemsFromUniverses(insReps, negEquClass
							.toArray(new Set[negEquClass.size()]));
		}
		return red;
	}

	
	/**
	 * Get the max & min values for each attribute.
	 * 
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param attribute
	 * 		Attributes of {@link Instance}.(Starts from 1)
	 * @return an int array of the max & min values. array[i][min/max]
	 */
	public static int[][] getMinMax(Collection<Instance> instances, int[] attribute){
		int attrValue;
		int[][] min_max = null;
		// Initiate pairs of (min,max), min_max[][] = [min, max]
		min_max = new int[attribute.length][2];
		for (int i=0; i<min_max.length; i++){
			min_max[i][0] = Integer.MAX_VALUE;
		}
		// Update min and max.
		for (Instance ins : instances) {
			for ( int i=0; i<attribute.length; i++) {
				attrValue = ins.getAttributeValue(attribute[i]);
				//update min
				if (attrValue<min_max[i][0]) {	min_max[i][0]=attrValue;    }
				//update max
				if (attrValue>min_max[i][1]) {	min_max[i][1]=attrValue;    }
			}
		}
		return min_max;
	}

	/**
	 * Get the max & min values for each attribute.
	 * 
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param attribute
	 * 		Attributes of {@link Instance}.(Starts from 1)
	 * @return an int array of the max & min values. array[i][min/max]
	 */
	public static int[][] getMinMax(Collection<Instance> instances, List<Integer> attribute){
		int attrValue;
		int[][] min_max = null;
		// Initiate pairs of (min,max), min_max[][] = [min, max]
		min_max = new int[attribute.size()][2];
		for (int i=0; i<min_max.length; i++){
			min_max[i][0] = Integer.MAX_VALUE;
		}
		// Update min and max.
		int i;
		for (Instance ins : instances) {
			i=0;
			for (int attr : attribute) {
				attrValue = ins.getAttributeValue(attr);
				//update min
				if (attrValue<min_max[i][0]) {	min_max[i][0]=attrValue;	}
				//update max
				if (attrValue>min_max[i][1]) {	min_max[i][1]=attrValue;	}
				i++;
			}
		}
		return min_max;
	}

	
	/**
	 * Get allocated instance deque for one circulation. The allocation is based on the
	 * current order of the {@link Instance}s and the given <code>attribute values</code>.
	 * 
	 * @param instances
	 * 		An {@link Iterator} of {@link Instance}.
	 * @param attribute
	 * 		An attribute of {@link Instance}.(Starts from 1)
	 * @param min
	 * 		The min value of the attribute.
	 * @param max
	 * 		The max value of the attribute.
	 * @return a {@link LinkableDeque} array of the allocated {@link Instance}s based on the given
	 *      {@link Instance}s order and the attribute.
	 * @throws Exception if the attribute's value is illegal.
	 */
	private static LinkableDeque<Instance>[] getAllocatedUniverseDeques(
			Iterator<Instance> instances, int attribute, int min, int max
	) throws Exception {
		// Initiate an allocator
		@SuppressWarnings("unchecked")
		LinkableDeque<Instance>[] allocator = new LinkableDeque[max-min+1];
		for (int i=0; i<allocator.length; i++){
			allocator[i] = new LinkableDeque<>();
		}
		
		Instance instance;
		int allocateIndex;
		while (instances.hasNext()) {
			instance = instances.next();
			allocateIndex = instance.getAttributeValue(attribute) - min;
			if (allocateIndex==-1){
				throw new Exception("Illegal attribute : "+(attribute-min));
			}
			allocator[allocateIndex].addLast(instance);
		}
		return allocator;
	}


	/**
	 * Link allocated queue.
	 * 
	 * @param deques
	 * 		The deque array of the {@link Instance}.
	 * @return An {@link Instance} {@link LinkableDeque}.
	 */
	private static LinkableDeque<Instance> linkAllocatedDeques(LinkableDeque<Instance>[] deques) {
		LinkableDeque<Instance> collector = new LinkableDeque<>();
		for (LinkableDeque<Instance> queue: deques){
			collector.addAllDeque(queue);
		}
		return collector;
	}


	/**
	 * Obtain the equivalence classes by deque.
	 * 
	 * @param deque
	 * 		The sorted deque in order.
	 * @param attributes
	 * 		Attributes of {@link Instance}.
	 * @return a {@link Collection} of {@link Instance} {@link Collection}s.
	 */
	private static Collection<Collection<Instance>> getEquivalentClassByDeque(
			LinkableDeque<Instance> deque, int[] attributes
	){
		Collection<Collection<Instance>> equivalenceClass = new HashSet<>();
		if (deque==null || deque.size()==0)	return equivalenceClass;
		
		boolean same;
		Iterator<Instance> iterator = deque.iterator();
		Instance previous = null, current = iterator.next();
		// Circulate instances to sort.
		Set<Instance> instances = new HashSet<>();
		instances.add(current);
		while(iterator.hasNext()) {
			previous = current;
			current = iterator.next();
			
			same = true;
			for (int i=0; i<attributes.length; i++) {
				if (current.getAttributeValue(attributes[i]) !=
					previous.getAttributeValue(attributes[i])
				) {
					same = false;
					break;
				}
			}
			
			if (same) {
				instances.add(current);
			}else {
				equivalenceClass.add(instances);
				instances = new HashSet<>();
				instances.add(current);
			}
		}
		equivalenceClass.add(instances);
		
		return equivalenceClass;
	}
	
	/**
	 * Obtain the equivalence class by deque.
	 * 
	 * @param deque
	 * 		The sorted deque in order.
	 * @param attributes
	 * 		Attributes of {@link Instance}.
	 * @return a {@link Collection} of {@link Instance} {@link Collection}s.
	 */
	private static Collection<Collection<Instance>> getEquivalentClassByDeque(
			LinkableDeque<Instance> deque, List<Integer> attributes
	){
		Collection<Collection<Instance>> equivalenceClass = new HashSet<>();
		if ( deque==null || deque.size()==0 )	return equivalenceClass;
		
		boolean same;
		Iterator<Instance> iterator = deque.iterator();
		Instance previous = null, current = iterator.next();
		//Circulate instances to sort.
		Set<Instance> instances = new HashSet<>();
		instances.add(current);
		while( iterator.hasNext() ) {
			previous = current;
			current = iterator.next();
			
			same = true;
			for (int attr : attributes) {
				if (current.getAttributeValue(attr) !=
					previous.getAttributeValue(attr)
				) {
					same = false;
					break;
				}
			}
			
			if (same) {
				instances.add(current);
			}else {
				equivalenceClass.add(instances);
				instances = new HashSet<>();
				instances.add(current);
			}
		}
		equivalenceClass.add(instances);
		
		return equivalenceClass;
	}
}
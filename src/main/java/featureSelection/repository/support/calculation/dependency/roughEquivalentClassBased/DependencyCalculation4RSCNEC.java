package featureSelection.repository.support.calculation.dependency.roughEquivalentClassBased;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.PlainNestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.nestedEC.NestedEquivalenceClassesInfo;
import featureSelection.repository.entity.alg.rec.nestedEC.SignificanceCalculationResult;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedRealtimeSimpleCountingCalculation;
import featureSelection.repository.support.calculation.dependency.DefaultDependencyCalculation;
import lombok.Getter;

/**
 * Dependency Calculation for Simple Counting REC (Real-time Counting version).
 * 
 * <p>RSC-NEC: Real-time Simple Counting - NEC.
 * 
 * @see NestedEquivalenceClassBasedAlgorithm
 * 
 * @author Benjamin_L
 */
public class DependencyCalculation4RSCNEC
	extends DefaultDependencyCalculation
	implements NestedEquivalenceClassBasedRealtimeSimpleCountingCalculation<Double>
{
	@Getter private NestedEquivalenceClassesInfo<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>> necInfoWithMap;
	@Getter private Double result;
	
	@Override
	public DependencyCalculation4RSCNEC calculate(
			Collection<EquivalenceClass> equ, IntegerIterator attributes, Object... args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		int universeSize = (int) args[0];
		SignificanceCalculationResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Integer>
				sigPack = nestedEquivalenceClass(equ, attributes);
		result = sigPack.getSignificance() / (double) universeSize;
		necInfoWithMap = sigPack.getNestedEquivalenceClassesInfo();
		return this;
	}
	
	/**
	 * Get {@link NestedEquivalenceClass}es of {@link EquivalenceClass} {@link Collection} by
	 * given <code>attributes</code>.
	 * 
	 * @param equClasses
	 * 		An {@link EquivalenceClass} {@link Collection}.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1).
	 * @return {@link NestedEquivalenceClassesInfo} with
	 * 		1. A {@link Map} whose keys are the Equivalence values in {@link IntArrayKey} and
	 * 			values are correspondent {@link NestedEquivalenceClass};
	 * 		2. significance in {@link int};
	 * 		3. boolean value for empty boundary classes.
	 */
	public static SignificanceCalculationResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Integer>
		nestedEquivalenceClass(Collection<EquivalenceClass> equClasses, IntegerIterator attributes
	) {
		// Initiate a Hash: H
		Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses = new HashMap<>(equClasses.size());
		// pos=0, flag = false
		int pos = 0;
		boolean notEmptyBoundary = false;
		// Go through Equivalence Classes and partition.
		int[] keyArray;
		IntArrayKey key;
		NestedEquivalenceClass<EquivalenceClass> nestedEquClass;
		for (EquivalenceClass equClass: equClasses) {
			// key = P(e[i])
			keyArray = new int[attributes.size()];
			attributes.reset();
			for (int i=0; i<keyArray.length; i++)
				keyArray[i] = equClass.getAttributeValueAt(attributes.next()-1);
			key = new IntArrayKey(keyArray);
			// if H doesn't contains key
			nestedEquClass = nestedEquClasses.get(key);
			if (nestedEquClass==null) {
				// create h, h.count=e[i].count, h.dec=e[i].dec
				nestedEquClasses.put(key, nestedEquClass=new PlainNestedEquivalenceClass(equClass));
				// if e[i].cnst=true
				//	h.cnst=1, pos=pos+e[i].count
				if (equClass.sortable())	pos += equClass.getInstanceSize();
				// else e[i].cnst=false
				//	h.cnst=-1
			// else h in H
			}else {
				// h.count+=e[i].count
				nestedEquClass.addClassItem(equClass);
				switch (nestedEquClass.getType()) {
					case POSITIVE:		// 1-NEC
						// if h.dec=e[i].dec			1-NEC
						if (nestedEquClass.getDec()==equClass.getDecisionValue()) {
							// pos += e.count, continue;
							pos += equClass.getInstanceSize();
						// else							1-NEC => 0-NEC
						}else {
							// h.cnst=0, h.dec='/'
							// pos -= h.count
							// update flag
							nestedEquClass.setType(ClassSetType.BOUNDARY);
							pos -= nestedEquClass.getInstanceSize();
							notEmptyBoundary = true;
						}
						break;
					case NEGATIVE:		// -1-NEC
						// if e.cnst=true				-1-NEC => 0-NEC
						if (ClassSetType.POSITIVE.equals(equClass.getType())) {
							nestedEquClass.setType(ClassSetType.BOUNDARY);
							notEmptyBoundary = true;
						}
						break;
					default:			// 0-NEC
						break;
				}
			}
		}
		return new SignificanceCalculationResult<>(
				pos,
				new NestedEquivalenceClassesInfo<>(
					nestedEquClasses, 
					!notEmptyBoundary
				)
			);
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}
	
	@Override
	public Collection<Integer> getPartitionAttributes() {
		throw new UnsupportedOperationException("Unimplemeted method!");
	}
}
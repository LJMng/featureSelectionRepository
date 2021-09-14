 package featureSelection.repository.support.calculation.entropy.liangConditionEntropy.liangIncrementalAlgorithm;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.liangIncrementalAlgorithm.LiangIncrementalAlgorithm;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.EquivalenceClassInterf;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.MixedEquivalenceClassSequentialList;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.classSetType.ClassSetType;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiangIncremental;
import featureSelection.repository.support.calculation.entropy.liangConditionEntropy.DefaultLiangConditionEntropyCalculation;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

public class LCECalculation4LiangIncrementalAlgorithm
	extends DefaultLiangConditionEntropyCalculation
	implements FeatureImportance4LiangIncremental<Double>
{
	@Getter private Double result;

	/**
	 * <strong>LCE<sub>U∩{x}</sub>(D|B)</strong> = 1/(|U|+1)<sup>2</sup> * 
	 * (|U|<sup>2</sup>LCE<sub>U</sub>(D|B)+2|X'<sub>p</sub>-Y'<sub>q</sub>|)
	 * <p>
	 * where x is the new Instance added; X'<sub>p</sub> &isin; U∪{x}/B; Y'<sub>q</sub> &isin;
	 * U∪{x}/D.
	 * <p>
	 * <strong>Notice:</strong>
	 * Calculated entropy result is the one <strong>with/without</strong> denominator
	 * part(|U|<sup>2</sup>) bases on the value of <code>previousSigWithDenominator</code>:
	 * <ul>
	 * 	<li>LCE = numerator <del>/ denominator</del> if <code>previousSigWithDenominator</code> is
	 * 			<strong>false</strong>
	 * 	</li>
	 * 	<li>LCE = numerator / denominator if <code>previousSigWithDenominator</code> is
	 * 			<strong>true</strong>
	 * 	</li>
	 * </ul>
	 */
	@Override
	public FeatureImportance4LiangIncremental<Double> calculate(
			Collection<Instance> instances, Collection<Instance> equClass,
			Instance newInstance, Double previousSig, boolean previousSigWithDenominator,
			int attributeLength
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		result = liangConditionalEntropy(instances, equClass, newInstance, previousSig, previousSigWithDenominator);
		return this;
	}
	
	/**
	 * <strong>LCE<sub>U∪U<sub>X</sub></sub>(D|B)</strong> = 1 / |U∪U<sub>X</sub>|<sup>2</sup> * 
	 * (|U|<sup>2</sup>LCE<sub>U</sub>(D|B) + |U<sub>X</sub>|<sup>2</sup>LCE<sub>U<sub>X</sub></sub>(D|B))
	 * +<strong>&Delta;</strong>.
	 * <p>
	 * <strong>&Delta;</strong> = &Sigma;<sub>i=1</sub><sup>k</sup>(
	 * 	&Sigma;<sub>j=1</sub><sup>l</sup>(
	 * 		(|X<sub>i</sub>∩Y<sub>j</sub>||M<sub>i</sub>-Z<sub>j</sub>| + 
	 * 		|M<sub>i</sub>∩Z<sub>j</sub>||X<sub>i</sub>-Y<sub>j</sub>|) / 
	 * 		|U∪U<sub>X</sub>|<sup>2</sup>
	 * 	)+
	 * 	&Sigma;<sub>j=l+1</sub><sup>n</sup>(
	 * 		|X<sub>i</sub>∩Y<sub>j</sub>||M<sub>i</sub>| / |U∪U<sub>X</sub>|<sup>2</sup>
	 * 	)+
	 * 	&Sigma;<sub>j=l+1</sub><sup>n'</sup>(
	 * 		|M<sub>i</sub>∩Z<sub>j</sub>||X<sub>i</sub>| / |U∪U<sub>X</sub>|<sup>2</sup>
	 * 	)
	 * )
	 * <p>
	 * <strong>Notice:</strong>
	 * Calculated entropy result is the one <strong>with/without</strong> denominator part(|U∪U<sub>X</sub>|
	 * <sup>2</sup>) bases on the value of <code>previousSigWithDenominator</code>:
	 * <ul>
	 * 	<li>LCE = numerator <del>/ denominator</del> if <code>previousSigWithDenominator</code> is
	 * 			<strong>false</strong>
	 * 	</li>
	 * 	<li>LCE = numerator / denominator if <code>previousSigWithDenominator</code> is
	 * 			<strong>true</strong>
	 * 	</li>
	 * </ul>
	 */
	@Override
	public FeatureImportance4LiangIncremental<Double> calculate(
			MixedEquivalenceClassSequentialList equClassesCMBResult,
			MixedEquivalenceClassSequentialList decEquClassesCMBResult,
			int previousUniverse, int newInstance,
			Double previousSig, Double newSig, boolean previousSigWithDenominator,
			int attributeLength
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		result = liangConditionalEntropy(equClassesCMBResult, decEquClassesCMBResult, previousUniverse, newInstance, previousSig, newSig, previousSigWithDenominator);
		return this;
	}
	
	private static double liangConditionalEntropy(
		Collection<Instance> instances, Collection<Instance> equClass,
		Instance newInstance, Double previousSig, boolean previousSigWithDenominator
	) {
		// Locate equivalent class that has the same decision value as <code>newInstance</code>
		Map<IntArrayKey, Collection<Instance>> decEquClasses =
				LiangIncrementalAlgorithm
					.Basic
					.equivalenceClass(instances, new IntegerArrayIterator(new int[] {0}));
		Collection<Instance> decEquClassesIdentical2NewIns =
				decEquClasses.get(new IntArrayKey(new int[] {newInstance.getAttributeValue(0)}));
		// if decision value exist.
		if (decEquClassesIdentical2NewIns!=null) {
			// Add the new instance into the equivalent class.
			decEquClassesIdentical2NewIns.add(newInstance);
		// if no such decision value.
		}else {
			// decEquClassesIdentical2NewIns = {x}
			decEquClassesIdentical2NewIns = new LinkedList<>();
			decEquClassesIdentical2NewIns.add(newInstance);
		}
		// Initiate sum=0
		// Loop ins in X': equClass
		// 	Search for ins in Y'
		// 	if exist: intersection++
		long intersection=0;
		for (Instance ins: equClass)
			if (!decEquClassesIdentical2NewIns.contains(ins))
				intersection += 1;
		// LE(D/P) = 1/(|U|+1)^2 * (|U|^2 * sig<sub>U</sub>(D/P) + 2|X'-Y'|)
		//		   = 1/(|U|+1)^2 * (|U|^2 * sig<sub>U</sub>(D/P) + 2 * (X'-intersection))
		//		   = (|U|^2 * sig<sub>U</sub>(D/P) + 2 * (X'-intersection)) / (|U|+1)^2
		return previousSigWithDenominator?
				(FastMath.pow(instances.size(), 2) * previousSig + 2 * (equClass.size()-intersection)) / FastMath.pow(instances.size()+1, 2):
				(previousSig + 2 * (equClass.size()-intersection));
	}
	
	private static double liangConditionalEntropy(
			MixedEquivalenceClassSequentialList equClassesCMBResult,
			MixedEquivalenceClassSequentialList decEquClassesCMBResult,
			int previousInstance, int newInstance,
			Double previousSig, Double newSig, boolean previousSigWithDenominator
	) {
		// LCE<sub>U∪U<sub>X</sub></sub>(D|B)</strong> = 
		//	1 / |U∪U<sub>X</sub>|<sup>2</sup> *  
		//	(|U|<sup>2</sup>LCE<sub>U</sub>(D|B) + |U<sub>X</sub>|<sup>2</sup>LCE<sub>U<sub>X</sub></sub>(D|B))
		//	 + &Delta;
		double entropy, delta = delta(equClassesCMBResult, decEquClassesCMBResult, previousSig, newSig);
		if (previousSigWithDenominator) {
			entropy = FastMath.pow(previousInstance, 2) * previousSig + FastMath.pow(newInstance, 2) * newSig + delta;
			entropy /= FastMath.pow(previousInstance+newInstance, 2);
		}else{
			entropy = previousSig + newSig + delta;
		}
		return entropy;
	}
	
	/**
	 * <strong>&Delta;</strong> = &Sigma;<sub>i=1</sub><sup>k</sup>(
	 * 	&Sigma;<sub>j=1</sub><sup>l</sup>(
	 * 		(|X<sub>i</sub>∩Y<sub>j</sub>||M<sub>i</sub>-Z<sub>j</sub>| + 
	 * 		|M<sub>i</sub>∩Z<sub>j</sub>||X<sub>i</sub>-Y<sub>j</sub>|) / 
	 * 		|U∪U<sub>X</sub>|<sup>2</sup>
	 * 	)+
	 * 	&Sigma;<sub>j=l+1</sub><sup>n</sup>(
	 * 		|X<sub>i</sub>∩Y<sub>j</sub>||M<sub>i</sub>| / |U∪U<sub>X</sub>|<sup>2</sup>
	 * 	)+
	 * 	&Sigma;<sub>j=l+1</sub><sup>n'</sup>(
	 * 		|M<sub>i</sub>∩Z<sub>j</sub>||X<sub>i</sub>| / |U∪U<sub>X</sub>|<sup>2</sup>
	 * 	)
	 * )
	 * 
	 * @param equClassesCMBResult
	 * 		Combined Equivalent Classes result in {@link MixedEquivalenceClassSequentialList} on B:
	 * 		<strong>(U∪U<sub>X</sub>)/B</strong>
	 * @param decEquClassesCMBResult
	 * 		Combined Equivalent Classes result in {@link MixedEquivalenceClassSequentialList} on D:
	 * 		<strong>(U∪U<sub>X</sub>)/D</strong>
	 * @param previousSig
	 * 		Sig value of previous Universe instances on B: <strong>LCE<sub>U</sub>(D|B)</strong>.
	 * @param newSig
	 * 		Sig value of new Universe instances on B: <strong>LCE<sub>U<sub>X</sub></sub>(D|B)</strong>.
	 * @return the result of &Delta;.
	 */
	private static double delta(
			MixedEquivalenceClassSequentialList equClassesCMBResult,
			MixedEquivalenceClassSequentialList decEquClassesCMBResult,
			Double previousSig, Double newSig
	) {
		// Initiate delta.
		double delta = 0.0;
		// Loop over instances in X'[i], where X'[i] is in MixEquivalentClasses on B.
		Collection<Instance> x, m, y, z;
		EquivalenceClassInterf equClass, decEquClass;
		Iterator<EquivalenceClassInterf> decEquClassCMBIterator;
		Iterator<EquivalenceClassInterf> equClassCMBIterator = equClassesCMBResult.getEquClasses().iterator();
		for (int i=0; i<equClassesCMBResult.getMixed(); i++) {
			equClass = equClassCMBIterator.next();
			// X<sub>i</sub>
			x = equClass.getInstances(ClassSetType.PREVIOUS);
			double xSize = x==null? 0.0: x.size();
			// M<sub>i</sub>
			m = equClass.getInstances(ClassSetType.NEW);
			double mSize = m==null? 0.0: m.size();
			// loop over Y'[j], where Y'[j] is in MixEquivalentClasses on D.
			decEquClassCMBIterator = decEquClassesCMBResult.getEquClasses().iterator();
			for (int j=0; j<decEquClassesCMBResult.getMixed(); j++) {
				decEquClass = decEquClassCMBIterator.next();
				// Y<sub>j</sub>
				y = decEquClass.getInstances(ClassSetType.PREVIOUS);
				// |X<sub>i</sub>∩Y<sub>j</sub>|
				Collection<Instance> intersectionOfXY =
					LiangIncrementalAlgorithm
						.Basic
						.Mathematical
						.intersectionOf(x, y);
				// Z<sub>j</sub>
				z = decEquClass.getInstances(ClassSetType.NEW);
				// |M<sub>i</sub>∩Z<sub>j</sub>|
				Collection<Instance> intersectionOfMZ =
					LiangIncrementalAlgorithm
						.Basic
						.Mathematical
						.intersectionOf(m, z);
				// delta += (|X<sub>i</sub>∩Y<sub>j</sub>| * |M<sub>i</sub>-Z<sub>j</sub>|+
				//			|M<sub>i</sub>∩Z<sub>j</sub>| * |X<sub>i</sub>-Y<sub>j</sub>|
				//		) / |U∪U<sub>X</sub>|^2
				delta += intersectionOfXY.size() * (mSize - intersectionOfMZ.size()) + 
							intersectionOfMZ.size() * (xSize - intersectionOfXY.size());
			}
			// loop over Y[j], where Y[j] is in EquivalentClasses on D whose ClassSetType is PREVIOUS.
			for (int j=0; j<decEquClassesCMBResult.getPreviousOnly(); j++) {
				decEquClass = decEquClassCMBIterator.next();
				// Y<sub>j</sub>
				y = decEquClass.getInstances(ClassSetType.PREVIOUS);
				// |X<sub>i</sub>∩Y<sub>j</sub>|
				Collection<Instance> intersectionOfXY =
					LiangIncrementalAlgorithm
						.Basic
						.Mathematical
						.intersectionOf(x, y);
				// delta += (|X<sub>i</sub>∩Y<sub>j</sub>| * |M<sub>i</sub>|) / |U∪U<sub>X</sub>|^2
				delta += ((double) intersectionOfXY.size()) * mSize;
			}
			// loop over Z[j], where Z[j] is in EquivalentClasses on D whose ClassSetType is NEW.
			for (int j=0; j<decEquClassesCMBResult.getNewOnly(); j++) {
				decEquClass = decEquClassCMBIterator.next();
				// Y<sub>j</sub>
				z = decEquClass.getInstances(ClassSetType.NEW);
				// |M<sub>i</sub>∩Z<sub>j</sub>|
				Collection<Instance> intersectionOfMZ =
						LiangIncrementalAlgorithm
							.Basic
							.Mathematical
							.intersectionOf(m, z);
				// delta += (|M<sub>i</sub>∩Z<sub>j</sub>| * |X<sub>i</sub>|) / |U∪U<sub>X</sub>|^2
				delta += ((double) intersectionOfMZ.size()) * xSize;
			}
		}
		return delta;
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		throw new UnsupportedOperationException("Unimplemeted Method!");
	}

};
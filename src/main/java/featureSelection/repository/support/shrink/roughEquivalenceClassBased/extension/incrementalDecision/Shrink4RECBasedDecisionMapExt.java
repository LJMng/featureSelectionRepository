package featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalDecision;

import java.util.Iterator;

import featureSelection.basic.support.shrink.ShrinkInstance;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.RoughEquivalenceClassDecMapXtension;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation;
import lombok.Setter;

public class Shrink4RECBasedDecisionMapExt<Sig extends Number>
		implements ShrinkInstance<ShrinkInput4RECIncrementalDecisionExtension<Sig>,
									RoughEquivalenceClassDecMapXtension<Sig>,
									ShrinkResult4RECIncrementalDecisionExtension<Sig>>
{
	@Setter private int universeSize;
	@Setter private RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation;
	
	@Override
	public ShrinkResult4RECIncrementalDecisionExtension<Sig> shrink(
			ShrinkInput4RECIncrementalDecisionExtension<Sig> in
	) throws Exception {
		int removedNegUniverse = 0, removeNegEquClass = 0;
		Sig removedSig = null;
		RoughEquivalenceClassDecMapXtension<Sig> roughClass;
		Iterator<RoughEquivalenceClassDecMapXtension<Sig>> iterator = in.getRoughClasses()
																			.iterator();
		while (iterator.hasNext()) {
			roughClass = iterator.next();
			if (removAble(roughClass)) {
				iterator.remove();
				calculation.calculate(roughClass, in.getAttributesInvolvedNumber(), universeSize);
				if (roughClass.getType().isNegative()) {
					removedSig = calculation.plus(removedSig, calculation.getResult());
					removedNegUniverse += roughClass.getInstanceSize();
					removeNegEquClass += roughClass.getItemSize();
				}
			}
		}
		return new ShrinkResult4RECIncrementalDecisionExtension<>(
					in.getRoughClasses(), 
					new int[] {removedNegUniverse, removeNegEquClass}, 
					removedSig
				);
	}

	@Override
	public boolean removAble(RoughEquivalenceClassDecMapXtension<Sig> item) {
		return item.getType().isPositive() ||
				(item.getType().isNegative() && item.getItemSize()==1)	||
				item.getItemSize()==0;
	}
}

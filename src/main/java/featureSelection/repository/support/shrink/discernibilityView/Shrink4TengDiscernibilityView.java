package featureSelection.repository.support.shrink.discernibilityView;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.shrink.ShrinkInstance;
import featureSelection.repository.algorithm.alg.discernibilityView.TengDiscernibilityViewAlgorithm;

import java.util.Collection;
import java.util.Iterator;

public class Shrink4TengDiscernibilityView
	implements ShrinkInstance<ShrinkInput4TengDiscernibilityView, ShrinkRemovableCriteria4TengDiscernibilityView, Integer>
{

	@Override
	public Integer shrink(ShrinkInput4TengDiscernibilityView in) throws Exception {
		int removeCount = 0;
		// Go through M[r] in U[j]/red.
		Collection<Instance> equClass;
		Iterator<Collection<Instance>> iterator = in.getEquClasses2BRemoved().iterator();
		while (iterator.hasNext()) {
			equClass = iterator.next();
			if (removAble(
					new ShrinkRemovableCriteria4TengDiscernibilityView(
						in.getGlobalEquClasses(), in.getDecEquClasses(), equClass
					)
				)
			) {
				iterator.remove();
				removeCount+=equClass.size();
			}
		}
		return removeCount;
	}

	@Override
	public boolean removAble(ShrinkRemovableCriteria4TengDiscernibilityView item) {
		// if  M[r] is a sub-element of U[j]/D
		// 	or M[r] is a sub-element of U[j]/C
		// then U' = U' ∪ M[r], where U' contains universes removable.
		return TengDiscernibilityViewAlgorithm
					.isSubEquivalenceClassOf(
						// U[j]/D
						item.getDecEquClasses(),
						// M[r]
						item.getEquClass2BRemoved()
					) ||
				TengDiscernibilityViewAlgorithm
					.isSubEquivalenceClassOf(
						// U[j]/C
						item.getGlobalEquClasses(), 
						// M[r]
						item.getEquClass2BRemoved()
					);
	}
}

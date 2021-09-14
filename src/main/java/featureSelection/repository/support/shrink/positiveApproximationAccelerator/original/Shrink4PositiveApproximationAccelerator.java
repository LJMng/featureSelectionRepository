package featureSelection.repository.support.shrink.positiveApproximationAccelerator.original;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.shrink.ShrinkInstance;
import featureSelection.repository.entity.alg.positiveApproximationAccelerator.EquivalenceClass;

import java.util.Collection;
import java.util.Iterator;

public class Shrink4PositiveApproximationAccelerator
	implements ShrinkInstance<Collection<EquivalenceClass>, EquivalenceClass, int[]>
{
	/**
	 * Remove items that can be removed. Using {@link #removAble(EquivalenceClass)}.
	 * 
	 * @see #removAble(EquivalenceClass).
	 * 
	 * @param in
	 * 		{@link EquivalenceClass} {@link Collection}.
	 * @return number of removed {@link Instance} and {@link EquivalenceClass}.
	 */
	@Override
	public int[] shrink(Collection<EquivalenceClass> in) throws Exception {
		int remove = in.size(), removedUniverse = 0;
		EquivalenceClass equ;
		Iterator<EquivalenceClass> iterator = in.iterator();
		while(iterator.hasNext()) {
			// if h.dec!='/'
			if (removAble(equ=iterator.next())) {
				// Delete h
				iterator.remove();
				removedUniverse += equ.getInstances().size();
			}
		}
		return new int[] {removedUniverse, remove - in.size()};
	}
	
	/**
	 * If item's decision value != -1 (i.e. item is positive region), then it is removeable.
	 * 
	 * @return <code>true</code> if item can be removed.
	 */
	@Override
	public boolean removAble(EquivalenceClass item) {
		return item.getDecisionValue()!=-1;
	}
}

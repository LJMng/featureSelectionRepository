package featureSelection.repository.support.shrink.roughEquivalenceClassBased;

import featureSelection.basic.support.shrink.ShrinkInstance;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.interf.ClassSet;

import java.util.Collection;
import java.util.Iterator;

public class Shrink4RECBoundaryClassSetStays
	implements ShrinkInstance<Collection<? extends ClassSet<EquivalenceClass>>, ClassSet<EquivalenceClass>, Integer>
{
	@Override
	public Integer shrink(Collection<? extends ClassSet<EquivalenceClass>> in) {
		int removed = 0;
		ClassSet<EquivalenceClass> roughClass;
		Iterator<? extends ClassSet<EquivalenceClass>> iterator = in.iterator();
		while (iterator.hasNext()) {
			roughClass = iterator.next();
			if (removAble(roughClass)) {
				iterator.remove();
				removed += roughClass.getInstanceSize();
			}
		}
		return removed;
	}

	@Override
	public boolean removAble(ClassSet<EquivalenceClass> item) {
		return !ClassSetType.BOUNDARY.equals(item.getType());
	}
}
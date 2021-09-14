package featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalPartition;

import featureSelection.basic.support.shrink.ShrinkInstance;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.interf.ClassSet;

import java.util.Collection;
import java.util.Iterator;

public class Shrink4RECBoundaryClassSetStays
	implements ShrinkInstance<Collection<? extends ClassSet<EquivalenceClass>>, ClassSet<EquivalenceClass>, int[]>
{
	@Override
	public int[] shrink(Collection<? extends ClassSet<EquivalenceClass>> in) {
		int removePosU = 0, removeNegU = 0, removePosEqu = 0, removeNegEqu  = 0;
		ClassSet<EquivalenceClass> roughClass;
		Iterator<? extends ClassSet<EquivalenceClass>> iterator = in.iterator();
		while (iterator.hasNext()) {
			roughClass = iterator.next();
			switch(roughClass.getType()) {
				case POSITIVE:
					iterator.remove();
					removePosU+=roughClass.getInstanceSize();
					removePosEqu+=roughClass.getItemSize();
					break;
				case NEGATIVE:
					iterator.remove();
					removeNegU+=roughClass.getInstanceSize();
					removeNegEqu+=roughClass.getItemSize();
					break;
				default:
					break;
			}
		}
		return new int[] {removePosU, removeNegU, removePosEqu, removeNegEqu};
	}

	@Override
	public boolean removAble(ClassSet<EquivalenceClass> item) {
		return !ClassSetType.BOUNDARY.equals(item.getType());
	}
}
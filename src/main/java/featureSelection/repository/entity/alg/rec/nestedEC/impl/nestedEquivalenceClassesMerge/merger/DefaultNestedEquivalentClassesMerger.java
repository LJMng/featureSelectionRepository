package featureSelection.repository.entity.alg.rec.nestedEC.impl.nestedEquivalenceClassesMerge.merger;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.nestedEC.impl.nestedEquivalenceClassesMerge.params.DefaultNestedEquivalenceClassesMergerParams;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMerger;

import java.util.Iterator;
import java.util.Map;

/**
 * Default implementation of {@link NestedEquivalenceClassesMerger}
 * 
 * @author Benjamin_L
 */
public class DefaultNestedEquivalentClassesMerger
	implements NestedEquivalenceClassesMerger<DefaultNestedEquivalenceClassesMergerParams<Integer>,
													NestedEquivalenceClass<EquivalenceClass>>
{
	@Override
	public NestedEquivalenceClass<EquivalenceClass> merge(DefaultNestedEquivalenceClassesMergerParams<Integer> params) {
		// s_E=Es, Es.count<=Em.count; m_E=Em, Em.count>=Es.count
		NestedEquivalenceClass<EquivalenceClass> largerNestedEquClass, smallerNestedEquClass;
		if (params.getPreviousNestedEquClass().getItemSize()> params.getArrivedNestedEquClass().getItemSize()) {
			largerNestedEquClass = params.getPreviousNestedEquClass();
			smallerNestedEquClass = params.getArrivedNestedEquClass();
		}else {
			largerNestedEquClass = params.getArrivedNestedEquClass();
			smallerNestedEquClass = params.getPreviousNestedEquClass();
		}
		
		// flag = false
		boolean existBoundary = 
				ClassSetType.BOUNDARY.equals(largerNestedEquClass.getType())  ||
				ClassSetType.BOUNDARY.equals(smallerNestedEquClass.getType());
		// Go through Nested Equivalent Classes in s_E
		EquivalenceClass largerEquClass;
		boolean updateNestedEquivalentClass = false;
		for (Map.Entry<IntArrayKey, EquivalenceClass> samllEquClass:
				smallerNestedEquClass.getEquClasses().entrySet()
		) {
			// key = C(e)
			// b_E.count = b_E.count + e.count
			// if b_E doesn't contain key
			largerEquClass = largerNestedEquClass.getEquClasses()
												.get(samllEquClass.getKey());
			if (largerEquClass==null) {
				// Add e into b_E
				largerNestedEquClass.addClassItem(samllEquClass.getValue());
				// (Then, update nested equivalent class based on the added equivalent class): 
				// if b_E.cnst!=0
				if (!ClassSetType.BOUNDARY.equals(largerNestedEquClass.getType())) {
					// if (e.cnst==true && e.dec!=b_dec) or 
					//	  (e.cnst==false && b_cnst=1)
					//	b_E.dec=0
					//	flag=true
					if (samllEquClass.getValue().sortable()) {		// small Equ: 1
						if (samllEquClass.getValue().getDecisionValue()!=largerNestedEquClass.getDec()) {		
							// large NEC: 1-NEC/-1-NEC => 0-NEC
							largerNestedEquClass.setType(ClassSetType.BOUNDARY);
							if (!existBoundary)	existBoundary = true;
						}
					}else {								// small Equ: -1
						if (ClassSetType.POSITIVE.equals(largerNestedEquClass.getType())) {
							// large NEC: 1-NEC => 0-NEC
							largerNestedEquClass.setType(ClassSetType.BOUNDARY);
							if (!existBoundary)	existBoundary = true;
						}
					}
				}
			// else b_E contains key, a nested equivalent class already exists in b_E
			}else {
				// h.count = h.count + e.count
				largerNestedEquClass.addClassItem(samllEquClass.getValue());
				// if e.cnst==true
				// 	if e.dec!=b_E.dec
				//		true=>false, because the decision values are not equal
				boolean changed = largerEquClass.mergeClassItemsAndClassSetTypeHasChanged(samllEquClass.getValue());
				if (!updateNestedEquivalentClass && changed)	updateNestedEquivalentClass = true;
			}
		}
		
		// if updateFlag==true
		//	b_E.cnst, flag = updateConsistency(b_E)
		if (updateNestedEquivalentClass)	updateConsistency(largerNestedEquClass);
		
		return largerNestedEquClass;
	}
	
	/**
	 * Update the consistency(i.e. <code>nestedEquClass.type</code>) of
	 * {@link NestedEquivalenceClass}
	 * 
	 * @param nestedEquClass
	 * 		{@link NestedEquivalenceClass} to be updated.
	 */
	private void updateConsistency(NestedEquivalenceClass<EquivalenceClass> nestedEquClass) {
		Iterator<EquivalenceClass> iterator = nestedEquClass.getEquClasses().values().iterator();
		EquivalenceClass equClass = iterator.next();
		
		nestedEquClass.setDec(equClass.getDecisionValue());
		boolean boundary, sortable = equClass.sortable();
		while (iterator.hasNext()) {
			equClass = iterator.next();
			boundary = 
					(sortable && nestedEquClass.getDec()!=equClass.getDecisionValue()) ||
					(!sortable && equClass.sortable());
			// 0-NEC
			if (boundary) {
				nestedEquClass.setDec(-1);
				nestedEquClass.setType(ClassSetType.BOUNDARY);
				return;
			}
		}
		nestedEquClass.setType(sortable? ClassSetType.POSITIVE: ClassSetType.NEGATIVE);
	}
}
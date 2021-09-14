package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.evenlyDividenMultiAttribute;

import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;

/**
 * A Calculator for the number of attribute group elements for {@link AttributeProcessStrategy}.
 * <p>
 * This group number calculator calculate the number as even as possible.
 * 
 * @author Benjamin_L
 */
public interface GroupNumberCalculator {
	int calculate(AttributeProcessStrategy processor, Object...args);
}

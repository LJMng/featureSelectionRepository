package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;

/**
 * Before calling any other method, make sure attributes is set by calling {@link #initiate(IntegerIterator)}.
 * 
 * @author Benjamin_L
 */
public interface AttributeProcessStrategy {
	/**
	 * Initiate current instance and prepare for the given next attributes with no changes on
	 * parameters nor settings.
	 * 
	 * @param attributes
	 * 		{@link IntegerIterator} instance with attributes.
	 * @return {@link AttributeProcessStrategy}.
	 */
	AttributeProcessStrategy initiate(IntegerIterator attributes);
	
	int attributeLength();
	
	int left();
	boolean hasNext();
	int[] next();
	
	String shortName();
}
package featureSelection.repository.entity.alg.compactedDecisionTable.interf;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;

/**
 * An interface for Decision Number recorder.
 * 
 * @author Benjamin_L
 */
public interface DecisionNumber extends Cloneable {
	/**
	 * Get the number of the given decision value.
	 * 
	 * @param decision
	 * 		A decision value.
	 * @return The number of the decision value.
	 */
	int getNumberOfDecision(int decision);
	/**
	 * Set the number of the given decision value.
	 * 
	 * @param decision
	 * 		A decision value.
	 * @param number
	 * 		The number of the decision value.
	 */
	void setDecisionNumber(int decision, int number);
	
	/**
	 * Get decision values.
	 * 
	 * @return {@link IntegerIterator}.
	 */
	IntegerIterator decisionValues();
	/**
	 * Get the numbers of decision values.
	 * 
	 * @return {@link IntegerIterator}.
	 */
	IntegerIterator numberValues();
	DecisionNumber clone();
}
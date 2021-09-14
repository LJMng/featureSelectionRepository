package featureSelection.repository.entity.opt.improvedHarmonySearch.interf.calculation;

import lombok.Getter;

/**
 * For using Generation(NI) based dynamic Calculation. Generation(NI) should be added manually
 * before each calculation, and initiated as value 1:
 * <pre>
 * 	<code>preProcess(); // or nextGeneration();</code>
 * 	<code>ni = getCurrentGeneration();</code>
 * 	<code>//doSomething();</code>
 * 	<code>afterProcess();</code>
 * </pre>
 */
@Getter
public abstract class GenerationBasedDynamicCalculation {
	private int maxIteration;
	private int currentGeneration;
	
	public GenerationBasedDynamicCalculation(int maxIteration) {
		this.maxIteration = maxIteration;
		resetGeneration();
	}
	
	/**
	 * Reset generation: 
	 * <pre>
	 * currentGeneration = 1;
	 * </pre>
	 */
	protected void resetGeneration() {
		currentGeneration = 1;
	}
	
	/**
	 * Update to the next generation.
	 * 
	 * @return <code>this</code>.
	 */
	GenerationBasedDynamicCalculation nextGeneration() {
		if (currentGeneration>maxIteration)	throw new GenerationOutOfBoundException();
		else								currentGeneration++;
		return this;
	}
	
	/**
	 * Pre-process of calculation, i.e. update current generation: 
	 * <pre>nextGeneration();</pre>
	 */
	public void preProcess() 	{	nextGeneration();	}
	/**
	 * After-process of calculation.
	 * <p>PS: Currently, this method does nothing.
	 */
	public void afterProcess()	{	/* do nothing;	*/	}

	@SuppressWarnings("serial")
	class GenerationOutOfBoundException extends IllegalStateException {
		public GenerationOutOfBoundException() {
			super("Current generation will be greater than max Iteration if added: "+
					"currentGeneration("+currentGeneration+")>maxIterationNum("+
					maxIteration+")");
		}
	}
}

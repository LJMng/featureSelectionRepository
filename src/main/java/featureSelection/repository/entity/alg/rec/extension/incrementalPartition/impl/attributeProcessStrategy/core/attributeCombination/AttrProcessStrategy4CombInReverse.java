package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination;

import common.utils.StringUtils;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import org.apache.commons.math3.util.FastMath;

/**
 * Full name: AttributeProcessStrategy4CombinationInReverse (Attribute Process Strategy 4 Combination 
 * In Reverse)
 * <p>
 * Implemented {@link AttributeProcessStrategy} for Attributes combination outputs.
 * <p>
 * Initiate fields below as soon as <code>this</code> instance is <strong>constructed</strong>: 
 * <ul>
 * 	<li>attributes in line(<code>inLineAttr</code>)</li>
 * </ul>
 * <p>
 * Initiate <strong>fields below<strong> when first using</strong>: 
 * <ul>
 * 	<li>examining attributes in line(<code>examingLineAttr</code>)</li>
 * 	<li>examining attribute(<code>examingAttr</code>)</li>
 * </ul>
 * 
 * @see AttrProcessStrategy4Comb
 * 
 * @author Benjamin_L
 */
public class AttrProcessStrategy4CombInReverse 
	extends AttrProcessStrategy4Comb
	implements AttributeProcessStrategy
{
	public AttrProcessStrategy4CombInReverse(AttrProcessStrategyParams params) {
		super(params);
	}
	
	/**
	 * Reset the Attribute Process and prepare for new process. Settings will be reset and updated 
	 * based on the given parameters. Updated settings includes:
	 * <li>{@link #processCount}</li>
	 * <li>{@link #exchangeCount}</li>
	 * <li>{@link #allLength}</li>
	 * <li>{@link #examCapacity}
	 * 	<p>{@link #examCapacityCalculator} and the size of {@link #sourceAttr} are used to calculate
	 * 	the value of {@link #examCapacity}:
	 * 	<pre>this.examCapacity = FastMath.min(examCapacityCalculator.compute(this), sourceAttr.size());</pre>
	 * </li>
	 * <li><code>{@link #sourceAttr}.reset()</code></li>
	 * <li><code>{@link #examingLineAttr}=null</code></li>
	 * <li>{@link #inLineAttr}</li>
	 * 
	 * @param startInclusive
	 * 		Start index of the attributes. (inclusive).
	 * @param endExclusive
	 * 		End index of the attributes. (exclusive).
	 * @return <code>this</code> {@link AttrProcessStrategy4Comb}.
	 */
	@Override
	public void reset(int startInclusive, int endExclusive) {
		processCount = 0;
		exchangeCount = 0;
		allLength = endExclusive - startInclusive;

		this.examCapacity = FastMath.min(examCapacityCalculator.compute(this), sourceAttr.size());
		
		sourceAttr.reset().skip(startInclusive);
		initInLineAttr(sourceAttr, startInclusive);
		examingLineAttr = null;
	}
	
	private void initInLineAttr(IntegerIterator sourceAttr, int startInclusive) {
		inLineAttr = new int[allLength-examCapacity];
		for (int i=0; i<inLineAttr.length; i++)			inLineAttr[i] = sourceAttr.next();
	}
	
	private void initExamingAttrIfNull(IntegerIterator sourceAttr) {
		if (examingLineAttr==null) {
			if (sourceAttr.currentIndex()!=inLineAttr.length)	sourceAttr.reset().skip(inLineAttr.length);
			examingAttr = sourceAttr.next();
			examingLineAttr = new int[examCapacity-1];
			for (int i=0; i<examingLineAttr.length; i++)	examingLineAttr[i] = sourceAttr.next();
		}
	}

	@Override
	public void updateInLineAttrs() {
		int left=left();
		initExamingAttrIfNull(sourceAttr);
		if (left>examCapacity) {
			int tmp = examingAttr, inLineAttrIndex = inLineAttr.length-processCount;
			examingAttr = inLineAttr[inLineAttrIndex];
			inLineAttr[inLineAttrIndex] = tmp;
			exchange(inLineAttrIndex);

			exchangeCount = 0;
		}else if (left>0) {
			int[] newInLineAttr = new int[allLength-left];
			newInLineAttr[0] = examingAttr;
			for (int i=0; i<examingLineAttr.length; i++)
				newInLineAttr[i+1] = examingLineAttr[i];
			for (int i=examCapacity; i<newInLineAttr.length; i++)
				newInLineAttr[i] = inLineAttr[i-examCapacity+left];
			
			examingAttr = inLineAttr[0];
			if (left>1) {
				examingLineAttr = new int[left-1];
				for (int i=0; i<examingLineAttr.length; i++)
					examingLineAttr[i] = inLineAttr[i+1];
			}else {
				examingLineAttr = new int[0];
			}

			inLineAttr = newInLineAttr;
			exchangeCount = 0;
		}
	}

	@Override
	public boolean hasNextExamAttribute() {
		initExamingAttrIfNull(sourceAttr);
		return exchangeCount<examCapacity && processCount<allLength;
	}
	
	public void updateExamAttribute() {
		initExamingAttrIfNull(sourceAttr);
		if (!hasNextExamAttribute())
			throw new RuntimeException("Exchange index out of bound: "+exchangeCount);//*/
		
		int result = examingAttr;
		
		/*System.out.println("result: "+result);
		System.out.println("examingLineAttr: "+Arrays.toString(examingLineAttr));
		System.out.println("inLineAttr: "+Arrays.toString(inLineAttr));//*/
		
		if (exchangeCount<examingLineAttr.length) {
			examingAttr = examingLineAttr[exchangeCount];
			examingLineAttr[exchangeCount] = result;
		}
		
		processCount++;
		exchangeCount++;
	}
	
	@Override
	public int getExamingAttr() {
		initExamingAttrIfNull(sourceAttr);
		return examingAttr;
	}
	
	@Override
	public int[] getExamingLineAttr() {
		initExamingAttrIfNull(sourceAttr);
		return examingLineAttr;
		
	}
	
	private void exchange(int inLineStartIndex) {
		int tmp, inLineAttrIndex;
		for (int i=0; i<examingLineAttr.length; i++) {
			inLineAttrIndex = inLineStartIndex+1+i;
			tmp = examingLineAttr[i];
			examingLineAttr[i] = inLineAttr[inLineAttrIndex];
			inLineAttr[inLineAttrIndex] = tmp;
		}
	}

	public String toString() {
		return String.format("AttributeProcessStrategy4CombinationInReverse [capacity=%d, examing=%d + %s, line=%s]", 
					examCapacity, examingAttr, 
					StringUtils.intToString(examingLineAttr, 50), 
					StringUtils.intToString(inLineAttr, 50)
				);
	}
}
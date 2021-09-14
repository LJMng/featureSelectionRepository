package featureSelection.repository.support.calculation.asitKDasFitness;

import java.util.Collection;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.dynamics.instance.asitKDasIncremental.fitness.FitnessEvaluationParameters;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

/**
 * A fitness evaluation in <strong>Asit.K.Das</strong>'s IFS, based on the Article 
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S1568494618300462">
 * "A group incremental feature selection for classification using rough set theory based
 * genetic algorithm"</a> by Asit.K.Das, Shampa Sengupta, Siddhartha Bhattacharyya.
 * 
 * @see #evaluateFitness(int, Collection, FitnessEvaluationParameters, int, Number, IntegerIterator)
 * 
 * @author Benjamin_L
 */
public abstract class DefaultAsitKDasFitnessCalculation<FI extends FeatureImportance<Sig>, Sig extends Number>
	implements FeatureImportance<Double>
{
	public static final String CALCULATION_NAME = "Asit.K.Das Fitness Cal.";
	
	@Getter private int attributeLength;
	@Getter private Collection<Integer> reductB4DataArrived;
	@Getter private FitnessEvaluationParameters fitnessEvalParams;
	
	@Getter private FI featureImportance;
	
	public DefaultAsitKDasFitnessCalculation(
			int attributeLength, Collection<Integer> reductB4DataArrived,
			FitnessEvaluationParameters fitnessEvalParams,
			FI featureImportance
	) {
		this.attributeLength = attributeLength;
		this.reductB4DataArrived = reductB4DataArrived;
		this.fitnessEvalParams = fitnessEvalParams;
		this.featureImportance = featureImportance;
	}
	
	@Getter protected long calculationTimes = 0;
	@Getter protected long calculationAttributeLength = 0;
	
	public void countCalculate(int attrLen) {
		calculationTimes++;
		calculationAttributeLength += attrLen;
	}
	
	@Override
	public Double plus(Double v1, Double v2) {
		throw new RuntimeException("Unimplemented method!");
	}
	
	@Override
	public boolean value1IsBetter(Double v1, Double v2, Double deviation) {
		return Double.compare((v1==null?0:v1) - (v2==null?0:v2), deviation)>0;
	}
	
	/**
	 * Fitness evaluation, based on the formular:
	 * <pre>
	 * <strong>fitness</strong> = w * ( <strong>sig</strong>^k / |A1|^z ) +
	 * 	(1-w) * (intersection of newReduct & redB4DataArrived) / |A|
	 * 
	 *  w, k, z: parameters set by user in {@link FitnessEvaluationParameters}
	 *  A: complete attributes of {@link Instance}.
	 *  R1: a reduct of the data before new one arriving.
	 *  A1: a reduct of the new data arrived.
	 *  
	 *  originally, sig=intersection(pos(U[new], A), pos(U[new], A1))/|U[new]|, where U[new] is the data 
	 *  arrived and intersection(pos(U[new], A), pos(U[new], A1))
	 * </pre>
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param attributeLength
	 * 		The number of {@link Instance} attributes.
	 * @param reductB4DataArrived
	 * 		The reduct of {@link Instance} before new ones arrived.
	 * @param fitnessEvalParams
	 * 		{@link FitnessEvaluationParameters} with parameters(<code>w</code>, <code>k</code>, 
	 * 		<code>z</code>) for the fitness evaluation.
	 * @param dataArrivedNum
	 * 		The number of {@link Instance}(data) arrived.
	 * @param significance4DataArrived
	 * 		Significance value of {@link Instance}(data) arrived with its reduct.
	 * @param newReduct
	 * 		New reduct after {@link Instance}(data) arrived.
	 * @return fitness evaluation result.
	 */
	public static <Sig extends Number> double evaluateFitness(
			int attributeLength, Collection<Integer> reductB4DataArrived, 
			FitnessEvaluationParameters fitnessEvalParams,
			int dataArrivedNum, Sig significance4DataArrived, IntegerIterator newReduct
	) {
		// fitness = w * ( Sig(dataArrived)^k / |newReduct|^z) +
		//			(1-w) * (intersection of newReduct & redB4DataArrived) / |A|
		return newReduct.size()==0?
				0:
				(fitnessEvalParams.getW() * 
					FastMath.pow(
						significance4DataArrived.doubleValue(),
						fitnessEvalParams.getK()
					) / FastMath.pow(newReduct.size(), 2)
				+
				(1-fitnessEvalParams.getW()) * 
					countIntersectionOf(reductB4DataArrived, newReduct) / (double) attributeLength
				);
	}
	
	/**
	 * Count the element number of intersection of <code>c1</code> & <code>c2</code>.
	 * 
	 * @param c1
	 * 		An {@link Integer} {@link Collection}.
	 * @param c2
	 * 		An {@link IntegerIterator}.
	 * @return the element number of intersection.
	 */
	private static int countIntersectionOf(Collection<Integer> c1, IntegerIterator c2) {
		int count = 0;
		c2.reset();	for (int i=0; i<c2.size(); i++)	if (c1.contains(c2.next()))	count++;
		return count;
	}

	public abstract Sig getFeatureImportanceValue();
}
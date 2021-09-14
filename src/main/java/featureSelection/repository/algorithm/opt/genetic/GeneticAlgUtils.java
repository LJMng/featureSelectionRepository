package featureSelection.repository.algorithm.opt.genetic;

import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double4AsitKDas;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GeneticAlgUtils {
	
	public static class ComparingBestFitness {
		public static <Chr extends Chromosome<?>, FValueType extends Number,
						FValue extends FitnessValue<FValueType>>
			int common(
				Fitness<Chr, FValue> fitness, GenerationRecord<Chr, FValue> geneRecord
		) {
			Number v1, v2;
			if (fitness==null ||
					fitness.getFitnessValue()==null ||
					fitness.getFitnessValue().getValue()==null
			) {
				v1 = 0.0;
			}else {
				v1 = fitness.getFitnessValue().getValue();
			}

			if (geneRecord==null ||
					geneRecord.getBestFitness()==null ||
					geneRecord.getBestFitness().getValue()==null
			) {
				v2 = 0.0;
			}else {
				v2 = geneRecord.getBestFitness().getValue();
			}

			return Double.compare(v1.doubleValue(), v2.doubleValue());
		}
	}
	
	public static class ComparingMaxFitness {
		public static <Chr extends Chromosome<?>, FValueType extends Number,
						FValue extends FitnessValue<FValueType>>
			int common(
					FValue fitnessValue, GenerationRecord<Chr, FValue> geneRecord
		) {
			Number v1, v2;
			if (fitnessValue==null || fitnessValue.getValue()==null)
				v1 = 0.0;
			else
				v1 = fitnessValue.getValue();
			if (geneRecord==null || geneRecord.getBestFitness()==null || geneRecord.getBestFitness().getValue()==null)
				v2 = 0.0;
			else 
				v2 = geneRecord.getBestFitness().getValue();
			return Double.compare(v1.doubleValue(), v2.doubleValue());
		}
		
		public static <Chr extends Chromosome<?>> int asitKDas(
					FitnessValue4Double4AsitKDas<Double> maxFitness,
					GenerationRecord<Chr, FitnessValue4Double4AsitKDas<Double>> geneRecord
		) {
			Number v1, v2;
			if (maxFitness==null || maxFitness.getFeatureSignificance()==null)
				v1 = 0.0;
			else
				v1 = maxFitness.getFeatureSignificance();
			if (geneRecord==null || geneRecord.getBestFitness()==null || geneRecord.getBestFitness().getFeatureSignificance()==null)
				v2 = 0.0;
			else 
				v2 = geneRecord.getBestFitness().getFeatureSignificance();
			return Double.compare(v1.doubleValue(), v2.doubleValue());
		}
	}
}

package featureSelection.tester.procedure.opt.genetic.procedure.common.gaInitiate;

import java.util.Collection;

import common.utils.LoggerUtil;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.ReductionParameters;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double4AsitKDas;
import featureSelection.repository.entity.opt.genetic.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.asitKDasFitness.DefaultAsitKDasFitnessCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Initialization of <strong>Genetic Algorithm</strong> specialised for Asit.K.Das GA Feature Selection.
 * Mainly, This {@link ProcedureContainer} contains 1 step:
 * <ul>
 *  <li>
 *  	<strong>GA basic initialization</strong>
 *  	<p>Create an instance of {@link FeatureImportance}, a {@link GenerationRecord} and calculate
 *  		the max fitness if needed
 *  </li>
 * </ul>
 * <p>
 * Different from {@link GeneticAlgorithmInitiateProcedureContainer}, this one initiate
 * using the created the instance of {@link FeatureImportance} with previous reduct and other info 
 * to support calculations for data arrived. 
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 *  <li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 *  <li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 *  <li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 *  <li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * </ul>
 * <p>
 * Among them, {@link DefaultAsitKDasFitnessCalculation} instance should be set in
 * {@link ProcedureParameters} by user using associated key
 * {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}.
 * 
 * @see DefaultAsitKDasFitnessCalculation
 * @see GeneticAlgorithmInitiateProcedureContainer
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <CollectionItem>
 * 		Universe or EquivalenceClass.
 * @param <Chr>
 * 		Type of Implemented {@link Chromosome}.
 * @param <FValue>
 * 		Type of {@link Comparable} extended fitness value.
 */
@Slf4j
public class GeneticAlgorithmInitiate4AsitKDasAlgsProcedureContainer<Cal extends DefaultAsitKDasFitnessCalculation<? extends FeatureImportance<Double>, Double>,
																	CollectionItem,
																	Chr extends Chromosome<?>,
																	FValue extends FitnessValue<?>>
	extends GeneticAlgorithmInitiateProcedureContainer<Cal, Double, CollectionItem, Chr, FValue>
{
	public GeneticAlgorithmInitiate4AsitKDasAlgsProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(parameters, logOn);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. GA basic initialization.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						
						Collection<Instance> instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						getParameters().setNonRoot(
								ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS, 
								instances
							);
						
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								instances,
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Cal calculation =
								(Cal) parameters[p++];
						ReductionParameters<Cal, Double, CollectionItem, Chr, FValue> gaParams =
								(ReductionParameters<Cal, Double, CollectionItem, Chr, FValue>) 
								parameters[p++];
						Collection<CollectionItem> collectionList =
								(Collection<CollectionItem>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						ReductionAlgorithm<Cal, Double, CollectionItem, Chr, FValue> redAlg =
								gaParams.getReductionAlgorithm();

						// Initiate max fitness for ReductionParameters.
						if (gaParams.getMaxFitness()==null || gaParams.getMaxFitness().getValue()==null) {
							gaParams.setMaxFitness(
									redAlg.calculateFitness(calculation, collectionList, attributes)
							);
						}
						return new Object[] {
								redAlg,
								new GenerationRecord<>(),
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						ReductionAlgorithm<Cal, Double, CollectionItem, Chr, FValue> redAlg = 
								(ReductionAlgorithm<Cal, Double, CollectionItem, Chr, FValue>) 
								result[r++];
						GenerationRecord<Chr, FValue> geneRecord = (GenerationRecord<Chr, FValue>) result[r++];
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							ReductionParameters<?, ?, ?, ?, ?> gaParams = 
									getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
							
							FitnessValue4Double4AsitKDas<Double> fv = (FitnessValue4Double4AsitKDas<Double>)gaParams.getMaxFitness();
							log.info(LoggerUtil.spaceFormat(2, "Max fitness = {}"), 
									fv==null? "null": 
									fv.getFeatureSignificance() instanceof Double?
									String.format("%.4f", fv.getFeatureSignificance()):
									fv.getFeatureSignificance()
							);
						}
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM, redAlg);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD, geneRecord);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder().loadCurrentInfo(instances)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("GA basic initialization"),
		};
	}
}
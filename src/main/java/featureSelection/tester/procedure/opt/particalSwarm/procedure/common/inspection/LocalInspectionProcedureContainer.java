package featureSelection.tester.procedure.opt.particalSwarm.procedure.common.inspection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.utils.ArrayCollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position.BytePosition4RealAttributeReflected;
import featureSelection.repository.entity.opt.particleSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Particle Swarm Optimization</strong> Final reduct inspection execution. Mainly, this 
 * contains only 1 {@link ProcedureComponent}:
 * <ul>
 * 	<li>
 * 		<strong>Inspection</strong>
 * 		<p>Use the local inspection method in {@link ProcedureParameters} to execute. 
 * 	</li>
 * </ul>
 * 
 * @see FeatureImportance
 * @see SkipLocalInspectionProcedureContainer
 * @see SkipLocalInspectionProcedureContainer4AsitKDasAlgs
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of Feature(subset) Significance.
 * @param <CollectionItem> 
 * 		{@link Instance} or EquivalentClass
 * @param <Velocity> 
 * 		Type of implemented {@link Number} as values of velocity
 * @param <Posi> 
 * 		Type of implemented {@link Position}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class LocalInspectionProcedureContainer<Cal extends FeatureImportance<Sig>,
												Sig extends Number, 
												CollectionItem,
												Velocity, 
												Posi extends Position<?>,
												FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<Map<IntArrayKey, Collection<OptimizationReduct>>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	public LocalInspectionProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "PSO Inspection(Local inspection method)";
	}
	
	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Inspection.
			new TimeCountedProcedureComponent<Map<IntArrayKey, Collection<OptimizationReduct>>>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Velocity, Posi, FValue> params =
								(ReductionParameters<Velocity, Posi, FValue>)
								parameters[p++];
						ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig> redAlg =
								(ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig>) 
								parameters[p++];
						Cal calculation =
								(Cal) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						Collection<CollectionItem> collection =
								(Collection<CollectionItem>) parameters[p++];
						GenerationRecord<Velocity, Posi, FValue> geneRecord =
								(GenerationRecord<Velocity, Posi, FValue>) 
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Map<IntArrayKey, Collection<OptimizationReduct>> reducts =
								new HashMap<>(geneRecord.countDistinctBestFitness());

						if (geneRecord.getGlobalBestFitness()!=null) {
							int[] redArray;
							IntArrayKey key;
							Collection<Integer> redAfterInspection;
							Collection<OptimizationReduct> optimizationReducts;
							for (Fitness<Posi, FValue> f: geneRecord.getGlobalBestFitnessCollection()) {
								// Load actual attributes.
								TimerUtils.timePause((TimeCounted) component);
								
								redArray = new int[f.getPosition().getAttributes().length];
								for (int a=0; a<redArray.length; a++) {
									redArray[a] = params.getAttributes()[f.getPosition().getAttributes()[a]];
								}

								TimerUtils.timeContinue((TimeCounted) component);

								redAfterInspection = redAlg.inspection(
															calculation, sigDeviation, 
															collection, redArray
														);
									
								TimerUtils.timePause((TimeCounted) component);
									
								if (redAfterInspection.size()!=redArray.length) {
									redArray = ArrayCollectionUtils.getIntArrayByCollection(redAfterInspection);
								}
								Arrays.sort(redArray);
								key = new IntArrayKey(redArray);
									
								optimizationReducts = reducts.get(key);
								if (optimizationReducts==null)	reducts.put(key, optimizationReducts=new LinkedList<>());
								optimizationReducts.add(
									new OptimizationReduct(
										new BytePosition4RealAttributeReflected(
											(Position<byte[]>) f.getPosition(), params.getAttributes()
										),
										redAfterInspection
									)
								);
								TimerUtils.timeContinue((TimeCounted) component);
							}
						}
						return reducts;
					}, 
					(component, reducts) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, reducts);
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|reduct| = {}"), reducts.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP,
							reducts
						);
						//	[STATISTIC_ITERATION_INFOS]
						GenerationRecord<Velocity, Posi, FValue> geneRecord = 
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						if (geneRecord.getGlobalBestFitness()!=null) {
							ReductionParameters<Velocity, Posi, FValue> params = 
									getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
							
							ProcedureUtils.Statistics.IterationInfos.markSolutions(
								ProcedureUtils
									.Statistics
									.IterationInfos
									.collectGlobalBestEntityMap(
										getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS),
										geneRecord.getGlobalBestFitnessCollection().size(), 
										params.isInspectReductInGreedySearch()
									), 
								reducts.values()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Inspection"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<IntArrayKey, Collection<OptimizationReduct>> exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (Map<IntArrayKey, Collection<OptimizationReduct>>) componentArray[0].exec();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}
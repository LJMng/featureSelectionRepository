package featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.streaming.inspection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.utils.ArrayCollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position.BytePosition4RealAttributeReflected;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Particle Swarm Optimization</strong> Final reduct inspection execution.
 * Mainly, this contains 2 {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Inspection</strong>
 * 		<p>Skip inspection step, wrap particles into reducts. 
 * 	</li>
 * 	<li>
 * 		<strong>Return reducts</strong>
 * 		<p>Return inspected reducts. 
 * 	</li>
 * </ul>
 * 
 * @see FeatureImportance
 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition.Inspection
 * 
 * @author Benjamin_L
 *
 * @param <Velocity>
 * 		Type of {@link Particle}'s Velocity.
 * @param <Posi>
 * 		Type of implemented {@link Position}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class IPNECSkipInspectionProcedureContainer4Streaming<Velocity,
															Posi extends Position<?>,
															FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<Object[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;

	public IPNECSkipInspectionProcedureContainer4Streaming(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "GA Inspection(IP-NEC inspection method)";
	}
	
	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Stream Inspection.
			new ProcedureComponent<Object[]>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
						});
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Velocity, Posi, FValue> params =
								(ReductionParameters<Velocity, Posi, FValue>) parameters[p++];
						GenerationRecord<Velocity, Posi, FValue> geneRecord =
								(GenerationRecord<Velocity, Posi, FValue>) 
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						int[] redArray;
						IntArrayKey key;
						Collection<Integer> redAfterInspection;
						Collection<OptimizationReduct> optimizationReducts;
						
						Map<IntArrayKey, Collection<OptimizationReduct>> result = new HashMap<>(
								FastMath.max(16, geneRecord.countDistinctBestFitness())
						);
						
						for (Fitness<Posi, FValue> f: geneRecord.getGlobalBestFitnessCollection()) {
							// Load actual attributes.
							redArray = new int[f.getPosition().getAttributes().length];
							for (int a=0; a<redArray.length; a++)
								redArray[a] = params.getAttributes()[f.getPosition().getAttributes()[a]];
							
							redAfterInspection = new ArrayList<>(redArray.length);
							for (int attr: redArray)	redAfterInspection.add(attr);

							redArray = ArrayCollectionUtils.getIntArrayByCollection(redAfterInspection);
							Arrays.sort(redArray);
							key = new IntArrayKey(redArray);
							
							optimizationReducts = result.get(key);
							if (optimizationReducts==null)	result.put(key, optimizationReducts=new LinkedList<>());
							optimizationReducts.add(
								new OptimizationReduct(
									new BytePosition4RealAttributeReflected(
										(Position<byte[]>) f.getPosition(), params.getAttributes()
									), 
									redAfterInspection
								)
							);
						}
						return new Object[] {
								result, false
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Map<IntArrayKey, Collection<OptimizationReduct>> redMap = (Map<IntArrayKey, Collection<OptimizationReduct>>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, redMap);
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|reduct| = {}"), redMap.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
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
								redMap.values()
							);
						}
						//	[STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP,
							redMap
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Stream Inspection"),
		};
	}

	@Override
	public Object[] exec() throws Exception {
		Object result = null;
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
			
			result = each.exec();
		}
		return (Object[]) result;
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}

package featureSelection.tester.procedure.opt.particalSwarm.procedure.common.inspection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import common.utils.ArrayCollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position.BytePosition4RealAttributeReflected;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import featureSelection.repository.support.calculation.asitKDasFitness.DefaultAsitKDasFitnessCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Particle Swarm Optimization</strong> Final reduct inspection execution. Mainly, this 
 * contains 2 {@link ProcedureComponent}s:
 * <ul>
 *  <li>
 *  	<strong>Inspection</strong>
 *  	<p>Use the local inspection method in {@link ProcedureParameters} to execute.
 *  </li>
 *  <li>
 *  	<strong>Return reducts.</strong>
 *  	<p>Return inspected reducts.
 *  </li>
 * </ul>
 * 
 * @see FeatureImportance
 * @see LocalInspectionProcedureContainer
 * @see SkipLocalInspectionProcedureContainer
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link DefaultAsitKDasFitnessCalculation}.
 * @param <CollectionItem> 
 * 		{@link Instance} or EquivalentClass
 * @param <Velocity> 
 * 		Type of implemented {@link Number} as values of velocity
 * @param <Posi> 
 * 		Type of implemented {@link Position}.
 */
@Slf4j
public class SkipLocalInspectionProcedureContainer4AsitKDasAlgs<Cal extends DefaultAsitKDasFitnessCalculation<? extends FeatureImportance<Double>, Double>,
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
	
	public SkipLocalInspectionProcedureContainer4AsitKDasAlgs(ProcedureParameters parameters, boolean logOn) {
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
			new ProcedureComponent<Map<IntArrayKey, Collection<OptimizationReduct>>>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
						});
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Velocity, Posi, FValue> params =
								(ReductionParameters<Velocity, Posi, FValue>)
								parameters[p++];
						GenerationRecord<Velocity, Posi, FValue> geneRecord =
								(GenerationRecord<Velocity, Posi, FValue>) 
								parameters[p++];
						Collection<Integer> previousReduct = (Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						Map<IntArrayKey, Collection<OptimizationReduct>> reducts =
								new HashMap<>(geneRecord.countDistinctBestFitness());

						int[] redArray;
						IntArrayKey key;
						OptimizationReduct optReduct;
						Collection<Integer> redAfterInspection;
						Collection<OptimizationReduct> optimizationReducts;
						// Asit.K.Das returns only 1 reduct(i.e. geneRecord.globalBestFitness).
						Fitness<Posi, FValue> fitness = geneRecord.getGlobalBestFitness();
						// Load actual attributes.
						redArray = new int[fitness.getPosition().getAttributes().length];
						for (int a=0; a<redArray.length; a++)
							redArray[a] = params.getAttributes()[fitness.getPosition().getAttributes()[a]];

						redAfterInspection = new ArrayList<>(redArray.length);
						for (int attr: redArray)	redAfterInspection.add(attr);
							
						optReduct = new OptimizationReduct(
										new BytePosition4RealAttributeReflected(
											(Position<byte[]>) fitness.getPosition(), params.getAttributes()
										), 
										redAfterInspection
									);
						
						redAfterInspection = new HashSet<>(redAfterInspection);
						for (int attr: previousReduct)		redAfterInspection.add(attr);
							
						if (redAfterInspection.size()!=redArray.length) {
							redArray = ArrayCollectionUtils.getIntArrayByCollection(redAfterInspection);
							Arrays.sort(redArray);
						}
						key = new IntArrayKey(redArray);
								
						optimizationReducts = reducts.get(key);
						if (optimizationReducts==null)	reducts.put(key, optimizationReducts=new LinkedList<>());
						optimizationReducts.add(optReduct);

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
		Object result = null;
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
			
			result = each.exec();
		}
		return (Map<IntArrayKey, Collection<OptimizationReduct>>) result;
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}
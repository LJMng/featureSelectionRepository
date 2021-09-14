package featureSelection.tester.procedure.opt.genetic.procedure.common.inspection;

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
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Genetic Algorithm</strong> Final reduct inspection execution. Mainly, this contains only 1  
 * {@link ProcedureComponent}:
 * <ul>
 * <li>
 * 	<strong>Inspection</strong>
 * 	<p>Skip inspection step, wrap Chromosomes into reducts. 
 * </li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_GENERATION_RECORD}</li>
 * </ul>
 * 
 * @see FeatureImportance
 * @see LocalInspectionProcedureContainer
 *
 * @author Benjamin_L
 *
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <Chr>
 * 		Type of Implemented {@link Chromosome}.
 * @param <FValue>
 * 		Type of {@link Comparable} extended fitness value.
 */
@Slf4j
public class SkipInspectionProcedureContainer4AsitKDas<Sig extends Number,
														Chr extends Chromosome<?>,
														FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<Object[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;

	public SkipInspectionProcedureContainer4AsitKDas(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "GA Inspection(Asit K Das Skip inspection)";
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
			new ProcedureComponent<Object[]>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
						});
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						GenerationRecord<Chr, FValue> geneRecord =
								(GenerationRecord<Chr, FValue>) parameters[p++];
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						Map<IntArrayKey, Collection<OptimizationReduct>> result =
								new HashMap<>(geneRecord.getFitness().size());
						
						int[] redArray;
						IntArrayKey key;
						Collection<Integer> redAfterInspection;
						Collection<OptimizationReduct> optimizationReducts;
						for (Fitness<Chr, FValue> fitness : geneRecord.getFitness()) {
							if ((redArray=fitness.getChromosome().getAttributes()).length==0){
								continue;
							}
								
							if (previousReduct!=null) {
								redAfterInspection = new HashSet<>(redArray.length+previousReduct.size());
								redAfterInspection.addAll(previousReduct);
								for (int attr: redArray){
									redAfterInspection.add(attr);
								}
								redArray = ArrayCollectionUtils.getIntArrayByCollection(redAfterInspection);
							}else{
								// No previous reduct, redArray is the final reduct.
								redAfterInspection = new ArrayList<>(redArray.length);
								for (int attr: redArray){
									redAfterInspection.add(attr);
								}
							}

							Arrays.sort(redArray);
							key = new IntArrayKey(redArray);
								
							optimizationReducts = result.get(key);
							if (optimizationReducts==null){
								result.put(key, optimizationReducts=new LinkedList<>());
							}
							optimizationReducts.add(new OptimizationReduct(fitness.getChromosome(), redAfterInspection));
						}
						return new Object[] {	result	};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Map<IntArrayKey, Collection<OptimizationReduct>> reducts = (Map<IntArrayKey, Collection<OptimizationReduct>>) result[r++];
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
						ProcedureUtils.Statistics.IterationInfos.markSolutions(
							ProcedureUtils
								.Statistics
								.IterationInfos
								.collectGlobalBestEntityMap(
									getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS), 1, false
								), 
							reducts.values()
						);
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

	@Override
	public Object[] exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (Object[]) componentArray[0].exec();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}
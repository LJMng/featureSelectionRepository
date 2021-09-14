package featureSelection.tester.procedure.opt.genetic.procedure.common.inspection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
import featureSelection.tester.report.ReportConstants;
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
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_GENERATION_RECORD}</li>
 * </ul>
 * 
 * @see FeatureImportance
 * @see LocalInspectionProcedureContainer
 * @see SkipInspectionProcedureContainer4AsitKDas
 *
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <CollectionItem>
 * 		Universe or EquivalentClass.
 * @param <Chr>
 * 		Type of Implemented {@link Chromosome}.
 * @param <FValue>
 * 		Type of {@link Comparable} extended fitness value.
 */
@Slf4j
public class SkipInspectionProcedureContainer<Cal extends FeatureImportance<Sig>,
												Sig extends Number, 
												CollectionItem,
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

	public SkipInspectionProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "GA Inspection(Skip inspection)";
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
						});
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						GenerationRecord<Chr, FValue> geneRecord = (GenerationRecord<Chr, FValue>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						Map<IntArrayKey, Collection<OptimizationReduct>> result = new HashMap<>(geneRecord.getFitness().size());
						
						int[] redArray;
						IntArrayKey key;
						Collection<Integer> redAfterInspection;
						Collection<OptimizationReduct> optimizationReducts;
						for (Fitness<Chr, FValue> fitness : geneRecord.getFitness()) {
							if ((redArray=fitness.getChromosome().getAttributes()).length==0){
								continue;
							}
								
							redAfterInspection = new ArrayList<>(redArray.length);
							for (int attr: redArray){
								redAfterInspection.add(attr);
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
						GenerationRecord<Chr, FValue> geneRecord =
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						//	[STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP]
						statistics.put(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP, reducts);
						//	[STATISTIC_ITERATION_INFOS]
						ProcedureUtils.Statistics.IterationInfos.markSolutions(
							ProcedureUtils
								.Statistics
								.IterationInfos
								.collectGlobalBestEntityMap(
									getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS),
									geneRecord.getFitness().size(), false
								), 
							reducts.values()
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_OPTIMIZATION_SIG_CALCULATION_TIMES]
						ProcedureUtils.Report
								.saveItem(report, 
										component.getDescription(),
										ReportConstants.Procedure.REPORT_OPTIMIZATION_EXIT_REDUCT_SIZE,
										reducts.size()
						);
						//	[REPORT_OPTIMIZATION_CURRENT_MAX_FITNESS]
						ProcedureUtils.Report
								.saveItem(report, 
										component.getDescription(),
										ReportConstants.Procedure.REPORT_OPTIMIZATION_CURRENT_MAX_FITNESS,
										reducts.isEmpty()? 0: geneRecord.getBestFitness().getValue()
						);
						//	[REPORT_OPTIMIZATION_SIG_CALCULATION_TIMES]
						ProcedureUtils.Report
								.saveItem(report, 
										component.getDescription(),
										ReportConstants.Procedure.REPORT_OPTIMIZATION_EXIT_ITERATION,
										geneRecord.getGeneration()
						);
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
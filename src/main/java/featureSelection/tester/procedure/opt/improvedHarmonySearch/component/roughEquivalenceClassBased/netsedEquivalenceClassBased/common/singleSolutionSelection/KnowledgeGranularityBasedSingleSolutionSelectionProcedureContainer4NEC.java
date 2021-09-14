package featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.singleSolutionSelection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
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
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.support.calculation.knowledgeGranularity.DefaultKnowledgeGranularityBasedObjectiveCalculation;
import featureSelection.repository.support.calculation.knowledgeGranularity.roughEquivalenceClassBased.KnowledgeGranularityCalculation4NEC;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Improved Harmony Search Algorithm</strong> Final single solution selection. Mainly, this
 * contains only 1 {@link ProcedureComponent}:
 * <ul>
 * 	<li>
 * 		<strong>Single solution selection controller</strong>
 * 		<p>Use <i>Knowledge Granularity</i> based objective function to select the best solution as the final
 * 			single solution.
 * 	</li>
 * </ul>
 * <p>
 * In this {@link DefaultProcedureContainer}(only!), the following parameters are used in 
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST_AFTER_INSPECTATION}</li>
 * </ul>
 * 
 * @see KnowledgeGranularityCalculation4NEC
 * @see DefaultKnowledgeGranularityBasedObjectiveCalculation
 * 
 * @author Benjamin_L
 */
@Slf4j
public class KnowledgeGranularityBasedSingleSolutionSelectionProcedureContainer4NEC
	extends DefaultProcedureContainer<int[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;

	public KnowledgeGranularityBasedSingleSolutionSelectionProcedureContainer4NEC(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "IHS Solution Selection";
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
			// 1. Single solution selection controller.
			new TimeCountedProcedureComponent<int[]>(
					ComponentTags.TAG_SOLUTION_SELECTION,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Map<IntArrayKey, Collection<OptimizationReduct>> newReducts =
								(Map<IntArrayKey, Collection<OptimizationReduct>>) parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						int[] previousReduct =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						KnowledgeGranularityCalculation4NEC gpCalculation =
								new KnowledgeGranularityCalculation4NEC();
						DefaultKnowledgeGranularityBasedObjectiveCalculation objectiveFuncCalculation =
								new DefaultKnowledgeGranularityBasedObjectiveCalculation();
						
						int[] bestReduct = null;
						double bestObjectiveValue = 0, calObjetiveValue;
						
						for (IntArrayKey reduct: newReducts.keySet()) {
							int[] redArray = reduct.getKey();
							if (// Initiate bestChromosomesGranularity
								bestReduct==null ||
								//	Not equal to best reduct
								redArray.length != bestReduct.length || !Arrays.equals(redArray, bestReduct)
							) {
								// reduct = reduct ∪ newReduct
								int[] fullReduct;
								if (previousReduct==null) {
									fullReduct = redArray;
								}else {
									fullReduct = Arrays.copyOf(previousReduct, previousReduct.length+redArray.length);
									for (int r=previousReduct.length, j=0; r<fullReduct.length; r++, j++)
										fullReduct[r] = redArray[j];
								}
								
								calObjetiveValue = 
									objectiveFuncCalculation.calculate(
										instances, new IntegerArrayIterator(fullReduct), gpCalculation
									).getResult();
								if (bestReduct==null || calObjetiveValue > bestObjectiveValue) {
									bestObjectiveValue = calObjetiveValue;
									bestReduct = redArray;
								}
							}
						}
						/* ------------------------------------------------------------------------------ */
						return bestReduct;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Single solution selection controller"),	
		};
	}

	@Override
	public int[] exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (int[]) componentArray[0].exec();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}
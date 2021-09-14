package featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.netsedEquivalenceClassBased.common.singleSolutionSelection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.iteration.optimization.BasicIterationInfo4Optimization;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.OptEntityBasicInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Particle Swarm Optimization</strong> Final single solution selection.
 * Mainly, this contains only 1 {@link ProcedureComponent}:
 * <ul>
 *  <li>
 *  	<strong>Single solution selection controller</strong>
 *  	<p>Use <i>Knowledge Granularity</i> based objective function to select the best solution as
 *      	the final single solution.
 *  </li>
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
		return "PSO Solution Selection";
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
						
						if (newReducts.keySet().size()==1) {
							bestReduct = newReducts.keySet().iterator().next().key();
						}else {
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
										for (int r=previousReduct.length, j=0; r<fullReduct.length; r++, j++) {
											fullReduct[r] = redArray[j];
										}
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
						}
						/* ------------------------------------------------------------------------------ */
						return bestReduct;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						List<BasicIterationInfo4Optimization<?>> iterInfos =
								getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);
						
						Collection<Integer> solutionSet = Arrays.stream(result).boxed().collect(Collectors.toSet());
						
						boolean found = false;
						for (int i=iterInfos.size()-1; i>=0; i--) {
							boolean breakLoop = false;
							// Loop over entity info.s and filter inspected ones with the same full length 
							//	(previous reduct included) as the solution's.
							// Searching should only carry out among those latest global best.
							for (OptEntityBasicInfo<?> entityInfo: iterInfos.get(i).getOptimizationEntityBasicInfo()) {
								// If finished searching in the latest global best, break loop.
								if (entityInfo.getSupremeMark()!=null && 
									entityInfo.getSupremeMark().getRank()==1L
								) {
									breakLoop = true;
								}
								
								if (entityInfo.getInspectedReduct()!=null &&
									entityInfo.getInspectedReduct().size()==result.length &&
									solutionSet.containsAll(entityInfo.getInspectedReduct())
								) {
									// set solution.
									entityInfo.setSolution(true);
									if (!found)	found = true;
								}
							}
							if (breakLoop)	break;
						}
						if (!found) {
							throw new IllegalStateException(
									"Selected solution not found in iter info.s: "+Arrays.toString(result)
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
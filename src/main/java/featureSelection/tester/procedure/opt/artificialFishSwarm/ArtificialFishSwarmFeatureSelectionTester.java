package featureSelection.tester.procedure.opt.artificialFishSwarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.container.SelectiveComponentsProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.support.reductMiningStrategy.optimization.ArtificialFishSwarmReductMiningStrategy;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.artificialFishSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.fsaInitiate.ArtificialFishSwarmInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop.DefaultGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Artificial Fish Swarm Algorithm</strong> <code>PLUS</code> 
 * <strong>Feature Selection</strong>. Check out the original paper
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0950705115000337">
 * "Finding rough set reducts with fish swarm algorithm"</a> 
 * by Yumin Chen. et al.
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 4 
 * {@link ProcedureComponent}s, referring to steps:
 * <ul>
 *  <li>
 *  	<strong>Initiate</strong>:
 *  	<p>Some initializations for Artificial Fish Swarm Algorithm.
 *  	<p><code>ArtificalFishSwarmInitiateProcedureContainer</code>,
 *  	<p><code>ArtificalFishSwarmInitiateProcedureContainer4REC</code>
 *  </li>
 *  <li>
 *  	<strong>Generation loops</strong>:
 *  	<p>Loop generations, initiate fish group, "study" and fish exits if it finds a reduct. Loop
 * 	    	until reaching maximum iteration.
 * 	    <p><code>DefaultGenerationLoopProcedureContainer</code>
 *  </li>
 *  <li>
 *  	<strong>Fishes to reducts</strong>:
 * 	    <p>Transfer fishes to reducts.
 *  </li>
 * </ul>
 * <p>
 * In this {@link SelectiveComponentsProcedureContainer}(only!), the following parameters are used in 
 * {@link #getParameters()}:
 * <p><i>NONE</i>
 * 
 * @author Benjamin_L
 * 
 * @see ArtificialFishSwarmInitiateProcedureContainer
 * @see DefaultGenerationLoopProcedureContainer
 *
 * @param <FitnessValue>
 * 		Type of fitness value.
 * @param <PosiValue>
 * 		Type of position value for {@link Position}.
 * @param <Posi>
 * 		Type of implemented {@link Position}.
 */
@Slf4j
public class ArtificialFishSwarmFeatureSelectionTester<FitnessValue,
														PosiValue, 
														Posi extends Position<PosiValue>>
	extends SelectiveComponentsProcedureContainer<Map<IntArrayKey, Collection<OptimizationReduct>>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				StatisticsCalculated,
				ArtificialFishSwarmReductMiningStrategy
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	private String[] componentExecOrder;
	
	public ArtificialFishSwarmFeatureSelectionTester(ProcedureParameters parameters, boolean logOn) {
		super(logOn, parameters);
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Opt("+
					ProcedureUtils.ShortName.optimizationAlgorithm(getParameters())+"-"+
					ProcedureUtils.ShortName.calculation(getParameters())+
				")";
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
	public void initDefaultComponents(boolean logOn) {
		ProcedureComponent<?>[] componentArray = new ProcedureComponent<?>[] {
			// 1. Initiate.
			new ProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("1. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (Object[]) component.getSubProcedureContainers().values().iterator().next().exec();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
					}
				){
					@Override public void init() {}
	
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate")
			.setSubProcedureContainer(
				"ArtificialFishSwarmInitiateProcedureContainer", 
				new ArtificialFishSwarmInitiateProcedureContainer<>(getParameters(), logOn)
			),
			// 2. Generation loops.
			new ProcedureComponent<GenerationRecord<Posi, FitnessValue>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (GenerationRecord<Posi, FitnessValue>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					},
					(component, geneRecord)->{
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
		
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Generation loops")
			.setSubProcedureContainer(
				"GenerationLoopProcedureContainer", 
				new DefaultGenerationLoopProcedureContainer<>(getParameters(), logOn)
			),
			// 3. Fishes to reducts.
			new ProcedureComponent<Map<IntArrayKey, Collection<OptimizationReduct>>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
						});
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						GenerationRecord<Posi, FitnessValue> generRecord = 
								(GenerationRecord<Posi, FitnessValue>) 
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						Map<IntArrayKey, Collection<OptimizationReduct>> result = new HashMap<>();
						
						Collection<Integer> redCollection;
						Collection<OptimizationReduct> optimizationReducts;
						
						int[] redArray;
						IntArrayKey key;
						for (Posi posi: generRecord.getBestFitnessPosition()) {
							redArray = posi.getAttributes();
							redCollection = new ArrayList<>(redArray.length);
							for (int each: redArray)	redCollection.add(each);
							Arrays.sort(redArray);
							key = new IntArrayKey(redArray);	
							optimizationReducts = result.get(key);
							if (optimizationReducts==null)	result.put(key, optimizationReducts=new LinkedList<>());
							optimizationReducts.add(new OptimizationReduct(posi, redCollection));
						}
						return result;
					}, 
					(component, reducts) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|reduct| = {}"), reducts.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP]
						statistics.put(
								StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_REDUCT_CODE_MAP,
								reducts
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Fishes to reducts"),
		};
		//	Component order.
		componentExecOrder = new String[componentArray.length];
		for (int i=0; i<componentArray.length; i++) {
			this.setComponent(componentArray[i].getDescription(), componentArray[i]);
			componentExecOrder[i] = componentArray[i].getDescription();
		}
	}
	
	@Override
	public String[] componentsExecOrder() {
		return componentExecOrder;
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		String[] keys = new String[this.getComponents().size()];
		int i=0;
		for (ProcedureComponent<?> comp: this.getComponents())	keys[i++] = comp.getDescription();
		return keys;
	}

	@Override
	public long getTime() {
		long total = 0;
		for (ProcedureComponent<?> component : this.getComponents())
			total += ProcedureUtils.Time.sumProcedureComponentTimes(component);
		return total;
	}

	@Override
	public Map<String, Long> getTimeDetailByTags() {
		return ProcedureUtils.Time.sumProcedureComponentsTimesByTags(this);
	}
}
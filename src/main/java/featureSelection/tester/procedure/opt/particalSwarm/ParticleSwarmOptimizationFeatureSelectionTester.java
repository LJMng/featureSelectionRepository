package featureSelection.tester.procedure.opt.particalSwarm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.utils.CollectionUtils;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.container.SelectiveComponentsProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.support.reductMiningStrategy.optimization.ParticleSwarmOptimizationReductMiningStrategy;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.generationLoop.DefaultGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.generationLoop.GlobalBestPrioritizedGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.generationLoop.ReductPrioritizedGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.inspection.LocalInspectionProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.inspection.SkipLocalInspectionProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.particleInitiate.ParticleInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.psoInitiate.ParticleSwarmOptimizationInitiateProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.original.inspection.IPRECInspectionProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.original.psoInitiate.ParticleSwarmOptimizationInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Particle Swarm Optimization</strong> <code>PLUS</code> 
 * <strong>Feature Selection</strong>.
 * <p>
 * Check out the original paper <a href="https://linkinghub.elsevier.com/retrieve/pii/S0169260713003477">
 * "Supervised hybrid feature selection based on PSO and rough sets for medical diagnosis"</a> 
 * by H.Hannah Inbarani, Ahmad Taher Azar, G. Jothi.
 * <p>
 * (<strong>NOTICE</strong>: 
 * However, some modifications have been made to reduce the execution time in <i>Generation loops</i>.
 * Check out the specific <i>Generation loops</i> {@link ProcedureContainer} to see if any change has
 * been made and seek for details.)
 * <p>
 * This is a {@link SelectiveComponentsProcedureContainer}. Procedure contains 5
 * {@link ProcedureComponent}s, referring to steps:
 * <ul>
 * 	<li>
 * 		<strong>Initiate</strong>: 
 * 		<p>Some initializations for Particle Swarm Optimization.
 * 		<p><code>ParticleSwarmOptimizationInitiateProcedureContainer</code>,  
 * 		<p><code>ParticleSwarmOptimizationInitiateProcedureContainer4REC</code>
 * 	</li>
 * 	<li>
 * 		<strong>Initiate particles</strong>: 
 * 		<p>Initiate particles randomly.
 * 		<p><code>ParticleInitiateProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Generation loops</strong>: 
 * 		<p>Loop generations, calculate particles' fitness, "study", until reaching maximum
 * 			iteration/maximum convergence/maximum fitness.
 * 		<p><code>DefaultGenerationLoopProcedureContainer</code>
 * 		<p><code>GlobalBestPrioritizedGenerationLoopProcedureContainer</code>
 * 		<p><code>ReductPrioritizedGenerationLoopProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Particles to reducts</strong>:
 * 		<p>Transfer particles to reducts.
 * 		<p><code>LocalInspectionProcedureContainer</code>,
 * 		<p><code>IPRECInspectionProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Return reducts</strong>:
 * 		<p>Return reducts.
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 *  <li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_ALGORITHM}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_GENERATION_RECORD}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_DEVIATION}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_REDUCT_LIST_AFTER_INSPECTATION}</li>
 * 	<li>particle</li>
 * </ul>
 * 
 * @author Benjamin_L
 * 
 * @see ParticleSwarmOptimizationInitiateProcedureContainer
 * @see ParticleSwarmOptimizationInitiateProcedureContainer4REC
 * @see ParticleInitiateProcedureContainer
 * @see DefaultGenerationLoopProcedureContainer
 * @see ReductPrioritizedGenerationLoopProcedureContainer
 * @see GlobalBestPrioritizedGenerationLoopProcedureContainer
 * @see LocalInspectionProcedureContainer
 * @see IPRECInspectionProcedureContainer
 *
 * @param <Velocity>
 * 		Type of {@link Particle}'s Velocity.
 * @param <Posi>
 * 		Type of implemented {@link Position}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class ParticleSwarmOptimizationFeatureSelectionTester<Velocity,
															Posi extends Position<?>,
															FValue extends FitnessValue<?>>
	extends SelectiveComponentsProcedureContainer<Map<IntArrayKey, Collection<OptimizationReduct>>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				StatisticsCalculated,
				ParticleSwarmOptimizationReductMiningStrategy
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	private String[] componentExecOrder;
	
	public ParticleSwarmOptimizationFeatureSelectionTester(ProcedureParameters parameters, boolean logOn) {
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
						return (Object[])
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
	
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate")
				.setSubProcedureContainer(
					"ParticleSwarmOptimizationInitiateProcedureContainer", 
					new ParticleSwarmOptimizationInitiateProcedureContainer<>(getParameters(), logOn)
				),
			// 2. Initiate particles.
			new ProcedureComponent<Particle<Velocity, Posi, FValue>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (Particle<Velocity, Posi, FValue>[])
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, chromosome)->{
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
	
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate particles")
				.setSubProcedureContainer(
					"ParticleInitiateProcedureContainer", 
					new ParticleInitiateProcedureContainer<>(getParameters(), logOn)
				),
			// 3. Generation loops.
			new ProcedureComponent<GenerationRecord<Velocity, Posi, FValue>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (GenerationRecord<Velocity, Posi, FValue>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, geneRecord)->{
						/* ------------------------------------------------------------------------------ */
						// Report
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
//					new DefaultGenerationLoopProcedureContainer<>(getParameters(), logOn)
					new ReductPrioritizedGenerationLoopProcedureContainer<>(getParameters(), logOn)
//					new GlobalBestPrioritizedGenerationLoopProcedureContainer<>(getParameters(), logOn)
				),
			// 4. Particles to reducts.
			new ProcedureComponent<Map<IntArrayKey, Collection<OptimizationReduct>>>(
					ComponentTags.TAG_INSPECT_LAST,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get("particle"),
						});
					},
					(component, parameters) -> {
						return (Map<IntArrayKey, Collection<OptimizationReduct>>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
										.exec();
					},
					(component, result)->{
						getParameters().setNonRoot("result", result);
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Particles to reducts")
				.setSubProcedureContainer(
					"InspectionProcedureContainer", 
					//new IPRECInspectionProcedureContainer<>(getParameters(), logOn)
//					new LocalInspectionProcedureContainer<>(getParameters(), logOn)
					new SkipLocalInspectionProcedureContainer<>(getParameters(), logOn)
				),
			// 5. Return reducts.
			new ProcedureComponent<Map<IntArrayKey, Collection<OptimizationReduct>>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("5. "+component.getDescription());
					},
					(component, parameters) -> {
						return getParameters().get("result");
					},
					null
				) {
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Return reducts"),
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
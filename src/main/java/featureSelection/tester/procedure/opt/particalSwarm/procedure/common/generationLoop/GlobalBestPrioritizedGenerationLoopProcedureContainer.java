package featureSelection.tester.procedure.opt.particalSwarm.procedure.common.generationLoop;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.opt.particleSwarm.func.ParticleSwarm4StaticData;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.psoGreedySearch.ParticleGreedySearchProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.psoGreedySearch.SkipParticleGreedySearchProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.ExitInfo;
import featureSelection.tester.statistics.info.iteration.optimization.BasicIterationInfo4Optimization;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.SupremeMark;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Particle Swarm Optimization</strong> loop executions. 
 * <p>
 * >> "best fitness == max fitness" <strong>NOT</strong> guaranteed. Not equal because of exiting when 
 * reaching max convergence or max iteration.
 * <p>
 * Mainly, this procedure container contains ProcedureComponents: 
 * <ul>
 * 	<li>
 * 		<strong>Generation loop procedure controller</strong>:
 * 		<p>Different from {@link DefaultGenerationLoopProcedureContainer}
 * 		<p>Only the global best fitness with current best fitness value and least attributes will be preserved. 
 * 			Global best fitnesses with the same fitness value as current global best fitness value with 
 * 			less/equal attribute number will all be deserted. Finally, when <strong>exiting loop(i.e. reaching 
 * 			max convergence or max iteration)</strong>, only <strong>1 global best fitness</strong> which 
 * 			contains the global best fitness value and least attributes will be preserved and returned.
 * 	 <p>
 * 	 >> Only <strong>1 global best fitness</strong> is preserved and returned.
 * 	 <p>
 * 	 >> {@link ReductionParameters#getMaxDistinctBestFitness()} <strong>ignored</strong>.
 * 	</li>
 * 	<li>
 * 		<strong>Calculate fitnesses of particles</strong>
 * 		<p>To calculate particles' fitness.
 * 	</li>
 * 	<li>
 * 		<strong>Update individual best fitnesses of particles</strong>
 * 		<p>Update particles' individual best fitness, i.e. if the new finess happens to be better, update the 
 * 			particles with the fitness and the position it represents.
 * 	</li>
 * 	<li>
 * 		<strong>Search alternative attributes for current codings of particles</strong>
 * 		<p>Using greedy search to search for alternative attributes for every current particle.
 * 	</li>
 * 	<li>
 * 		<strong>Check if break for reaching max convergence: true-break, false-continue.</strong>
 * 	</li>
 * 	<li>
 * 		<strong>Update particles' velocity & position</strong>
 * 		<p>Using the given particle velocity updating algorithm and position updating algorithm to update 
 * 			particles.
 * 	</li>
 * </ul>
 * 
 * @see FeatureImportance
 * @see ParticleGreedySearchProcedureContainer
 *
 * @see DefaultGenerationLoopProcedureContainer
 * @see ReductPrioritizedGenerationLoopProcedureContainer
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <CollectionItem>
 * 		Universe or EquivalentClass.
 * @param <Velocity>
 * 		Type of {@link Particle}'s Velocity.
 * @param <Posi>
 * 		Type of implemented {@link Position}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class GlobalBestPrioritizedGenerationLoopProcedureContainer<Cal extends FeatureImportance<Sig>,
																	Sig extends Number, 
																	CollectionItem,
																	Velocity,
																	Posi extends Position<?>,
																	FValue extends FitnessValue<?>>
	extends DefaultGenerationLoopProcedureContainer<Cal, Sig, CollectionItem, Velocity, Posi, FValue>
{
	public GlobalBestPrioritizedGenerationLoopProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(parameters, logOn);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Generation loop procedure controller.
			new TimeCountedProcedureComponent<GenerationRecord<Velocity, Posi, FValue>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int componentSize = this.getComponents().size();
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Velocity, Posi, FValue> params =
								(ReductionParameters<Velocity, Posi, FValue>)
								parameters[p++];
						GenerationRecord<Velocity, Posi, FValue> geneRecord = 
								(GenerationRecord<Velocity, Posi, FValue>)
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						boolean exit, convergence = false;
						do {
							geneRecord.nextGeneration();
							TimerUtils.timePause((TimeCounted) component);
							
							for (int i=1; i<componentSize; i++) {
								if (i==4) {
									convergence = (Boolean) this.getComponents().get(i).exec();
									if (convergence) {
										TimerUtils.timeContinue((TimeCounted) component);
										return geneRecord;
									}
								}else {
									this.getComponents().get(i).exec();
								}
							}
							TimerUtils.timeContinue((TimeCounted) component);
							
							exit =	// if any is satisfied
									//		reaching max convergence
									(convergence || 
									//		reaching max iteration
									 geneRecord.getGeneration() >= params.getIteration()
									);
						}while (!exit);
						return geneRecord;
					}, 
					(component, geneRecord) -> {
						/* ------------------------------------------------------------------------------ */
						// Preserve only 1 global best fitness.
						geneRecord.getGlobalBestFitnessCollection().clear();
						geneRecord.getGlobalBestFitnessCollection().add(geneRecord.getGlobalBestFitness());
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD, geneRecord);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_EXIT_REASON]
						if (statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON)==null) {
							statistics.put(
								StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON,
								ExitInfo.EXIT_MARK_ITERATION
							);
						}
						//	[STATISTIC_OPTIMIZATION_EXIT_ITERATION]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_ITERATION,
							geneRecord.getGeneration()
						);
						//	[STATISTIC_OPTIMIZATION_EXIT_ITERATION]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_FITNESS,
							geneRecord.getGlobalBestFitness().getFitnessValue().getValue()
						);
						//	[STATISTIC_OPTIMIZATION_EXIT_ITERATION]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_CONVERGENCE,
							geneRecord.getConvergenceCount()
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
			}.setDescription("Generation loop procedure controller"),
			// 2. Calculate fitnesses of particles.
			new TimeCountedProcedureComponent<Fitness<Posi, FValue>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get("particle"),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig> redAlg =
								(ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig>)
								parameters[p++];
						Cal calculation =
								(Cal) parameters[p++];
						Collection<CollectionItem> collectionList =
								(Collection<CollectionItem>) parameters[p++];
						Particle<Velocity, Posi, FValue>[] particle =
								(Particle<Velocity, Posi, FValue>[]) parameters[p++];
						ReductionParameters<?, ?, FValue> params =
								(ReductionParameters<?, ?, FValue>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return redAlg.fitness(calculation, collectionList, params.getAttributes(), particle);
					}, 
					(component, particleFitness) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("particleFitness", particleFitness);
						/* ------------------------------------------------------------------------------ */
						GenerationRecord<Velocity, Posi, FValue> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(1, "Generation {}"), geneRecord.getGeneration());
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						ReductionParameters<?, ?, FValue> params = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						Collection<CollectionItem> collectionItems = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS);
						Particle<Velocity, Posi, FValue>[] particle = getParameters().get("particle");
						
						getParameters().setNonRoot(
							StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS,
							ProcedureUtils.Statistics.IterationInfos.pushInfoOfIteration(
									statistics.getData(),
									params.getIteration(), geneRecord.getGeneration(), collectionItems, 
									params.getAttributes(),
									Arrays.stream(particle).map(p->p.getPosition()).toArray(Position[]::new), 
									Arrays.stream(particleFitness).map(f->f.getPosition().getAttributes()).iterator(), 
									Arrays.stream(particleFitness).map(f->f.getFitnessValue().getValue()).toArray(Number[]::new)
							)
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
			}.setDescription("Calculate fitnesses of particles"),
			// 3. Update individual best fitnesses of particles.
			new TimeCountedProcedureComponent<boolean[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "3. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM),
								getParameters().get("particle"),
								getParameters().get("particleFitness"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig> redAlg =
								(ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig>)
								parameters[p++];
						Particle<Velocity, Posi, FValue>[] particle =
								(Particle<Velocity, Posi, FValue>[]) parameters[p++];
						Fitness<Posi, FValue>[] particleFitness =
								(Fitness[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return ParticleSwarm4StaticData.updateParticleIndividualBestFitness(
								particle, redAlg, particleFitness
							);
					}, 
					(component, individualUpdated) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("individualUpdated", individualUpdated);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						Fitness<Posi, FValue>[] particleFitness = getParameters().get("particleFitness");
						
						List<BasicIterationInfo4Optimization<Number>> iterInfos =
								getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);
						BasicIterationInfo4Optimization<?> iterInfo = iterInfos.get(iterInfos.size()-1);
						for (int i=0; i<particleFitness.length; i++) {
							if (individualUpdated[i]) {
								iterInfo.getOptimizationEntityBasicInfo()[i]
										.setSupremeMark(SupremeMark.nextLocalBest());
							}
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update individual best fitnesses of particles"),
			// 4. Search alternative attributes for current codings of particles.
			new ProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					null, 
					(component, parameters) -> {
						return (Boolean) (
									(Object[])
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
													.exec()
								)[0];
					}, 
					(component, resetConvergenceCount) -> {
						/* ------------------------------------------------------------------------------ */
						localParameters.put("resetConvergenceCount", resetConvergenceCount);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Search alternative attributes for current codings of particles")
				.setSubProcedureContainer(
					"ParticleGreedySearchProcedureContainer", 
					new SkipParticleGreedySearchProcedureContainer<>(getParameters(), logOn)
//					new ParticleGreedySearchProcedureContainer<>(getParameters(), logOn)
				),
			// 5. Check if break for reaching max convergence: true-break, false-continue.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "5. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								localParameters.get("resetConvergenceCount"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						GenerationRecord<Velocity, Posi, FValue> geneRecord =
								(GenerationRecord<Velocity, Posi, FValue>)
								parameters[p++];
						ReductionParameters<Velocity, Posi, FValue> params = 
								(ReductionParameters<Velocity, Posi, FValue>)
								parameters[p++];
						boolean resetConvergenceCount =
								(boolean) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						if (!resetConvergenceCount) {
							// if the best value remains unchanged, count <code>conNum</code>
							geneRecord.countConvergence();
						}else {
							// Reset <code>conNum</code> for best value having changed.
							geneRecord.resetConvergence();
						}
						// if the best value remains unchanged <code>conNum</code> times, 
						//	considered converged.
						return geneRecord.getConvergenceCount() >= params.getConvergence();
					}, 
					(component, breakLoop) -> {
						/* ------------------------------------------------------------------------------ */
						GenerationRecord<Velocity, Posi, FValue> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						if (logOn) {
							if (geneRecord.getGlobalBestFitness()!=null) {
								log.info(LoggerUtil.spaceFormat(2, "best fitness = {}"), geneRecord.getGlobalBestFitness().getFitnessValue().getValue());
							}else {
								log.info(LoggerUtil.spaceFormat(2, "best fitness = null"));
							}
							log.info(LoggerUtil.spaceFormat(2, "convergence = {}"), geneRecord.getConvergenceCount());
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						ProcedureUtils.Statistics.IterationInfos
									.updateConvergenceInfo(
										statistics.getData(),
										geneRecord.getConvergenceCount()
									);
						//	[STATISTIC_OPTIMIZATION_EXIT_REASON]
						if (breakLoop)
							statistics.put(
								StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON,
								ExitInfo.EXIT_MARK_CONVERGENCE
							);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Check if break for reaching max convergence"),
			// 6. Update particles' velocity & position.
			new TimeCountedProcedureComponent<Particle<Velocity, Posi, FValue>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "6. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get("particle"),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_RANDOM_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Particle<Velocity, Posi, FValue>[] particle =
								(Particle<Velocity, Posi, FValue>[]) parameters[p++];
						GenerationRecord<Velocity, Posi, FValue> geneRecord = 
								(GenerationRecord<Velocity, Posi, FValue>) 
								parameters[p++];
						Random random =
								(Random) parameters[p++];
						ReductionParameters<Velocity, Posi, FValue> params = 
								(ReductionParameters<Velocity, Posi, FValue>)
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Loop over particles and update positions as well as velocities. 
						for (int i=0; i<particle.length; i++) {
							particle[i] = 
								params.getParticleUpdateAlgorithm()
									.updateVelocityNPosition(
											particle[i],
											geneRecord, 
											params, 
											random
								);
						}
						return particle;
					}, 
					(component, particle) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("particle", particle);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
											
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update particles' velocity & position"),
		};
	}
}
package featureSelection.tester.utils;

import common.utils.StringUtils;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.record.PlainRecord;
import featureSelection.tester.statistics.record.PlainRecordItem;
import featureSelection.tester.statistics.record.RecordFieldInfo;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class StatisticsUtils {

	/**
	 * Utilities for titles of statistics output file.
	 *
	 * @author Benjamin_L
	 */
	public static class Title {

		/**
		 * Transform the given {@link Collection} into {@link Map} whose keys are
		 * {@link RecordFieldInfo#getField()} and values are {@link RecordFieldInfo}
		 * themselves.
		 *
		 * @param titles A {@link Collection} of {@link RecordFieldInfo} to be transformed.
		 * @return Transformed {@link Map}.
		 */
		public static Map<String, RecordFieldInfo> toMap(Collection<RecordFieldInfo> titles) {
			return titles.stream().collect(Collectors.toMap(RecordFieldInfo::getField, t -> t));
		}

		/**
		 * Add a title into <code>titles</code>.
		 * <p>
		 * {@link RecordFieldInfo} is created based on the given info.s and then
		 * is added into <code>titles</code>.
		 * <p>
		 * Comparing to {@link #titleAdd(Collection, String, String, String, String)},
		 * <code>dbTable</code>, <code>dbField</code> is set to null when creating
		 * {@link RecordFieldInfo} instance.
		 *
		 * @param titles Titles in {@link RecordFieldInfo} {@link Collection}.
		 * @param field  The name of the field.
		 * @param desc   Description of the field.
		 * @see #titleAdd(Collection, String, String, String, String)
		 */
		private static void titleAdd(
				Collection<RecordFieldInfo> titles, String field, String desc
		) {
			titles.add(new RecordFieldInfo(field, desc, null, null));
		}

		/**
		 * Add a title into <code>titles</code>.
		 * <p>
		 * {@link RecordFieldInfo} is created based on the given info.s and then
		 * is added into <code>titles</code>.
		 *
		 * @param titles  Titles in {@link RecordFieldInfo} {@link Collection}.
		 * @param field   The name of the field.
		 * @param desc    Description of the field.
		 * @param dbTable The correspondent database table.
		 * @param dbField The correspondent database table field.
		 * @see #titleAdd(Collection, String, String)
		 */
		private static void titleAdd(
				Collection<RecordFieldInfo> titles,
				String field, String desc, String dbTable, String dbField
		) {
			titles.add(new RecordFieldInfo(field, desc, dbTable, dbField));
		}

		private static class Common {
			/**
			 * Add titles for basic info. of a record, including:
			 * <ul>
			 * 	<li>DATETIME
			 * 		<p> The date time info., usually is the date time of saving the statistics.
			 * 		<p>{@link StatisticsConstants.PlainRecordInfo#DATETIME}
			 * 	</li>
			 * 	<li>CONTAINER_ID:
			 * 		<p> The ID of the main execution {@link ProcedureContainer}.
			 * 		<p>{@link StatisticsConstants.PlainRecordInfo#CONTAINER_ID}
			 * 	</li>
			 * 	<li>DATABASE_UNIQUE_ID:
			 * 		<p> The ID of the executed dataset.
			 * 		<p>{@link StatisticsConstants.PlainRecordInfo#DATABASE_UNIQUE_ID}
			 * 	</li>
			 * </ul>
			 *
			 * @param titles A {@link List} of {@link RecordFieldInfo} to be loaded with titles.
			 */
			private static void addRecordBasicInfo(List<RecordFieldInfo> titles) {
				// DATETIME
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.DATETIME, "日期时间");
				// CONTAINER_ID
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.CONTAINER_ID, "记录ID");
				// DATABASE_UNIQUE_ID
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.DATABASE_UNIQUE_ID,
						"数据库记录ID",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "UNIQUE_ID"
				);
			}

			/**
			 * Add titles for dataset and algorithm basic info., including:
			 * <li>DATASET_ID
			 * <p> The ID of the dataset.
			 * <p>{@link StatisticsConstants.PlainRecordInfo#DATASET_ID}
			 * </li>
			 * <li>DATASET
			 * <p> The name of the dataset.
			 * <p>{@link StatisticsConstants.PlainRecordInfo#DATASET_NAME}
			 * </li>
			 * <li>ORIGINAL_UNIVERSE_SIZE
			 * <p> The number of {@link Instance}/instance executed.
			 * <p>{@link StatisticsConstants.PlainRecordInfo#ORIGINAL_UNIVERSE_SIZE}
			 * </li>
			 * <li>PURE_UNIVERSE_SIZE
			 * <p> The number of non-repeated {@link Instance}/instance executed:
			 * <strong>|U|<strong>
			 * <p>{@link StatisticsConstants.PlainRecordInfo#PURE_UNIVERSE_SIZE}
			 * </li>
			 * <li>UNIVERSE_PURE_RATE
			 * <p> The rate of non-repeated {@link Instance}/instance executed:
			 * rate = |U/C| / |U|.
			 * <p>{@link StatisticsConstants.PlainRecordInfo#UNIVERSE_PURE_RATE}
			 * </li>
			 * <li>ATTRIBUTE_SIZE
			 * <p> The number of condition attributes a {@link Instance} contains:
			 * <strong>|C|<strong>
			 * <p>{@link StatisticsConstants.PlainRecordInfo#ATTRIBUTE_SIZE}
			 * </li>
			 * <li>ATTRIBUTE_LIST
			 * <p> The list of condition attributes a {@link Instance}: <strong>C<strong>
			 * <p>{@link StatisticsConstants.PlainRecordInfo#ATTRIBUTE_LIST}
			 * </li>
			 * <li>DECISION_VALUE_NUMBER
			 * <p> The number of non-repeated decision values of the executed
			 * {@link Instance}: <strong>|D|<strong>
			 * <p>{@link StatisticsConstants.PlainRecordInfo#DECISION_VALUE_NUMBER}
			 * </li>
			 *
			 * @param titles A {@link List} of {@link RecordFieldInfo} to be loaded with titles.
			 */
			private static void addDatasetAlgorithmInfo(List<RecordFieldInfo> titles) {
				/* ----------------------------------------------------------------------------------- */
				// DATASET_ID
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.DATASET_ID,
						"数据集ID",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "DATASET_ID"
				);
				// DATASET
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.DATASET_NAME,
						"数据集名字",
						StatisticsConstants.Database.TABLE_DATASET, "NAME"
				);
				/* ----------------------------------------------------------------------------------- */
				// ORIGINAL_UNIVERSE_SIZE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.ORIGINAL_UNIVERSE_SIZE,
						"数据集原始记录个数",
						StatisticsConstants.Database.TABLE_DATASET, "INFO_UNIVERSE"
				);
				// PURE_UNIVERSE_SIZE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.PURE_UNIVERSE_SIZE,
						"数据集压缩后记录个数"
				);
				// UNIVERSE_PURE_RATE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.UNIVERSE_PURE_RATE,
						"数据集压缩后记录占比=数据集压缩后记录个数/数据集原始记录个数",
						StatisticsConstants.Database.TABLE_DATASET, "INFO_COMPRESS_RATE"
				);
				// EXEC_INSTANCE_SIZE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.EXEC_INSTANCE_SIZE,
						"数据集计算域(i.e.实际计算记录个数)",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "INFO_EXECUTE_INSTANCE_NUM"
				);
				// EXEC_INSTANCE_UNIT
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.EXEC_INSTANCE_UNIT,
						"数据集计算域单位",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "INFO_EXECUTE_INSTANCE_UNIT"
				);
				// ATTRIBUTE_SIZE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.ATTRIBUTE_SIZE,
						"数据集条件属性个数",
						StatisticsConstants.Database.TABLE_DATASET, "INFO_CONDITION_ATTRIBUTE"
				);
				// ATTRIBUTE_LIST
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.ATTRIBUTE_LIST,
						"数据集条件属性序列列表"
				);
				// DECISION_VALUE_NUMBER
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.DECISION_VALUE_NUMBER,
						"数据集决策属性个数",
						StatisticsConstants.Database.TABLE_DATASET, "INFO_DECISION_ATTRIBUTE_NUMBER"
				);
				/* ----------------------------------------------------------------------------------- */
			}

			private static void addCommonRuntimeInfo(List<RecordFieldInfo> titles) {
				// ALG_ID
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.ALG_ID,
						"算法ID",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "ALGORITHM_ID"
				);
				// ALG
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.ALG,
						"算法简称",
						StatisticsConstants.Database.TABLE_ALGORITHM, "NAME"
				);
				// PARAMETER_ID
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.PARAMETER_ID,
						"算法参数ID",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "PARAMETER_ID"
				);
				// TOTAL_TIME
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TOTAL_TIME,
						"记录所用时间(ms)",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "TIME_TOTAL"
				);
				// PURE_TIME
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.PURE_TIME,
						"除去检验时间的记录所用时间(ms)=记录所用时间-检验时间"
				);
				// RUN_TIMES
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.RUN_TIMES,
						"相同实验重复跑的次数",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "TIMES"
				);
			}

			private static void addCalculationInfo(List<RecordFieldInfo> titles) {
				// CALCULATION_TIMES
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.CALCULATION_TIMES,
						"sum(Cal.计算次数)"
				);
				// CALCULATION_ATTR_LEN
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.CALCULATION_ATTR_LEN,
						"sum(Cal.计算属性长度)"
				);
			}

			private static void addPositiveRegionRemoving(List<RecordFieldInfo> titles) {
				// SIGNIFICANCE_HISTORY
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.SIGNIFICANCE_HISTORY,
						"SIG记录"
				);
				// SIGNIFICANCE_SUM
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.SIGNIFICANCE_SUM,
						"sum(SIG记录)"
				);
				// SIGNIFICANCE_AVERAGE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.SIGNIFICANCE_AVERAGE,
						"SIG增加平均数 = ~记录 / ~次数"
				);

				// remove POS(U)：TOTAL_UNIVERSE_POS_REMOVE_NUMBER_HISTORY
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_POS_REMOVE_NUMBER_HISTORY,
						"删减POS(U)",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "POS_REMOVE_U_LIST"
				);
				// Sum(remove POS(U))：TOTAL_UNIVERSE_POS_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_POS_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE,
						"删减POS(U)总数",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "POS_REMOVE_U_SIZE"
				);
				// Eva(remove POS(U))：TOTAL_UNIVERSE_POS_REMOVE_NUMBER_EVALUATION
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_POS_REMOVE_NUMBER_EVALUATION,
						"删减POS(U)贡献评估=删减数/删减前剩余数",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "POS_REMOVE_U_EVAL"
				);

				// remove NEG(U)：TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_HISTORY
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_HISTORY,
						"删减NEG(U)",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "NEG_REMOVE_U_LIST"
				);
				// Sum(remove NEG(U))：TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE,
						"删减NEG(U)总数",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "NEG_REMOVE_U_SIZE"
				);
				// Eva(remove NEG(U))：TOTAL_UNIVERSE_POS_REMOVE_NUMBER_EVALUATION
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_EVALUATION,
						"删减NEG(U)贡献评估=删减数/删减前剩余数",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "NEG_REMOVE_U_EVAL"
				);

				// remove U：TOTAL_UNIVERSE_REMOVE_NUMBER_HISTORY
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_REMOVE_NUMBER_HISTORY,
						"删减U"
				);
				// Sum(remove U)：TOTAL_UNIVERSE_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE,
						"删减U总数"
				);
				// Eva(remove U)：TOTAL_UNIVERSE_REMOVE_EVALUATION
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_REMOVE_INDIVIDUAL_EVALUATION,
						"删减贡献评估(U)=删减数/删减前剩余数",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "EVAL_REMOVE_U_LIST"
				);
				// Eva(Sum(remove U))：TOTAL_UNIVERSE_REMOVE_TOTAL_EVALUATION
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_REMOVE_TOTAL_EVALUATION,
						"删减贡献评估(U)=sum(删减数)/sum(删减前剩余数)",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "EVAL_REMOVE_U_AVERAGE"
				);

				// Eva(remove POS(U/C))：COMPACTED_UNIVERSE_POS_REMOVE_NUMBER_EVALUATION
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_POS_REMOVE_NUMBER_EVALUATION,
						"删减POS(U/C)贡献评估=删减数/删减前剩余数",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "POS_REMOVE_UC_EVAL"
				);

				// Eva(remove NEG(U/C))：COMPACTED_UNIVERSE_NEG_REMOVE_NUMBER_EVALUATION
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_NEG_REMOVE_NUMBER_EVALUATION,
						"删减NEG(U/C)贡献评估=删减数/删减前剩余数",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "NEG_REMOVE_UC_EVAL"
				);

				// Eva(remove U/C)：COMPACTED_UNIVERSE_REMOVE_INDIVIDUAL_EVALUATION
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_REMOVE_INDIVIDUAL_EVALUATION,
						"删减贡献评估(U/C)=删减数/删减前剩余数"
				);
				// Eva(Sum(remove U/C))：COMPACTED_UNIVERSE_REMOVE_TOTAL_EVALUATION
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_REMOVE_TOTAL_EVALUATION,
						"删减贡献评估(U/C)=sum(删减数)/sum(删减前剩余数)"
				);
			}

			private static void addProcedureInfo(List<RecordFieldInfo> titles) {
				// PROCEDURE_INFO
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.PROCEDURE_CLASSES_INFO,
						"测试运行使用组件"
				);
				// PROCEDURE_INFO
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.PROCEDURE_INFO,
						"测试运行组件信息(JSON)"
				);
			}

		}


		public static class QuickReduct {

			public static List<RecordFieldInfo> titles() {
				List<RecordFieldInfo> titles = new LinkedList<>();
				Common.addRecordBasicInfo(titles);
				Common.addDatasetAlgorithmInfo(titles);
				Common.addCommonRuntimeInfo(titles);
				Common.addCalculationInfo(titles);
				QuickReduct.addHeuristicSearchSignificanceCalculationLengths(titles);
				Common.addPositiveRegionRemoving(titles);
				QuickReduct.addTime(titles);
				QuickReduct.addCore(titles);
				QuickReduct.addReductResult(titles);
				Common.addProcedureInfo(titles);
				return titles;
			}

			private static void addTime(List<RecordFieldInfo> titles) {
				// TIME_INIT
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TIME_INIT,
						"初始化数据集用时(ms)",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "TIME_INIT"
				);
				// TIME_COMPRESS
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TIME_COMPRESS,
						"压缩数据集记录用时(ms)",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "TIME_COMPRESS"
				);
				// TIME_COMPRESS
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TIME_CORE,
						"求核用时(ms)",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "TIME_CORE"
				);
				// TIME_RED
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TIME_RED,
						"约简流程主要步骤(初始化、检验、求核外)用时(ms)",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "TIME_REDUCTION"
				);
				// TIME_INSPECT
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.TIME_INSPECT,
						"检验用时(ms)",
						StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "TIME_CHECK"
				);
			}

			private static void addCore(List<RecordFieldInfo> titles) {
				// CORE_INCLUDE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.CORE_INCLUDE,
						"是否求核？",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "BY_CORE"
				);
				// CORE_SIZE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.CORE_SIZE,
						"核结果个数",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "CORE_SIZE"
				);
				// CORE_LIST
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.CORE_LIST,
						"核结果列表",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "CORE_LIST"
				);
				// CORE_ATTRIBUTE_EXAM_SIZE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.CORE_ATTRIBUTE_EXAM_SIZE,
						"求核过程中遍历属性的个数"
				);
			}

			private static void addReductResult(List<RecordFieldInfo> titles) {
				// REDUCT_SIZE
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.REDUCT_SIZE,
						"约简结果属性数",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "RED_SIZE"
				);
				// REDUCT_LIST
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.REDUCT_LIST,
						"约简结果列表",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "RED_LIST"
				);
				// REDUNDANT_SIZE_BEFORE_INSPECT
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.REDUNDANT_SIZE_BEFORE_INSPECT,
						"检验结果冗余属性个数",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "CHECK_REDUNDANCY_SIZE"
				);
				// REDUNDANT_LIST_BEFORE_INSPECT
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.REDUNDANT_LIST_BEFORE_INSPECT,
						"检验结果冗余属性列表",
						StatisticsConstants.Database.TABLE_RUN_TIME_EXT_QR, "CHECK_REDUNDANCT_LIST"
				);
			}

			private static void addHeuristicSearchSignificanceCalculationLengths(List<RecordFieldInfo> titles) {
				// CALCULATION_HEURISTIC_LENGTH_IN_SEARCH_LIST
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.CALCULATION_HEURISTIC_LENGTH_IN_SEARCH_LIST,
						"寻解属性计算长度列表"
				);
				// CALCULATION_HEURISTIC_LENGTH_IN_SEARCH_LENGTH
				titleAdd(titles, StatisticsConstants.PlainRecordInfo.CALCULATION_HEURISTIC_LENGTH_IN_SEARCH_LENGTH,
						"寻解属性计算长度总和"
				);
			}
		}

		public static class Optimization {
			public static class Common {
				public static List<RecordFieldInfo> initReductsSummaryTitles(
						boolean groupByAlg
				) {
					List<RecordFieldInfo> titles = new LinkedList<>();
					// DATETIME
					titleAdd(titles, StatisticsConstants.PlainRecordInfo.DATETIME,
							"日期"
					);
					// DATASET
					titleAdd(titles, StatisticsConstants.PlainRecordInfo.DATASET_NAME,
							"数据集名字",
							StatisticsConstants.Database.TABLE_DATASET, "NAME"
					);
					if (groupByAlg) {
						// Algorithm
						titleAdd(titles, StatisticsConstants.PlainRecordInfo.ALG,
								"算法名字"
						);
					}
					// |Reduct|
					titleAdd(titles, "|Reduct|",
							"约简解个数"
					);
					// Attribute
					titleAdd(titles, "Attribute",
							"属性"
					);
					// Contains Red. number
					titleAdd(titles, "Contains Red. number",
							"含该属性的约简个数"
					);
					// Contains Red. rate
					titleAdd(titles, "Contains Red. rate",
							"含该属性的约简占比"
					);
					// appearance(attr.) / sum(red.length)
					titleAdd(titles, "appearance(attr.) / sum(red.length)",
							"该属性占约简属性总数比例=属性出现次数/sum(约简解属性数)"
					);
					return titles;
				}

				public static List<RecordFieldInfo> initDistinctReductsTitles() {
					List<RecordFieldInfo> titles = new LinkedList<>();
					// DATETIME
					titleAdd(titles, StatisticsConstants.PlainRecordInfo.DATETIME, "日期");
					// DATASET
					titleAdd(titles, StatisticsConstants.PlainRecordInfo.DATASET_NAME,
							"数据集名字",
							StatisticsConstants.Database.TABLE_DATASET, "NAME"
					);
					// Algorithm
					titleAdd(titles, StatisticsConstants.PlainRecordInfo.ALG, "算法名字");
					// |Reduct|
					titleAdd(titles, "|Reduct|", "约简解个数");
					// No.
					titleAdd(titles, "No.", "约简解序号");
					// Reduct
					titleAdd(titles, "Reduct", "约简解");
					return titles;
				}

				private static void addTimeInfo(List<RecordFieldInfo> titles) {
					// TIME_INIT
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.TIME_INIT,
							"初始化数据集用时(ms)",
							StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "TIME_INIT"
					);
					// TIME_COMPRESS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.TIME_COMPRESS,
							"压缩数据集记录用时(ms)",
							StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "TIME_COMPRESS"
					);
					// TIME_RED
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.TIME_RED,
							"约简流程主要步骤(初始化、最终检验外)用时(ms)",
							StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "TIME_REDUCTION"
					);
					// TIME_INSPECT_DURING
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.TIME_INSPECT_DURING,
							"检验(搜索过程中)用时(ms)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "TIME_SWARM_INSPECT"
					);
					// TIME_INSPECT
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.TIME_INSPECT_LAST,
							"检验(最终检验)用时(ms)",
							StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "TIME_CHECK"
					);
					// TIME_SOLUTION_SELECTION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.TIME_SOLUTION_SELECTION,
							"选最终解用时(ms)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "TIME_SOLUTION_SELECTION"
					);
				}

				private static void addReductGeneralInfo(List<RecordFieldInfo> titles) {
					// RUN_TIME_ID
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.RUN_TIME_ID,
							"次数ID"
					);
					// REDUCT_ID
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.REDUCT_ID,
							"约简解ID"
					);
					// REDUCT_DISTINCT_NUMBER
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.REDUCT_DISTINCT_NUMBER,
							"约简解总数(不含重复)"
					);
					// REDUCT_NUMBER
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.REDUCT_NUMBER,
							"约简解总数(含重复)",
							StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "RED_SIZE"
					);
					// REDUCT_CODE_NUMBER
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.REDUCT_CODE_NUMBER,
							"约简解编码总数"
					);
					// REDUCT_NO
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.REDUCT_NO,
							"单个约简解编号"
					);
					// SUM_REDUCT_CANDIDATE_NUMBER
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_CANDIDATE_NUMBER,
							"sum(约简候选解个数)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "CANDIDATE_SIZE"
					);
					// SUM_REDUCT_CANDIDATE_REDUNDANT_NUMBER
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_CANDIDATE_HAS_REDUNDANT_NUMBER,
							"sum(含冗余约简候选解个数)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "CANDIDATE_HAS_REDUNDANT_SIZE"
					);
					// MIN_CANDIDATE_ATTRIBUTE_LENGTH
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.MIN_CANDIDATE_ATTRIBUTE_LENGTH,
							"min(候选解属性个数)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "MIN_CANDIDATE_ATTR_LENGTH"
					);
					// MAX_CANDIDATE_ATTRIBUTE_LENGTH
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.MAX_CANDIDATE_ATTRIBUTE_LENGTH,
							"max(候选解属性个数)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "MAX_CANDIDATE_ATTR_LENGTH"
					);
					// AVG_CANDIDATE_ATTRIBUTE_LENGTH
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.AVG_CANDIDATE_ATTRIBUTE_LENGTH,
							"avg(候选解属性个数)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "AVG_CANDIDATE_ATTR_LENGTH"
					);
					// SUM_CANDIDATE_ATTRIBUTE_LENGTH
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.SUM_CANDIDATE_ATTRIBUTE_LENGTH,
							"sum(候选解属性个数)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "SUM_CANDIDATE_ATTR_LENGTH"
					);
					// MIN_REDUCT_LENGTH
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.MIN_REDUCT_LENGTH,
							"min(约简解属性个数)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "MIN_RED_LENGTH"
					);
					// MAX_REDUCT_LENGTH
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.MAX_REDUCT_LENGTH,
							"max(约简解属性个数)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "MAX_RED_LENGTH"
					);
					// AVG_REDUCT_LENGTH
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.AVG_REDUCT_LENGTH,
							"avg(约简解属性个数)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "AVG_RED_LENGTH"
					);
					// SUM_REDUCT_LENGTH
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_LENGTH,
							"sum(约简解属性个数)",
							StatisticsConstants.Database.TABLE_RUN_TIME_EXT_SWARM, "SUM_RED_LENGTH"
					);
					// AVG_REDUCT_CANDIDATE_REDUNDANT_ATTRIBUTE_PROPORTION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.REDUCT_CANDIDATE_REDUNDANT_ATTRIBUTE_PROPORTION,
							"冗余属性总数占解属性总数比例"
					);
				}

				private static void addReductDetailInfo(List<RecordFieldInfo> titles) {
					// LIST_REDUCT_CANDIDATE_CODES_WITH_REDUNDANCY
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.LIST_REDUCT_CANDIDATE_CODES_WITH_REDUNDANCY,
							"约简候选解编码(含冗余)"
					);
					// REDUCT_CANDIDATE_CODE_LENGTH_WITHOUT_REDUNDANCY
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.REDUCT_CANDIDATE_CODE_LENGTH_WITHOUT_REDUNDANCY,
							"约简候选解属性个数(不含冗余)"
					);
					// REDUCT_CANDIDATE_CODES_WITHOUT_REDUNDANCY
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Common.REDUCT_CANDIDATE_CODES_WITHOUT_REDUNDANCY,
							"约简候选解编码(不含冗余)"
					);
				}
			}

			public static class IterationInfos {
				public static List<RecordFieldInfo> individualTitles() {
					List<RecordFieldInfo> titles = new LinkedList<>();
					/* --------------------------------------------------------------------------------- */
					// DATETIME
					titleAdd(titles, StatisticsConstants.PlainRecordInfo.DATETIME,
							"日期时间"
					);
					// CONTAINER_ID
					titleAdd(titles, StatisticsConstants.PlainRecordInfo.CONTAINER_ID,
							"记录ID"
					);
					/* --------------------------------------------------------------------------------- */
					// DATASET
					titleAdd(titles, StatisticsConstants.PlainRecordInfo.DATASET_NAME,
							"数据集名字",
							StatisticsConstants.Database.TABLE_DATASET, "NAME"
					);
					/* --------------------------------------------------------------------------------- */
					// ALG
					titleAdd(titles, StatisticsConstants.PlainRecordInfo.ALG,
							"算法简称",
							StatisticsConstants.Database.TABLE_ALGORITHM, "NAME"
					);
					// PARAMETER_ID
					titleAdd(titles, StatisticsConstants.PlainRecordInfo.PARAMETER_ID,
							"算法参数ID",
							StatisticsConstants.Database.TABLE_RUN_TIME_GENERAL, "PARAMETER_ID"
					);
					/* --------------------------------------------------------------------------------- */
					// GENERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.GENERATION,
							"代数"
					);
					// CONVERGENCE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.CONVERGENCE,
							"收敛"
					);
					/* --------------------------------------------------------------------------------- */
					// RECORD_NUM
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.RECORD_NUM,
							"记录数"
					);
					// RECORD_CLASS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.RECORD_CLASS,
							"记录单位"
					);
					/* --------------------------------------------------------------------------------- */
					// ENTITY_NO
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_NO,
							"[个体] No."
					);
					// ENTITY_CODING_ATTR_LEN
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_CODING_ATTR_LEN,
							"[个体] 编码属性长度"
					);
					// ENTITY_FINAL_ATTR_LEN
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FINAL_ATTR_LEN,
							"[个体] 确定属性长度"
					);
					// ENTITY_FINAL_ATTR
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FINAL_ATTR,
							"[个体] 确定属性列表"
					);
					// ENTITY_INSPECTED_ATTR_LEN
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_INSPECTED_ATTR_LEN,
							"[个体] 属性列表(去冗余后)长度"
					);
					// ENTITY_INSPECTED_ATTR_LEN
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_INSPECTED_ATTR,
							"[个体] 属性列表(去冗余后)"
					);
					/* --------------------------------------------------------------------------------- */
					// ENTITY_IS_SOLUTION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_IS_SOLUTION,
							"[个体] 返回解"
					);
					// ENTITY_FITNESS_VALUE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FITNESS_VALUE,
							"[个体] fitness"
					);
					// ENTITY_SUPREME_MARK
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_SUPREME_MARK,
							"[个体] 最优解标记(- for unchanged)"
					);
					// ENTITY_SUPREME_MARK_NO
					titleAdd(titles, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_SUPREME_MARK_NO,
							"[个体] 最优解标记No.(- for unchanged)"
					);
					/* --------------------------------------------------------------------------------- */
					return titles;
				}
			}

			/**
			 * Feature Selection using Genetic Algorithm
			 */
			public static class Genetic {

				public static List<RecordFieldInfo> summaryTitles() {
					List<RecordFieldInfo> titles = new LinkedList<>();
					Title.Common.addRecordBasicInfo(titles);
					Title.Common.addDatasetAlgorithmInfo(titles);
					Title.Common.addCommonRuntimeInfo(titles);
					Common.addTimeInfo(titles);
					Title.Common.addCalculationInfo(titles);
					Common.addReductGeneralInfo(titles);
					Genetic.addParametersInfo(titles);
					//addProcedureInfo(titles);
					return titles;
				}

				public static List<RecordFieldInfo> individualTitles() {
					List<RecordFieldInfo> titles = new LinkedList<>();
					Title.Common.addRecordBasicInfo(titles);
					Title.Common.addDatasetAlgorithmInfo(titles);
					Title.Common.addCommonRuntimeInfo(titles);
					Common.addTimeInfo(titles);
					Title.Common.addCalculationInfo(titles);
					Common.addReductGeneralInfo(titles);
					Common.addReductDetailInfo(titles);
					Genetic.addParametersInfo(titles);
					Title.Common.addProcedureInfo(titles);
					return titles;
				}

				private static void addParametersInfo(List<RecordFieldInfo> titles) {
					// PARAM_POPULATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_POPULATION,
							"染色体个数"
					);
					// PARAM_GENE_LENGTH
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_GENE_LENGTH,
							"基因长度"
					);
					// PARAM_CHROMOSOME_GENE_SWITCH_NUMBER
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_CHROMOSOME_GENE_SWITCH_NUMBER,
							"染色体交换基因个数"
					);
					// PARAM_CHROMOSOME_RESERVE_NUMBER
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_CHROMOSOME_RESERVE_NUMBER,
							"染色体保留个数"
					);
					// PARAM_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_ITERATION,
							"最大迭代次数"
					);
					// PARAM_CONVERGENCE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_CONVERGENCE,
							"最大收敛次数"
					);
					// PARAM_MAX_FITNESS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_MAX_FITNESS,
							"最大fitness"
					);
					// PARAM_GENE_MUTATE_NUMBER
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_GENE_MUTATE_NUMBER,
							"基因突变个数"
					);
					// PARAM_GENE_MUTATE_PROBABILITY
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_GENE_MUTATE_PROBABILITY,
							"基因突变概率"
					);
					// PARAM_RANDOM_SEED
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_RANDOM_SEED,
							"随机种子"
					);

					// PARAM_4_ALG_CHROMOSOME_INITIATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_4_ALG_CHROMOSOME_INITIATION,
							"[算法] 染色体初始化"
					);
					// PARAM_4_ALG_CHROMOSOME_CROSS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_4_ALG_CHROMOSOME_CROSS,
							"[算法] 染色体交叉"
					);
					// PARAM_4_ALG_CHROMOSOME_CLASS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.PARAM_4_ALG_CHROMOSOME_CLASS,
							"[算法] 染色体实体"
					);

					// RECORD_EXIT_REASON
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.RECORD_EXIT_REASON,
							"退出搜索原因"
					);
					// RECORD_EXIT_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.RECORD_EXIT_ITERATION,
							"[退出时] 迭代次数"
					);
					// RECORD_EXIT_CONVERGENCE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.RECORD_EXIT_CONVERGENCE,
							"[退出时] 收敛统计"
					);
					// RECORD_EXIT_FITNESS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.RECORD_EXIT_FITNESS,
							"[退出时] fitness"
					);

					// RECORD_AVG_EXIT_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.RECORD_AVG_EXIT_ITERATION,
							"avg([退出时] 迭代次数)"
					);
					// RECORD_AVG_EXIT_CONVERGENCE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.RECORD_AVG_EXIT_CONVERGENCE,
							"avg([退出时] 收敛统计)"
					);
					// RECORD_AVG_EXIT_FITNESS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.Genetic.RECORD_AVG_EXIT_FITNESS,
							"avg([退出时] fitness)"
					);
				}

				/**
				 * Asit.K.Das incremental Feature Selection using Genetic Algorithm
				 */
				public static class AsitKDas {

					private static void addReductGeneralInfo(List<RecordFieldInfo> titles) {
						Common.addReductDetailInfo(titles);
					}

					private static void addReductDetailInfo(List<RecordFieldInfo> titles) {
						Common.addReductDetailInfo(titles);
					}

					public static List<RecordFieldInfo> individualTitles() {
						List<RecordFieldInfo> titles = new LinkedList<>();
						Title.Common.addRecordBasicInfo(titles);
						Title.Common.addDatasetAlgorithmInfo(titles);
						Title.Common.addCommonRuntimeInfo(titles);
						Common.addTimeInfo(titles);
						Title.Common.addCalculationInfo(titles);
						addReductGeneralInfo(titles);
						addReductDetailInfo(titles);
						Genetic.addParametersInfo(titles);
						Title.Common.addProcedureInfo(titles);
						return titles;
					}
				}
			}

			/**
			 * Feature Selection using Particle Swarm Optimization
			 */
			public static class ParticleSwarm {

				public static List<RecordFieldInfo> summaryTitles() {
					List<RecordFieldInfo> titles = new LinkedList<>();
					Title.Common.addRecordBasicInfo(titles);
					Title.Common.addDatasetAlgorithmInfo(titles);
					Title.Common.addCommonRuntimeInfo(titles);
					Common.addTimeInfo(titles);
					Title.Common.addCalculationInfo(titles);
					Common.addReductGeneralInfo(titles);
					ParticleSwarm.addParametersInfo(titles);
					return titles;
				}

				public static List<RecordFieldInfo> individualTitles() {
					List<RecordFieldInfo> titles = new LinkedList<>();
					Title.Common.addRecordBasicInfo(titles);
					Title.Common.addDatasetAlgorithmInfo(titles);
					Title.Common.addCommonRuntimeInfo(titles);
					Common.addTimeInfo(titles);
					Title.Common.addCalculationInfo(titles);
					Common.addReductGeneralInfo(titles);
					Common.addReductDetailInfo(titles);
					ParticleSwarm.addParametersInfo(titles);
					Title.Common.addProcedureInfo(titles);
					return titles;
				}

				private static void addParametersInfo(List<RecordFieldInfo> titles) {
					// PARAM_POPULATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_POPULATION,
							"粒子个数"
					);
					// PARAM_PARTICLE_LENGTH
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_PARTICLE_LENGTH,
							"粒子长度"
					);
					// PARAM_C1
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_C1,
							"C1：加速度常数/学习步长"
					);
					// PARAM_C2
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_C2,
							"C2：加速度常数/学习步长"
					);
					// PARAM_R1
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_R1,
							"r1：粒子个体系数"
					);
					// PARAM_R2
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_R2,
							"r2：粒子社会系数"
					);
					// PARAM_MIN_VELOCITY
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_MIN_VELOCITY,
							"最小粒子速度"
					);
					// PARAM_MAX_VELOCITY
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_MAX_VELOCITY,
							"最大粒子速度"
					);
					// PARAM_MAX_FITNESS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_MAX_FITNESS,
							"最大fitness"
					);
					// PARAM_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_ITERATION,
							"最大代数"
					);
					// PARAM_CONVERGENCE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_CONVERGENCE,
							"最大收敛次数"
					);
					// PARAM_RANDOM_SEED
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_RANDOM_SEED,
							"随机种子"
					);

					// PARAM_4_ALG_PARTICLE_INITIATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_4_ALG_PARTICLE_INITIATION,
							"[算法] 粒子初始化"
					);
					// PARAM_4_ALG_PARTICLE_UPDATE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_4_ALG_PARTICLE_UPDATE,
							"[算法] 粒子更新"
					);
					// PARAM_4_ALG_INERTIA_WEIGHT
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_4_ALG_INERTIA_WEIGHT,
							"[算法] 粒子关系权重"
					);

					// RECORD_EXIT_REASONS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_REASONS,
							"退出搜索原因 "
					);
					// RECORD_EXIT_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_ITERATION,
							"[退出时] 迭代次数"
					);
					// RECORD_EXIT_CONVERGENCE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_CONVERGENCE,
							"[退出时] 收敛统计"
					);
					// RECORD_EXIT_FITNESS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_FITNESS,
							"[退出时] fitness"
					);

					// RECORD_AVG_EXIT_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_AVG_EXIT_ITERATION,
							"avg([退出时] 迭代次数)"
					);
					// RECORD_AVG_EXIT_CONVERGENCE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_AVG_EXIT_CONVERGENCE,
							"avg([退出时] 收敛统计)"
					);
					// RECORD_AVG_EXIT_FITNESS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_AVG_EXIT_FITNESS,
							"avg([退出时] fitness)"
					);
				}
			}

			/**
			 * Feature Selection using Artificial Fish Swarm
			 */
			public static class ArtificialFishSwarm {

				public static List<RecordFieldInfo> summaryTitles() {
					List<RecordFieldInfo> titles = new LinkedList<>();
					Title.Common.addRecordBasicInfo(titles);
					Title.Common.addDatasetAlgorithmInfo(titles);
					Title.Common.addCommonRuntimeInfo(titles);
					Common.addTimeInfo(titles);
					Title.Common.addCalculationInfo(titles);
					Common.addReductGeneralInfo(titles);
					ArtificialFishSwarm.addParametersInfo(titles);
					return titles;
				}

				public static List<RecordFieldInfo> individualTitles() {
					List<RecordFieldInfo> titles = new LinkedList<>();
					Title.Common.addRecordBasicInfo(titles);
					Title.Common.addDatasetAlgorithmInfo(titles);
					Title.Common.addCommonRuntimeInfo(titles);
					Common.addTimeInfo(titles);
					Title.Common.addCalculationInfo(titles);
					Common.addReductGeneralInfo(titles);
					Common.addReductDetailInfo(titles);
					ArtificialFishSwarm.addParametersInfo(titles);
					Title.Common.addProcedureInfo(titles);
					return titles;
				}

				private static void addParametersInfo(List<RecordFieldInfo> titles) {
					// PARAM_GROUP_SIZE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_GROUP_SIZE,
							"鱼群鱼总量"
					);
					// PARAM_VISUAL
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_VISUAL,
							"鱼视野"
					);
					// PARAM_CROWD_FACTOR
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_CROWD_FACTOR,
							"拥挤系数"
					);
					// PARAM_SEARCH_TRY_NUMBER
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_SEARCH_TRY_NUMBER,
							"搜索尝试次数"
					);
					// PARAM_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_ITERATION,
							"最大代数"
					);
					// PARAM_MAX_FISH_EXIT
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_MAX_FISH_EXIT,
							"鱼退出最大个数"
					);
					// PARAM_RANDOM_SEED
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_RANDOM_SEED,
							"随机种子"
					);

					// PARAM_4_ALG_DISTANCE_MEASURE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_DISTANCE_MEASURE,
							"[算法] 距离算法"
					);
					// PARAM_4_ALG_FITNESS_CALCULATE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FITNESS_CALCULATE,
							"[算法] fitness计算"
					);
					// PARAM_4_ALG_FISH_GROUP_UPDATE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_UPDATE,
							"[算法] 鱼群更新"
					);
					// PARAM_4_ALG_FISH_GROUP_SWARM
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_SWARM,
							"[算法] 鱼群游泳"
					);
					// PARAM_4_ALG_FISH_GROUP_FOLLOW
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_FOLLOW,
							"[算法] 鱼群跟随"
					);
					// PARAM_4_ALG_FISH_GROUP_CENTER
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_CENTER,
							"[算法] 鱼群中心"
					);
					// PARAM_4_CLASS_POSITION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_CLASS_POSITION,
							"位置实体类"
					);

					// RECORD_EXIT_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_ITERATION,
							"[退出时] 迭代次数"
					);

					// RECORD_AVG_EXIT_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_AVG_EXIT_ITERATION,
							"avg([退出时] 迭代次数)"
					);
				}
			}

			/**
			 * Feature Selection using Improved Harmony Search
			 */
			public static class ImprovedHarmonySearch {

				public static List<RecordFieldInfo> summaryTitles() {
					List<RecordFieldInfo> titles = new LinkedList<>();
					Title.Common.addRecordBasicInfo(titles);
					Title.Common.addDatasetAlgorithmInfo(titles);
					Title.Common.addCommonRuntimeInfo(titles);
					Common.addTimeInfo(titles);
					Title.Common.addCalculationInfo(titles);
					Common.addReductGeneralInfo(titles);
					ImprovedHarmonySearch.addParametersInfo(titles);
					return titles;
				}

				public static List<RecordFieldInfo> individualTitles() {
					List<RecordFieldInfo> titles = new LinkedList<>();
					Title.Common.addRecordBasicInfo(titles);
					Title.Common.addDatasetAlgorithmInfo(titles);
					Title.Common.addCommonRuntimeInfo(titles);
					Common.addTimeInfo(titles);
					Title.Common.addCalculationInfo(titles);
					Common.addReductGeneralInfo(titles);
					Common.addReductDetailInfo(titles);
					ImprovedHarmonySearch.addParametersInfo(titles);
					Title.Common.addProcedureInfo(titles);
					return titles;
				}

				private static void addParametersInfo(List<RecordFieldInfo> titles) {
					// PARAM_GROUP_SIZE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_GROUP_SIZE,
							"和音种群总量"
					);
					// PARAM_HARMONY_MEMORY_SIZE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_HARMONY_MEMORY_SIZE,
							"和音个体长度"
					);
					// PARAM_HARMONY_MEMORY_CONSIDERATION_RATE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_HARMONY_MEMORY_CONSIDERATION_RATE,
							"和音延用比例"
					);
					// PARAM_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_ITERATION,
							"最大代数"
					);
					// PARAM_CONVERGENCE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_CONVERGENCE,
							"最大收敛次数"
					);
					// PARAM_RANDOM_SEED
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_RANDOM_SEED,
							"随机种子"
					);

					// PARAM_4_ALG_PITCH_ADJUSTMENT_RATE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_4_ALG_PITCH_ADJUSTMENT_RATE,
							"[算法] PAR算法"
					);
					// PARAM_4_ALG_BAND_WIDTH
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_4_ALG_BAND_WIDTH,
							"[算法] Bandwidth算法"
					);
					// PARAM_4_ALG_HARMONY_INITIALIZTION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_4_ALG_HARMONY_INITIALIZTION,
							"[算法] 和音初始化"
					);

					// RECORD_EXIT_REASON
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_REASON,
							"退出搜索原因"
					);
					// RECORD_EXIT_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_ITERATION,
							"[退出时] 迭代次数"
					);
					// RECORD_EXIT_CONVERGENCE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_CONVERGENCE,
							"[退出时] 收敛统计"
					);
					// RECORD_EXIT_FITNESS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_FITNESS,
							"[退出时] fitness"
					);

					// RECORD_AVG_EXIT_ITERATION
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_AVG_EXIT_ITERATION,
							"avg([退出时] 迭代次数)"
					);
					// RECORD_AVG_EXIT_CONVERGENCE
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_AVG_EXIT_CONVERGENCE,
							"avg([退出时] 收敛统计)"
					);
					// RECORD_AVG_EXIT_FITNESS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_AVG_EXIT_FITNESS,
							"avg([退出时] fitness)"
					);
					// RECORD_MEDIAN_ORDER_BY_ITERATION_EXIT_FITNESS
					titleAdd(titles, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_MEDIAN_ORDER_BY_ITERATION_EXIT_FITNESS,
							"median([退出时] fitness)-order by iteration"
					);
				}
			}
		}
	}

	/**
	 * Transfer record value in {@link PlainRecord} with the title set by
	 * {@link RecordFieldInfo} (<code>title</code>)
	 *
	 * @param title       {@link RecordFieldInfo} instance with title info.
	 * @param record      {@link PlainRecord} instance.
	 * @param maxItem     Maximum item of collection/array/... showed in string.
	 * @param decimalLeft Decimal left for double values.
	 * @return String transfered by {@link PlainRecord}
	 * @see StatisticsUtils#record(Object, int, int)
	 */
	public static String record(RecordFieldInfo title, PlainRecord record, int maxItem, int decimalLeft) {
		PlainRecordItem<?, ?> recordItem = record.getRecordItems().get(title.getField());
		return recordItem == null ? "null" : record(recordItem.getValue(), maxItem, decimalLeft);
	}

	/**
	 * Transfer record value (<code>value</code>) into String.
	 * <p>
	 * This method uses {@link StringBuilder} to build the returned value.
	 *
	 * @param value       The value to be transfered.
	 * @param maxItem     The maximum item showed in String.
	 * @param decimalLeft Decimal left for double values.
	 * @return Transfered String.
	 * @see StringUtils#numberToString(Collection, int, int)
	 * @see StringUtils#toString(Collection, int)
	 * @see StringUtils#intToString(int[], int)
	 * @see StringUtils#doubleToString(double[], int, int)
	 * @see StringUtils#longToString(long[], int)
	 * @see StringUtils#numberToString(Number[], int)
	 */
	@SuppressWarnings("unchecked")
	public static String record(Object value, int maxItem, int decimalLeft) {
		StringBuilder builder = new StringBuilder();
		if (value == null) {
			builder.append("null");
		} else if (value instanceof Collection) {
			Collection<?> collection = (Collection<?>) value;
			if (!collection.isEmpty()) {
				if (collection.iterator().next() instanceof Double)
					builder.append(StringUtils.numberToString((Collection<Double>) collection, maxItem, decimalLeft));
				else
					builder.append(StringUtils.toString(collection, maxItem));
			} else {
				builder.append("");
			}
		} else if (value instanceof int[]) {
			builder.append(StringUtils.intToString((int[]) value, maxItem));
		} else if (value instanceof Integer[]) {
			builder.append(StringUtils.numberToString((Integer[]) value, maxItem));
		} else if (value instanceof double[]) {
			builder.append(StringUtils.doubleToString((double[]) value, maxItem, decimalLeft));
		} else if (value instanceof Double[]) {
			builder.append(StringUtils.numberToString((Double[]) value, maxItem, decimalLeft));
		} else if (value instanceof long[]) {
			builder.append(StringUtils.longToString((long[]) value, maxItem));
		} else if (value instanceof Long[]) {
			builder.append(StringUtils.numberToString((Long[]) value, maxItem));
		} else if (value instanceof String[]) {
			builder.append(StringUtils.toString((String[]) value, maxItem));
		} else {
			builder.append(value);
		}
		return builder.toString();
	}

	/**
	 * Get the sum value of {@link Number} values in <code>collection</code>.
	 *
	 * @param collection A {@link Collection} with {@link Number}s to be sum.
	 * @return sum value in double.
	 */
	public static double numberSum(Collection<Number> collection) {
		if (collection == null || collection.isEmpty()) return 0;
		return collection.stream()
				.filter(num -> num != null)
				.map(num -> num.doubleValue())
				.reduce(Double::sum)
				.orElse(0.0);
	}

	/**
	 * Get the sum value of {@link Integer} values in <code>collection</code>.
	 *
	 * @param collection A {@link Collection} with {@link Number}s to be sum.
	 * @return sum value in int.
	 */
	public static int intSum(Collection<Integer> collection) {
		if (collection == null || collection.isEmpty()) return 0;
		return collection.stream().filter(num -> num != null).reduce(Integer::sum).get();
	}

	/**
	 * Get the average value of {@link double[]}
	 *
	 * @param array A {@link double[]}.
	 * @return sum value in double.
	 */
	public static double doubleAverage(double[] array) {
		return array == null ? 0 : Arrays.stream(array).average().getAsDouble();
	}
}
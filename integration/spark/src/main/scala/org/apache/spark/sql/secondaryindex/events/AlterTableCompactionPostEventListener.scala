/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.secondaryindex.events

import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable

import org.apache.log4j.Logger
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.CarbonMergeFilesRDD
import org.apache.spark.sql.CarbonEnv
import org.apache.spark.sql.hive.CarbonRelation
import org.apache.spark.sql.index.CarbonIndexUtil
import org.apache.spark.sql.secondaryindex.command.IndexModel
import org.apache.spark.sql.secondaryindex.load.Compactor

import org.apache.carbondata.common.logging.LogServiceFactory
import org.apache.carbondata.core.constants.CarbonCommonConstants
import org.apache.carbondata.core.index.Segment
import org.apache.carbondata.core.metadata.index.IndexType
import org.apache.carbondata.core.mutate.CarbonUpdateUtil
import org.apache.carbondata.core.statusmanager.{LoadMetadataDetails, SegmentStatusManager}
import org.apache.carbondata.events.{AlterTableCompactionPreStatusUpdateEvent, Event, OperationContext, OperationEventListener}
import org.apache.carbondata.processing.merger.{CarbonDataMergerUtil, CompactionType}

class AlterTableCompactionPostEventListener extends OperationEventListener with Logging {

  val LOGGER: Logger = LogServiceFactory.getLogService(this.getClass.getCanonicalName)

  /**
   * Called on a specified event occurrence
   *
   */
  override def onEvent(event: Event, operationContext: OperationContext): Unit = {
    event match {
      case alterTableCompactionPostEvent: AlterTableCompactionPreStatusUpdateEvent =>
        LOGGER.info("post load event-listener called")
        val carbonLoadModel = alterTableCompactionPostEvent.carbonLoadModel
        val sQLContext = alterTableCompactionPostEvent.sparkSession.sqlContext
        val compactionType: CompactionType = alterTableCompactionPostEvent.carbonMergerMapping
          .compactionType
        if (compactionType.toString
          .equalsIgnoreCase(CompactionType.SEGMENT_INDEX.toString)) {
          val carbonMainTable = carbonLoadModel.getCarbonDataLoadSchema.getCarbonTable
          val indexProviderMap = carbonMainTable.getIndexesMap
          if (!indexProviderMap.isEmpty &&
              null != indexProviderMap.get(IndexType.SI.getIndexProviderName)) {
            val iterator = indexProviderMap.get(IndexType.SI.getIndexProviderName)
              .entrySet().iterator()
            while (iterator.hasNext) {
              val index = iterator.next()
              val secondaryIndex = IndexModel(Some(carbonLoadModel.getDatabaseName),
                carbonLoadModel.getTableName,
                index.getValue.get(CarbonCommonConstants.INDEX_COLUMNS).split(",").toList,
                index.getKey)
              val metastore = CarbonEnv.getInstance(sQLContext.sparkSession)
                .carbonMetaStore
              val indexCarbonTable = metastore
                .lookupRelation(Some(carbonLoadModel.getDatabaseName),
                  secondaryIndex.indexName)(sQLContext
                  .sparkSession).carbonTable

              val validSegmentIds =
                CarbonDataMergerUtil
                  .getValidSegmentList(carbonMainTable)
                  .asScala
                  .map(_.getSegmentNo)
              // Just launch job to merge index for all index tables
              CarbonMergeFilesRDD.mergeIndexFiles(
                sQLContext.sparkSession,
                validSegmentIds,
                SegmentStatusManager.mapSegmentToStartTime(carbonMainTable),
                indexCarbonTable.getTablePath,
                indexCarbonTable,
                mergeIndexProperty = true)
            }
          }
        } else {
          val loadsToMerge =
            alterTableCompactionPostEvent
              .carbonMergerMapping
              .validSegments
              .map(_.getSegmentNo)
          val mergedLoadName = alterTableCompactionPostEvent.mergedLoadName
          val loadName = mergedLoadName
            .substring(mergedLoadName.indexOf(CarbonCommonConstants.LOAD_FOLDER) +
                       CarbonCommonConstants.LOAD_FOLDER.length)
          val factTimestamp = carbonLoadModel.getFactTimeStamp
          val mergeLoadStartTime = if (factTimestamp == 0) {
            CarbonUpdateUtil.readCurrentTime()
          } else {
            factTimestamp
          }
          val segmentIdToLoadStartTimeMapping: scala.collection.mutable.Map[String, java.lang
          .Long] = scala.collection.mutable.Map((loadName, mergeLoadStartTime))
          Compactor.createSecondaryIndexAfterCompaction(sQLContext,
            carbonLoadModel,
            List(loadName),
            loadsToMerge,
            segmentIdToLoadStartTimeMapping, forceAccessSegment = true)
        }
      case _ =>
    }
  }
}

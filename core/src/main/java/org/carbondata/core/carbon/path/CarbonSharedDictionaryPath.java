/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.carbondata.core.carbon.path;

import java.io.File;

/**
 * Helps to get Shared dimension files path.
 */
public class CarbonSharedDictionaryPath {

    static private final String SHAREDDIM_DIR = "SharedDictionary";
    static private final String DICTIONARY_EXT = ".dict";
    static private final String DICTIONARY_META_EXT = ".dictmeta";
    static private final String SORT_INDEX_EXT = ".sortindex";

    /***
     * @param storePath    store path
     * @param databaseName data base name
     * @param columnId     unique column identifier
     * @return absolute path of shared dictionary file
     */
    static public String getDictionaryFilePath(String storePath, String databaseName, String columnId) {
        return getSharedDictionaryDir(storePath, databaseName) + File.separator + columnId
                + DICTIONARY_EXT;
    }

    /***
     * @param storePath    store path
     * @param databaseName data base name
     * @param columnId     unique column identifier
     * @return absolute path of shared dictionary meta file
     */
    static public String getDictionaryMetaFilePath(String storePath, String databaseName,
            String columnId) {
        return getSharedDictionaryDir(storePath, databaseName) + File.separator + columnId
                + DICTIONARY_META_EXT;
    }

    /***
     * @param storePath    store path
     * @param databaseName data base name
     * @param columnId     unique column identifier
     * @return absolute path of shared dictionary sort index file
     */
    static public String getSortIndexFilePath(String storePath, String databaseName, String columnId) {
        return getSharedDictionaryDir(storePath, databaseName) + File.separator + columnId
                + SORT_INDEX_EXT;
    }

    static private String getSharedDictionaryDir(String storePath, String databaseName) {
        return storePath + File.separator + databaseName + File.separator + SHAREDDIM_DIR;
    }

}

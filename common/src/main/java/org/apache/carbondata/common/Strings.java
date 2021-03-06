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

package org.apache.carbondata.common;

import java.util.Objects;

import org.apache.carbondata.common.annotations.InterfaceAudience;

@InterfaceAudience.Internal
public class Strings {

  /**
   * Provide same function as mkString in Scala.
   * This is added to avoid JDK 8 dependency.
   */
  public static String mkString(String[] strings, String delimiter) {
    Objects.requireNonNull(strings);
    Objects.requireNonNull(delimiter);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < strings.length; i++) {
      builder.append(strings[i]);
      if (i != strings.length - 1) {
        builder.append(delimiter);
      }
    }
    return builder.toString();
  }

  /**
   * Append KB/MB/GB/TB to the input size and return
   * @param sizeInBytes data size in bytes
   * @return data size with unit
   */
  public static String formatSize(float sizeInBytes) {
    long KB = 1024L;
    long MB = KB << 10;
    long GB = MB << 10;
    long TB = GB << 10;
    if (sizeInBytes < 0) {
      return "NA";
    } else if (sizeInBytes >= 0 && sizeInBytes < KB) {
      return String.format("%sB", sizeInBytes);
    } else if (sizeInBytes >= KB && sizeInBytes < MB) {
      return String.format("%.2fKB", sizeInBytes / KB);
    } else if (sizeInBytes >= MB && sizeInBytes < GB) {
      return String.format("%.2fMB", sizeInBytes / MB);
    } else if (sizeInBytes >= GB && sizeInBytes < TB) {
      return String.format("%.2fGB", sizeInBytes / GB);
    } else {
      return String.format("%.2fTB", sizeInBytes / TB);
    }
  }
}

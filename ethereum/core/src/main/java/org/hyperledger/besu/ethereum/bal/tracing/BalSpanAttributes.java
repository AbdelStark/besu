/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.bal.tracing;

/**
 * Constants for OpenTelemetry span attributes related to Block-level Access Lists (BAL)
 * as defined in the EIP-7928 BAL OTel specification.
 */
public final class BalSpanAttributes {

  // Block-level BAL attributes
  /** BAL hash attribute */
  public static final String BAL_HASH = "bal.hash";

  /** Number of accounts in the BAL */
  public static final String BAL_ACCOUNTS_COUNT = "bal.accounts_count";

  /** Number of storage slots in the BAL */
  public static final String BAL_STORAGE_SLOTS_COUNT = "bal.storage_slots_count";

  /** Number of code accesses in the BAL */
  public static final String BAL_CODE_COUNT = "bal.code_count";

  /** Size of the BAL in bytes */
  public static final String BAL_SIZE_BYTES = "bal.size_bytes";

  // Transaction attributes
  /** Transaction index in block */
  public static final String TX_INDEX = "tx.index";

  /** Transaction hash */
  public static final String TX_HASH = "tx.hash";

  /** Gas used by transaction */
  public static final String TX_GAS_USED = "tx.gas_used";

  // State root calculation attributes
  /** Number of accounts updated during state root calculation */
  public static final String ACCOUNTS_UPDATED = "accounts_updated";

  /** Number of storage slots updated during state root calculation */
  public static final String STORAGE_SLOTS_UPDATED = "storage_slots_updated";

  /** Whether BAL processing was done in parallel */
  public static final String BAL_PARALLEL = "bal.parallel";

  // BAL prefetch attributes
  /** Number of accounts prefetched */
  public static final String ACCOUNTS_COUNT = "accounts_count";

  /** Number of storage slots prefetched */
  public static final String STORAGE_SLOTS_COUNT = "storage_slots_count";

  /** Number of code accesses prefetched */
  public static final String CODE_COUNT = "code_count";

  /** Total bytes of code prefetched */
  public static final String CODE_BYTES = "code_bytes";

  /** Number of cache hits during prefetch */
  public static final String CACHE_HITS = "cache_hits";

  /** Number of cache misses during prefetch */
  public static final String CACHE_MISSES = "cache_misses";

  // Resource attributes (as per Section 4 of the spec)
  /** Service name */
  public static final String SERVICE_NAME = "service.name";

  /** Service version */
  public static final String SERVICE_VERSION = "service.version";

  /** Deployment environment */
  public static final String DEPLOYMENT_ENVIRONMENT = "deployment.environment";

  /** Ethereum chain ID */
  public static final String ETHEREUM_CHAIN_ID = "ethereum.chain.id";

  private BalSpanAttributes() {
    // Utility class - prevent instantiation
  }
}
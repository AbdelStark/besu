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
package org.hyperledger.besu.ethereum.bal;

import org.hyperledger.besu.ethereum.core.Hash;

import java.util.List;
import java.util.Objects;

/**
 * Represents an individual entry in a Block-level Access List (BAL).
 * Each entry corresponds to a specific account and tracks which storage slots
 * and code were accessed during block execution.
 */
public class BlockAccessListEntry {

  private final List<Hash> storageKeys;
  private final boolean codeAccess;

  /**
   * Creates a new BlockAccessListEntry.
   *
   * @param storageKeys List of storage slot keys (hashes) that were accessed
   * @param codeAccess Whether the account's code was accessed
   */
  public BlockAccessListEntry(final List<Hash> storageKeys, final boolean codeAccess) {
    this.storageKeys = Objects.requireNonNull(storageKeys, "storageKeys cannot be null");
    this.codeAccess = codeAccess;
  }

  /**
   * Creates a new BlockAccessListEntry with only code access and no storage access.
   *
   * @param codeAccess Whether the account's code was accessed
   * @return A new entry with code access but no storage keys
   */
  public static BlockAccessListEntry codeOnly(final boolean codeAccess) {
    return new BlockAccessListEntry(List.of(), codeAccess);
  }

  /**
   * Creates a new BlockAccessListEntry with only storage access and no code access.
   *
   * @param storageKeys List of storage slot keys that were accessed
   * @return A new entry with storage access but no code access
   */
  public static BlockAccessListEntry storageOnly(final List<Hash> storageKeys) {
    return new BlockAccessListEntry(storageKeys, false);
  }

  /** @return List of storage slot keys (hashes) that were accessed */
  public List<Hash> getStorageKeys() {
    return storageKeys;
  }

  /** @return true if the account's code was accessed, false otherwise */
  public boolean hasCodeAccess() {
    return codeAccess;
  }

  /** @return true if any storage slots were accessed, false otherwise */
  public boolean hasStorageAccess() {
    return !storageKeys.isEmpty();
  }

  /** @return Number of storage slots accessed */
  public int getStorageKeysCount() {
    return storageKeys.size();
  }

  /**
   * Checks if a specific storage key was accessed.
   *
   * @param storageKey The storage key to check
   * @return true if the storage key was accessed, false otherwise
   */
  public boolean hasStorageKey(final Hash storageKey) {
    return storageKeys.contains(storageKey);
  }

  /** @return true if this entry has any access (code or storage), false otherwise */
  public boolean hasAnyAccess() {
    return codeAccess || !storageKeys.isEmpty();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final BlockAccessListEntry that = (BlockAccessListEntry) o;
    return codeAccess == that.codeAccess && Objects.equals(storageKeys, that.storageKeys);
  }

  @Override
  public int hashCode() {
    return Objects.hash(storageKeys, codeAccess);
  }

  @Override
  public String toString() {
    return String.format(
        "BlockAccessListEntry{storageKeysCount=%d, codeAccess=%s}",
        storageKeys.size(), codeAccess);
  }
}
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

import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.Hash;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a Block-level Access List (BAL) as defined in EIP-7928.
 * This is a data structure that tracks which accounts, storage slots, and code
 * are accessed during block execution to enable prefetching optimizations.
 */
public class BlockAccessList {

  private final Hash blockHash;
  private final Map<Address, BlockAccessListEntry> entries;
  private final long sizeBytes;

  /**
   * Creates a new BlockAccessList.
   *
   * @param blockHash The hash of the block this BAL belongs to
   * @param entries Map of addresses to their access list entries
   * @param sizeBytes Total size of the BAL in bytes
   */
  public BlockAccessList(
      final Hash blockHash,
      final Map<Address, BlockAccessListEntry> entries,
      final long sizeBytes) {
    this.blockHash = Objects.requireNonNull(blockHash, "blockHash cannot be null");
    this.entries = Objects.requireNonNull(entries, "entries cannot be null");
    this.sizeBytes = sizeBytes;
  }

  /** @return The hash of the block this BAL belongs to */
  public Hash getBlockHash() {
    return blockHash;
  }

  /** @return Map of addresses to their access list entries */
  public Map<Address, BlockAccessListEntry> getEntries() {
    return entries;
  }

  /** @return Total number of accounts in the BAL */
  public int getAccountsCount() {
    return entries.size();
  }

  /** @return Total number of storage slots across all accounts in the BAL */
  public int getStorageSlotsCount() {
    return entries.values().stream()
        .mapToInt(entry -> entry.getStorageKeys().size())
        .sum();
  }

  /** @return Number of accounts that have code access */
  public int getCodeCount() {
    return (int) entries.values().stream()
        .filter(BlockAccessListEntry::hasCodeAccess)
        .count();
  }

  /** @return Total size of the BAL in bytes */
  public long getSizeBytes() {
    return sizeBytes;
  }

  /** @return List of all addresses in the BAL */
  public List<Address> getAddresses() {
    return List.copyOf(entries.keySet());
  }

  /**
   * Gets the access list entry for a specific address.
   *
   * @param address The address to look up
   * @return The access list entry for the address, or null if not present
   */
  public BlockAccessListEntry getEntry(final Address address) {
    return entries.get(address);
  }

  /**
   * Checks if the BAL contains an entry for the specified address.
   *
   * @param address The address to check
   * @return true if the address is in the BAL, false otherwise
   */
  public boolean containsAddress(final Address address) {
    return entries.containsKey(address);
  }

  /** @return true if the BAL is empty (no entries), false otherwise */
  public boolean isEmpty() {
    return entries.isEmpty();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final BlockAccessList that = (BlockAccessList) o;
    return sizeBytes == that.sizeBytes
        && Objects.equals(blockHash, that.blockHash)
        && Objects.equals(entries, that.entries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockHash, entries, sizeBytes);
  }

  @Override
  public String toString() {
    return String.format(
        "BlockAccessList{blockHash=%s, accountsCount=%d, storageSlotsCount=%d, codeCount=%d, sizeBytes=%d}",
        blockHash.toHexString(),
        getAccountsCount(),
        getStorageSlotsCount(),
        getCodeCount(),
        sizeBytes);
  }
}
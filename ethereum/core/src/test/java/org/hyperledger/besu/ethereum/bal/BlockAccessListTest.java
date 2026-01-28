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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.Hash;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class BlockAccessListTest {

  private static final Hash BLOCK_HASH = Hash.fromHexString("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");
  private static final Address ADDRESS_1 = Address.fromHexString("0x1234567890123456789012345678901234567890");
  private static final Address ADDRESS_2 = Address.fromHexString("0xabcdefabcdefabcdefabcdefabcdefabcdefabcdef");
  private static final Hash STORAGE_KEY_1 = Hash.fromHexString("0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
  private static final Hash STORAGE_KEY_2 = Hash.fromHexString("0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");

  @Test
  public void shouldCreateBlockAccessListWithEntries() {
    final Map<Address, BlockAccessListEntry> entries = Map.of(
        ADDRESS_1, BlockAccessListEntry.codeOnly(true),
        ADDRESS_2, BlockAccessListEntry.storageOnly(List.of(STORAGE_KEY_1, STORAGE_KEY_2))
    );
    
    final BlockAccessList bal = new BlockAccessList(BLOCK_HASH, entries, 1024L);

    assertThat(bal.getBlockHash()).isEqualTo(BLOCK_HASH);
    assertThat(bal.getEntries()).isEqualTo(entries);
    assertThat(bal.getSizeBytes()).isEqualTo(1024L);
  }

  @Test
  public void shouldCalculateCorrectCounts() {
    final Map<Address, BlockAccessListEntry> entries = Map.of(
        ADDRESS_1, BlockAccessListEntry.codeOnly(true),
        ADDRESS_2, BlockAccessListEntry.storageOnly(List.of(STORAGE_KEY_1, STORAGE_KEY_2))
    );
    
    final BlockAccessList bal = new BlockAccessList(BLOCK_HASH, entries, 1024L);

    assertThat(bal.getAccountsCount()).isEqualTo(2);
    assertThat(bal.getStorageSlotsCount()).isEqualTo(2); // Only ADDRESS_2 has storage
    assertThat(bal.getCodeCount()).isEqualTo(1); // Only ADDRESS_1 has code access
  }

  @Test
  public void shouldReturnCorrectAddressList() {
    final Map<Address, BlockAccessListEntry> entries = Map.of(
        ADDRESS_1, BlockAccessListEntry.codeOnly(true),
        ADDRESS_2, BlockAccessListEntry.storageOnly(List.of(STORAGE_KEY_1))
    );
    
    final BlockAccessList bal = new BlockAccessList(BLOCK_HASH, entries, 1024L);

    assertThat(bal.getAddresses()).containsExactlyInAnyOrder(ADDRESS_1, ADDRESS_2);
  }

  @Test
  public void shouldGetEntryForAddress() {
    final BlockAccessListEntry entry1 = BlockAccessListEntry.codeOnly(true);
    final Map<Address, BlockAccessListEntry> entries = Map.of(ADDRESS_1, entry1);
    
    final BlockAccessList bal = new BlockAccessList(BLOCK_HASH, entries, 1024L);

    assertThat(bal.getEntry(ADDRESS_1)).isEqualTo(entry1);
    assertThat(bal.getEntry(ADDRESS_2)).isNull();
  }

  @Test
  public void shouldCheckIfContainsAddress() {
    final Map<Address, BlockAccessListEntry> entries = Map.of(
        ADDRESS_1, BlockAccessListEntry.codeOnly(true)
    );
    
    final BlockAccessList bal = new BlockAccessList(BLOCK_HASH, entries, 1024L);

    assertThat(bal.containsAddress(ADDRESS_1)).isTrue();
    assertThat(bal.containsAddress(ADDRESS_2)).isFalse();
  }

  @Test
  public void shouldDetectEmptyBAL() {
    final BlockAccessList emptyBal = new BlockAccessList(BLOCK_HASH, Map.of(), 0L);
    final BlockAccessList nonEmptyBal = new BlockAccessList(BLOCK_HASH, 
        Map.of(ADDRESS_1, BlockAccessListEntry.codeOnly(true)), 1024L);

    assertThat(emptyBal.isEmpty()).isTrue();
    assertThat(nonEmptyBal.isEmpty()).isFalse();
  }

  @Test
  public void shouldThrowWhenNullBlockHash() {
    assertThatThrownBy(() -> new BlockAccessList(null, Map.of(), 0L))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("blockHash cannot be null");
  }

  @Test
  public void shouldThrowWhenNullEntries() {
    assertThatThrownBy(() -> new BlockAccessList(BLOCK_HASH, null, 0L))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("entries cannot be null");
  }

  @Test
  public void shouldHaveCorrectEqualsAndHashCode() {
    final Map<Address, BlockAccessListEntry> entries = Map.of(
        ADDRESS_1, BlockAccessListEntry.codeOnly(true)
    );
    
    final BlockAccessList bal1 = new BlockAccessList(BLOCK_HASH, entries, 1024L);
    final BlockAccessList bal2 = new BlockAccessList(BLOCK_HASH, entries, 1024L);
    final BlockAccessList bal3 = new BlockAccessList(BLOCK_HASH, entries, 2048L); // Different size

    assertThat(bal1).isEqualTo(bal2);
    assertThat(bal1.hashCode()).isEqualTo(bal2.hashCode());
    assertThat(bal1).isNotEqualTo(bal3);
  }

  @Test
  public void shouldHaveCorrectToString() {
    final Map<Address, BlockAccessListEntry> entries = Map.of(
        ADDRESS_1, BlockAccessListEntry.codeOnly(true),
        ADDRESS_2, BlockAccessListEntry.storageOnly(List.of(STORAGE_KEY_1))
    );
    
    final BlockAccessList bal = new BlockAccessList(BLOCK_HASH, entries, 1024L);
    final String toString = bal.toString();

    assertThat(toString).contains("BlockAccessList");
    assertThat(toString).contains("accountsCount=2");
    assertThat(toString).contains("storageSlotsCount=1");
    assertThat(toString).contains("codeCount=1");
    assertThat(toString).contains("sizeBytes=1024");
    assertThat(toString).contains(BLOCK_HASH.toHexString());
  }
}
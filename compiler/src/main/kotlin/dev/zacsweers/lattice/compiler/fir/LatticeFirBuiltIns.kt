/*
 * Copyright (C) 2024 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.zacsweers.lattice.compiler.fir

import dev.zacsweers.lattice.compiler.LatticeClassIds
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent

internal class LatticeFirBuiltIns(session: FirSession, val latticeClassIds: LatticeClassIds) :
  FirExtensionSessionComponent(session) {
  companion object {
    fun getFactory(latticeClassIds: LatticeClassIds) = Factory { session ->
      LatticeFirBuiltIns(session, latticeClassIds)
    }
  }
}

internal val FirSession.latticeFirBuiltIns: LatticeFirBuiltIns by
  FirSession.sessionComponentAccessor()

internal val FirSession.latticeClassIds: LatticeClassIds
  get() = latticeFirBuiltIns.latticeClassIds

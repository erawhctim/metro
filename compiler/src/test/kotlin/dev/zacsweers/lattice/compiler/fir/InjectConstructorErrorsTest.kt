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

import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import dev.zacsweers.lattice.compiler.LatticeCompilerTest
import dev.zacsweers.lattice.compiler.assertDiagnostics
import org.junit.Test

class InjectConstructorErrorsTest : LatticeCompilerTest() {

  @Test
  fun `cannot have multiple inject targets`() {
    compile(
      source(
        """
            @Inject
            class ExampleClass @Inject constructor(private val value: String)
          """
          .trimIndent()
      ),
      expectedExitCode = ExitCode.COMPILATION_ERROR,
    ) {
      assertDiagnostics(
        "e: ExampleClass.kt:7:20 You should annotate either a class XOR constructor with `@Inject` but not both."
      )
    }
  }

  @Test
  fun `suggest moving inject annotation to class if constructor is empty`() {
    compile(
      source(
        """
            class ExampleClass @Inject constructor()
          """
          .trimIndent()
      )
    ) {
      assertDiagnostics(
        "w: ExampleClass.kt:6:20 There are no parameters on the @Inject-annotated constructor. Consider moving the annotation to the class instead."
      )
    }
  }

  @Test
  fun `cannot have multiple inject constructors`() {
    compile(
      source(
        """
            class ExampleClass @Inject constructor() {
              @Inject constructor(value: String) : this()
            }
          """
          .trimIndent()
      ),
      expectedExitCode = ExitCode.COMPILATION_ERROR,
    ) {
      assertDiagnostics("e: ExampleClass.kt:6:20 Only one `@Inject` constructor is allowed.")
    }
  }

  @Test
  fun `only classes can be injected`() {
    compile(
      source(
        fileNameWithoutExtension = "OnlyClasses",
        source =
          """
            @Inject
            enum class EnumExampleClass {
              @Inject
              INSTANCE
            }

            @Inject
            object ObjectExampleClass

            @Inject
            interface InterfaceExampleClass

            @Inject
            annotation class AnnotationExampleClass

            @Inject
            class HappyClass
          """
            .trimIndent(),
      ),
      expectedExitCode = ExitCode.COMPILATION_ERROR,
    ) {
      assertDiagnostics(
        """
            e: OnlyClasses.kt:7:12 Only classes can be annotated with @Inject or have @Inject-annotated constructors.
            e: OnlyClasses.kt:13:8 Only classes can be annotated with @Inject or have @Inject-annotated constructors.
            e: OnlyClasses.kt:16:11 Only classes can be annotated with @Inject or have @Inject-annotated constructors.
            e: OnlyClasses.kt:19:18 Only classes can be annotated with @Inject or have @Inject-annotated constructors.
          """
          .trimIndent()
      )
    }
  }

  @Test
  fun `only final classes can be injected`() {
    compile(
      source(
        fileNameWithoutExtension = "FinalClasses",
        source =
          """
            @Inject
            open class OpenExampleClass

            @Inject
            sealed class SealedExampleClass

            @Inject
            abstract class AbstractExampleClass

            @Inject
            class HappyClass
          """
            .trimIndent(),
      ),
      expectedExitCode = ExitCode.COMPILATION_ERROR,
    ) {
      assertDiagnostics(
        """
            e: FinalClasses.kt:7:1 Only final classes be annotated with @Inject or have @Inject-annotated constructors.
            e: FinalClasses.kt:10:1 Only final classes be annotated with @Inject or have @Inject-annotated constructors.
            e: FinalClasses.kt:13:1 Only final classes be annotated with @Inject or have @Inject-annotated constructors.
          """
          .trimIndent()
      )
    }
  }

  @Test
  fun `local classes cannot be injected`() {
    compile(
      source(
        """
            fun example() {
              @Inject
              class ExampleClass
            }
          """
          .trimIndent()
      ),
      expectedExitCode = ExitCode.COMPILATION_ERROR,
    ) {
      assertDiagnostics(
        "e: ExampleClass.kt:8:9 Local classes cannot be annotated with @Inject or have @Inject-annotated constructors."
      )
    }
  }

  @Test
  fun `injected classes must be visible`() {
    compile(
      source(
        fileNameWithoutExtension = "VisibleClasses",
        source =
          """
            @Inject
            private class PrivateClass

            open class Example {
              @Inject
              protected class ProtectedClass
            }

            @Inject
            internal class HappyInternalClass

            @Inject
            public class HappyPublicClass
          """
            .trimIndent(),
      ),
      expectedExitCode = ExitCode.COMPILATION_ERROR,
    ) {
      assertDiagnostics(
        """
            e: VisibleClasses.kt:7:1 Injected classes must be visible, either `public` or `internal`.
            e: VisibleClasses.kt:11:3 Injected classes must be visible, either `public` or `internal`.
          """
          .trimIndent()
      )
    }
  }

  @Test
  fun `injected constructors must be visible`() {
    compile(
      source(
        fileNameWithoutExtension = "InjectedConstructors",
        source =
          """
            @Inject
            class ClassWithPrivateConstructor private constructor()

            class ClassWithInjectedPrivateConstructor @Inject private constructor()

            class ClassWithInjectedProtectedConstructor @Inject protected constructor()

            @Inject
            class HappyClassWithExplicitInternalConstructor internal constructor()

            @Inject
            class HappyClassWithExplicitPublicConstructor public constructor()

            @Inject
            class HappyClassWithNoConstructor
          """
            .trimIndent(),
      ),
      expectedExitCode = ExitCode.COMPILATION_ERROR,
    ) {
      assertDiagnostics(
        """
            e: InjectedConstructors.kt:7:35 Injected constructors must be public or internal.
            e: InjectedConstructors.kt:9:51 Injected constructors must be public or internal.
            e: InjectedConstructors.kt:11:53 Injected constructors must be public or internal.
          """
          .trimIndent()
      )
    }
  }

  @Test
  fun `assisted factories cannot have type params`() {
    compile(
      source(
        """
            @Inject
            class ExampleClass<T> {
              @AssistedFactory
              interface Factory {
                fun <T> create(): ExampleClass<T>
              }
            }
          """
          .trimIndent()
      ),
      expectedExitCode = ExitCode.COMPILATION_ERROR,
    ) {
      assertDiagnostics(
        "e: ExampleClass.kt:10:13 `@AssistedFactory` functions cannot have type parameters."
      )
    }
  }
}

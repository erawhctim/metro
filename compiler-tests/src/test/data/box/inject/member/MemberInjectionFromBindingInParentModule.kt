interface ServiceClass { fun doSomething() }

class RealServiceClass @Inject constructor(): ServiceClass {
  override fun doSomething(): Unit {}
}

@BindingContainer
interface ServiceModule {
  @Binds fun bind(impl: RealServiceClass): ServiceClass
}

@BindingContainer
interface DebugServiceModule: ServiceModule

@DependencyGraph(bindingContainers = [DebugServiceModule::class])
interface AppGraph {
  fun inject(a: ConsumerActivity)
}

class ConsumerActivity {
  @Inject lateinit var service: ServiceClass

  fun onCreate(): Unit {
    createGraph<AppGraph>().inject(this)

    service.doSomething()
  }
}

fun box(): String {
    ConsumerActivity().onCreate()
    return "OK"
}
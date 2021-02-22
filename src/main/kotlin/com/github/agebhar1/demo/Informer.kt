package com.github.agebhar1.demo

import io.fabric8.kubernetes.api.model.Event
import io.fabric8.kubernetes.api.model.EventList
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.OperationContext
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private val logger = LoggerFactory.getLogger("main")

fun main() {

  val latch = CountDownLatch(1)

  DefaultKubernetesClient().use { client ->
    val sharedInformerFactory = client.informers()

    val podInformer =
        sharedInformerFactory.sharedIndexInformerFor(
            Event::class.java,
            EventList::class.java,
            OperationContext().withNamespace(client.configuration.namespace),
            60 * 1000)

    podInformer.addEventHandler(
        object : ResourceEventHandler<Event> {
          override fun onAdd(obj: Event?) {
            val now = Instant.now()
            val lastEventTimestamp = Instant.parse(obj?.lastTimestamp)
            if (now.minusMillis(5000).isBefore(lastEventTimestamp)) {
              logger.info("add :: {} {}", obj?.lastTimestamp, obj?.message)
            }
          }

          override fun onUpdate(oldObj: Event?, newObj: Event?) {
            val now = Instant.now()
            val lastEventTimestamp = Instant.parse(newObj?.lastTimestamp)
            if (now.minusMillis(5000).isBefore(lastEventTimestamp)) {
              logger.info("update new:: {} {} ", newObj?.lastTimestamp, newObj?.message)
            }
          }

          override fun onDelete(obj: Event?, deletedFinalStateUnknown: Boolean) {
            val now = Instant.now()
            val lastEventTimestamp = Instant.parse(obj?.lastTimestamp)
            if (now.minusMillis(5000).isBefore(lastEventTimestamp)) {
              logger.info(
                  "delete :: {} deleteFinalStateUnknown: {}",
                  obj?.message,
                  deletedFinalStateUnknown)
            }
          }
        })

    sharedInformerFactory.addSharedInformerEventListener {
      logger.error("Exception occurred, but caught: {}", it.message)
    }

    sharedInformerFactory.startAllRegisteredInformers()

    val futureTask =
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(
                { logger.debug("podInformer.hasSynced?: {}", podInformer.hasSynced()) },
                1,
                10,
                TimeUnit.SECONDS)

    latch.await()

    futureTask.cancel(true)
    sharedInformerFactory.stopAllRegisteredInformers()
  }
}

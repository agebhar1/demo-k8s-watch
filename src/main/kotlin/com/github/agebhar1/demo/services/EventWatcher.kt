package com.github.agebhar1.demo.services

import io.fabric8.kubernetes.api.model.Event
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.Watcher
import io.fabric8.kubernetes.client.WatcherException
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.Instant.now

@Service
final class EventWatcher(client: KubernetesClient) : Watcher<Event> {

  init {
    client.v1().events().watch(this)
  }

  override fun eventReceived(action: Watcher.Action, event: Event) {

    val now = now()
    val lastEventTimestamp = Instant.parse(event.lastTimestamp)

    logger.info("age: {}sec", kv("age", Duration.between(lastEventTimestamp, now).seconds))
    if (now.minusMillis(5000).isBefore(lastEventTimestamp)) {
      logger.info("{}", event.message, kv("action", action), kv("event", event))
    }
  }

  override fun onClose(exception: WatcherException) {
    logger.error("Watch error received: ${exception.message}", exception)
  }

  override fun onClose() {
    logger.info("Watch gracefully closed")
  }
}

private val logger = LoggerFactory.getLogger(EventWatcher::class.java)

package com.github.agebhar1.demo.config

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Beans {

  @Bean fun kubernetesClient() = DefaultKubernetesClient()
}

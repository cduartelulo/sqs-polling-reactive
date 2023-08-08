package com.lulobank.events;

import reactor.core.publisher.Flux;

public class FluxGenerateExample {
        public static void main(String[] args) {
            Flux<String> synchronousFlux = Flux.generate(
                    // El estado inicial
                    () -> 1,
                    // La función generadora
                    (state, sink) -> {
                        if (state <= 5) {
                            sink.next("Element " + state);
                        } else {
                            sink.complete();
                        }
                        return state + 1;
                    },
                    // La función que se llama cuando se completa el flujo
                    (state) -> System.out.println("Generation completed")
            );

            synchronousFlux.subscribe(
                    // Consumer para manejar cada elemento emitido
                    element -> System.out.println("Received: " + element),
                    // Consumer para manejar errores
                    Throwable::printStackTrace,
                    // Runnable para manejar la finalización del flujo
                    () -> System.out.println("Stream completed")
            );
        }
    }


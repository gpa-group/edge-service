package com.gpa.edge.datahub;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import java.util.concurrent.CompletionStage;

import java.time.Duration;

import io.smallrye.mutiny.Multi;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DataHubMqttImpl {

	@Inject
	@Channel("gas")
    Emitter<String> gasEmitter;
    
	@Inject
	@Channel("pollution")
    Emitter<String> pollutionEmitter;

	public void sendGas(String measure){
		System.out.println("Sending gas..");
        CompletionStage<Void> acked = gasEmitter.send(measure);
            acked.whenComplete((unused, throwable) -> {
                System.out.println("Managed to send gas data.");
            })
            .exceptionally(throwable -> {
                System.out.println("Failed to send gas message. Error: " + throwable.getMessage());
                return null;
            });
        // acked.toCompletableFuture().join();
    }
    
	public void sendPollution(String measure){
		System.out.println("Sending pollution..");
        CompletionStage<Void> acked = pollutionEmitter.send(measure);
            acked.whenComplete((unused, throwable) -> {
                System.out.println("Managed to send pollution data.");
            })
            .exceptionally(throwable -> {
                System.out.println("Failed to send pollution message. Error: " + throwable.getMessage());
                return null;
            });
        // acked.toCompletableFuture().join();
    }
}
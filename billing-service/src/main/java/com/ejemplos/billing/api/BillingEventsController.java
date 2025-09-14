package com.ejemplos.billing.api;

import com.ejemplos.common.events.SagaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

@RestController
@RequestMapping("/api/billing")
public class BillingEventsController {

    private static final Logger log = LoggerFactory.getLogger(BillingEventsController.class);

    private final BlockingQueue<SagaEvent> billingChargedQueue;
    private final BlockingQueue<SagaEvent> sagaCompletedQueue;

    public BillingEventsController(@Qualifier("billingChargedQueue") BlockingQueue<SagaEvent> billingChargedQueue,
                                   @Qualifier("sagaCompletedQueue") BlockingQueue<SagaEvent> sagaCompletedQueue) {
        this.billingChargedQueue = billingChargedQueue;
        this.sagaCompletedQueue = sagaCompletedQueue;
    }

    @GetMapping("/events")
    public ResponseEntity<Map<String, List<SagaEvent>>> pollEvents(
            @RequestParam(name = "max", defaultValue = "10") int max) {
        int limit = Math.max(1, Math.min(max, 100));
        List<SagaEvent> charged = pollMany(billingChargedQueue, limit);
        List<SagaEvent> completed = pollMany(sagaCompletedQueue, limit);
        Map<String, List<SagaEvent>> out = new HashMap<>();
        out.put("billingCharged", charged);
        out.put("sagaCompleted", completed);
        log.info("[BillingEventsController] Polled events -> billingCharged={} sagaCompleted={}", charged.size(), completed.size());
        return ResponseEntity.ok(out);
    }

    private List<SagaEvent> pollMany(BlockingQueue<SagaEvent> q, int limit) {
        List<SagaEvent> list = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            SagaEvent ev = q.poll();
            if (ev == null) break;
            list.add(ev);
        }
        return list;
    }
}

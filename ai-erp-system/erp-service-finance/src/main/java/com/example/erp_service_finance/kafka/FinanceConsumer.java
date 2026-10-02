package com.example.erp_service_finance.kafka;

import com.example.erp_service_finance.dto.OrderEvent;
import com.example.erp_service_finance.dto.PaymentEvent;
/**
 * Retained only for source compatibility. Order events are handled by
 * OrderEventListener; having a second listener would race it and bypass
 * double-entry accounting.
 */
public class FinanceConsumer {

    private final FinanceProducer financeProducer;

    // 🌟 Inject the Producer via constructor
    public FinanceConsumer(FinanceProducer financeProducer) {
        this.financeProducer = financeProducer;
    }

}
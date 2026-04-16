package com.example.eduskill.utility;

import java.time.Year;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class InvoiceUtil {

    public static String generateInvoiceNumber() {

        return "INV-" +
                Year.now().getValue() +
                "-" +
                UUID.randomUUID().toString().substring(0,8).toUpperCase();
    }
}
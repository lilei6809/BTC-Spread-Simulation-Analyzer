package com.lanlan.mock.core.events;

import lombok.Data;

@Data
public class BasePriceCreateEvent {

    private double price;
    private long timestamp;
}

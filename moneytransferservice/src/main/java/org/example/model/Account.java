package org.example.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Account {
    private String id;
    private int balance;
}

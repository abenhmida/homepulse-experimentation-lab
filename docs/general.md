## 1. The HomePulse domain
This system models a connected home.
A Home contains
```text
Home
 ├── Users
 ├── Rooms
 ├── Devices
 │    ├── Thermostat
 │    ├── Light
 │    ├── Motion sensor
 │    ├── Door sensor
 │    ├── Smoke detector
 │    ├── Energy meter
 │    └── Smart plug
 │
 ├── Automation rules
 └── Security configuration
```
We deliberately distinguish:
+ device
+ device state
+ device event
+ command
+ automation
+ alert

These are not interchangeable concepts.

## 2. Bounded contexts
The architectural decomposition is
```text
                         HOME PULSE
                              │
       ┌──────────────────────┼──────────────────────┐
       │                      │                      │
       ▼                      ▼                      ▼
 Device Management      Automation            Security
       │                      │                      │
       │                      │                      │
       ▼                      ▼                      ▼
 Device Registry        Rule Engine          Alert Engine
       │                      │                      │
       └──────────────┬───────┴──────────────┘
                      │
                      ▼
                Event Backbone
                    Kafka
                      │
             ┌────────┴────────┐
             ▼                 ▼
        State Model        Event History
             │
             ▼
          DynamoDB
```
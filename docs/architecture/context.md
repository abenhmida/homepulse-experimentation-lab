## 1. The HomePulse domain

This system models a connected home. A Home contains

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

### 2.1 Contexts

| Context           | Responsibility                |
|-------------------|-------------------------------|
| Device Management | Device identity and metadata  |
| Device Ingestion  | Receive telemetry             |
| State Management  | Maintain current device state |
| Automation        | Evaluate rules                |
| Command           | Send commands to devices      |
| Security          | Security events and alerts    |
| Notification      | Notify users                  |
| Query             | Serve read models             |
| Observability     | Metrics/traces/logs           |

A critical rule:
```text
A service owns its state. Other services interact through APIs or events rather than directly 
modifying its database.
```
## 3. Device model
A device has an identity.
```text
homeId     = home-001
deviceId   = thermostat-living-room-01
deviceType = THERMOSTAT
roomId     = living-room
```
A thermostat might expose:
```text
temperature
targetTemperature
humidity
mode
```
A motion sensor:
```text
motionDetected
```
A door sensor:
```text
open
closed
```
A light:
```text
on
off
brightness	
```
A device can be modeled as follow
```text
Device
   │
   ├── identity
   ├── metadata
   └── capabilities
```
Capabilities determine what the device can report or receive.

## 4. Device capabilities
```text
THERMOSTAT
 ├── TEMPERATURE_READING
 ├── HUMIDITY_READING
 └── TEMPERATURE_CONTROL

LIGHT
 ├── SWITCH
 └── BRIGHTNESS_CONTROL

MOTION_SENSOR
 └── MOTION_DETECTION

DOOR_SENSOR
 └── OPEN_CLOSE_DETECTION

ENERGY_METER
 └── ENERGY_READING
```
## 5. Event envelope
We don't want every event to reinvent metadata.
```text
EventEnvelope
│
├── eventId
├── eventType
├── eventVersion
├── occurredAt
├── producedAt
├── homeId
├── deviceId
├── correlationId
├── causationId
├── source
└── payload
```
such as in json form
```json
{
  "eventId": "01J...",
  "eventType": "TemperatureMeasured",
  "eventVersion": 1,
  "occurredAt": "2026-08-15T14:30:12.412Z",
  "producedAt": "2026-08-15T14:30:12.428Z",
  "homeId": "home-001",
  "deviceId": "thermostat-living-room-01",
  "correlationId": "01J...",
  "causationId": null,
  "source": "thermostat-living-room-01",
  "payload": {
    "temperature": 21.7,
    "unit": "CELSIUS"
  }
}
```
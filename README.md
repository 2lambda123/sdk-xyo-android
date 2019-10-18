[logo]: https://cdn.xy.company/img/brand/XY_Logo_GitHub.png

![logo]

# sdk-xyo-android

[![](https://travis-ci.org/XYOracleNetwork/sdk-core-kotlin.svg?branch=master)](https://travis-ci.org/XYOracleNetwork/sdk-xyo-android)[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2fb2eb69c1db455299ffce57b0216aa6)](https://www.codacy.com/app/XYOracleNetwork/sdk-xyo-android?utm_source=github.com&utm_medium=referral&utm_content=XYOracleNetwork/sdk-xyo-android&utm_campaign=Badge_Grade) [![Maintainability](https://api.codeclimate.com/v1/badges/af641257b27ecea22a9f/maintainability)](https://codeclimate.com/github/XYOracleNetwork/sdk-xyo-android/maintainability) [![](https://img.shields.io/gitter/room/XYOracleNetwork/Stardust.svg)](https://gitter.im/XYOracleNetwork/Dev)

## Table of Contents

-   [Title](#sdk-xyo-android)
-   [Description](#description)
-   [Getting Started](#getting-started)
-   [Install](#install)
-   [Implementation](#implementation)
-   [Architecture](#architecture)
-   [Maintainers](#maintainers)
-   [Contributing](#contributing)
-   [License](#license)
-   [Credits](#credits)

## Description 

A simple high-level SDK for integration of key XYO BLE and TCP methods for bound witnessing and bridging. 

## Getting Started

To integrate into your project:

Update your gradle

## Install

To use the sample app to measure functionality

- Launch [Android Studio](https://developer.android.com/studio/install)
- Click on `Open an existing Android Studio Project`
- Navigate to `<path to the sdk-xyo-android>/xyo-android-sample` in your file explorer

Once you open the sample in Android Studio it will go through the build process.

You can then run the app in a simulator of your choice or an Android device. 

## Implementation

## Architecture

This sdk is built on a client/server architecture as opposed to our past ble SDKs which were built on an idea of central/peripheral. 

> XyoSDK
  - mutableList `<XyoNode>` 
    - `XyoNode(storage, networks)`
      - `listeners`
        - `boundWitnessTarget`
    - XyoClient, XyoServer
      - Ble
        - `context`
        - `relayNode`
        - `procedureCatalog`
        - `autoBridge`
        - `acceptBridging`
        - `autoBoundWitness`
        - `scan`
    
      - TcpIp
        - `relayNode`
        - `procedureCatalog`
        - `autoBridge`
        - `acceptBridging`
        - `autoBoundWitness`

## Sample App

Please refer to the [xyo-android-sample](/xyo-android-sample/src/main/java/network/xyo/sdk/sample/MainActivity.kt) for an exmple implementation for bound witness and bridging. 

This sample app includes client bridging and bound witnessing with a BLE server listener. 

## Usage

Build an XYO Node 

```kotlin
val builder = XYONodeBuilder()
``` 

After calling the node builder, you can start the build

```kotlin
lateinit var node = XyoNode()

node = builder.build(this)
```

Once you have a build, you have access to properties to help you shape your node and what you want out of it. 

```kotlin
node.networks["this can be "ble" or "tcpip""]
```

You should use `ble` for client services.

After choosing the network, you have these properties available

Client

```kotlin
network.client.autoBridge
network.client.autoBoundWitness
network.client.scan
```

Server

```kotlin
network.server.autoBridge
network.server.listen
```

These will allow your app to actively seek devices to bound witness with and bridge from the client to the server.

You can also get payload data from the bound witness 

```kotlin
node.listener.getPayloadData(target: boundWitnessTarget)
```

This will return a byteArray.

There are other properties from the client and server which you can find in the source code as well as a reference guide that we have prepared. 

## Maintainers

- Arie Trouw

## Contributing

## License

See the [LICENSE](LICENSE) file for license details.

## Credits

Made with 🔥and ❄️ by [XY - The Persistent Company](https://www.xy.company)

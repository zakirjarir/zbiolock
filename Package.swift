// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "ZBioLockPlugin",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "ZBioLockPlugin",
            targets: ["ZBioLockPlugin"]
        )
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "ZBioLockPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm"),
            ],
            path: "ios/Sources/ZBioLockPlugin",
            // LocalAuthentication and Security are system frameworks — no linker flags needed
            swiftSettings: [
                .define("SWIFT_STRICT_CONCURRENCY", .when(configuration: .debug))
            ]
        ),
        .testTarget(
            name: "ZBioLockPluginTests",
            dependencies: ["ZBioLockPlugin"],
            path: "ios/Tests/ZBioLockPluginTests"
        )
    ]
)
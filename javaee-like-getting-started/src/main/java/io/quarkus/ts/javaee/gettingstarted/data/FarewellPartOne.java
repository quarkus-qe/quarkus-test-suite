package io.quarkus.ts.javaee.gettingstarted.data;

public sealed interface FarewellPartOne permits FarewellPartOneHappyImpl, FarewellPartOneSadImpl, FarewellPartOneDefaultMethod {
}

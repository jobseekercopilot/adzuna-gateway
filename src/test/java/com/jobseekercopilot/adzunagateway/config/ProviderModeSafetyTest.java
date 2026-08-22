package com.jobseekercopilot.adzunagateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(OutputCaptureExtension.class)
class ProviderModeSafetyTest {

    @Test
    void fixtureIsTheSafeDefaultAndNeedsNoLiveCredentials() {
        ExternalProviderProperties provider = new ExternalProviderProperties();
        AdzunaProperties adzuna = adzuna(true, "", "");

        assertThat(provider.getMode()).isEqualTo(ExternalProviderMode.FIXTURE);
        assertThatCode(() -> safety(provider, adzuna, new MockEnvironment())
                        .run(null))
                .doesNotThrowAnyException();
    }

    @Test
    void enabledLiveModeFailsClosedWhenEitherCredentialIsMissing() {
        ExternalProviderProperties provider =
                provider(ExternalProviderMode.LIVE);

        assertThatThrownBy(() -> safety(
                                provider,
                                adzuna(true, "", "private-key"),
                                new MockEnvironment())
                        .run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Adzuna LIVE mode requires both ADZUNA_APP_ID and ADZUNA_APP_KEY.")
                .hasMessageNotContaining("private-key");
        assertThatThrownBy(() -> safety(
                                provider,
                                adzuna(true, "private-id", ""),
                                new MockEnvironment())
                        .run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining("private-id");
    }

    @Test
    void configuredLiveModeLogsOnlyCredentialState(
            CapturedOutput output) {
        ExternalProviderProperties provider =
                provider(ExternalProviderMode.LIVE);
        String privateAppId = "private-live-id";
        String privateAppKey = "private-live-key";

        safety(
                        provider,
                        adzuna(true, privateAppId, privateAppKey),
                        new MockEnvironment())
                .run(null);

        assertThat(output)
                .contains(
                        "mode=LIVE",
                        "externalCallsEnabled=true",
                        "credentialsConfigured=true")
                .doesNotContain(privateAppId, privateAppKey);
    }

    @Test
    void disabledLiveProviderIsAnExplicitSafeKillSwitch() {
        assertThatCode(() -> safety(
                                provider(ExternalProviderMode.LIVE),
                                adzuna(false, "", ""),
                                new MockEnvironment())
                        .run(null))
                .doesNotThrowAnyException();
    }

    @Test
    void productionStillRejectsFixtureMode() {
        MockEnvironment environment =
                new MockEnvironment().withProperty(
                        "spring.profiles.active", "production");
        environment.setActiveProfiles("production");

        assertThatThrownBy(() -> safety(
                                provider(ExternalProviderMode.FIXTURE),
                                adzuna(true, "", ""),
                                environment)
                        .run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "cannot start in FIXTURE mode with a production profile");
    }

    private ProviderModeSafety safety(
            ExternalProviderProperties provider,
            AdzunaProperties adzuna,
            MockEnvironment environment) {
        FixtureProperties fixture = new FixtureProperties();
        fixture.setDatasetId("synthetic-dataset");
        fixture.setDatasetVersion("1.0");
        fixture.setScenario("DEMO_READY");
        return new ProviderModeSafety(
                provider, adzuna, fixture, environment);
    }

    private ExternalProviderProperties provider(ExternalProviderMode mode) {
        ExternalProviderProperties provider =
                new ExternalProviderProperties();
        provider.setMode(mode);
        return provider;
    }

    private AdzunaProperties adzuna(
            boolean enabled, String appId, String appKey) {
        AdzunaProperties adzuna = new AdzunaProperties();
        adzuna.setEnabled(enabled);
        adzuna.setAppId(appId);
        adzuna.setAppKey(appKey);
        return adzuna;
    }
}

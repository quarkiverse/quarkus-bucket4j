package io.quarkiverse.bucket4j.deployment.devui;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.bucket4j.BucketConfiguration;
import io.quarkiverse.bucket4j.deployment.RateLimitCheckBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.PageBuilder;

/**
 * Dev UI card for displaying important details such as the Bucket4J library version.
 */
public class Bucket4jDevUIProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    void createVersion(BuildProducer<CardPageBuildItem> cardPageBuildItemBuildProducer,
            List<RateLimitCheckBuildItem> rateLimitChecks) {
        final CardPageBuildItem card = new CardPageBuildItem();

        final PageBuilder versionPage = Page.externalPageBuilder("Version")
                .icon("font-awesome-solid:bucket")
                .url("https://bucket4j.com/")
                .doNotEmbed()
                .staticLabel(Objects.toString(BucketConfiguration.class.getPackage().getImplementationVersion(), "8.2.0"));
        card.addPage(versionPage);

        final PageBuilder bucketsPage = Page.tableDataPageBuilder("Buckets")
                .icon("font-awesome-solid:bucket")
                .staticLabel(
                        String.valueOf(rateLimitChecks.stream().map(RateLimitCheckBuildItem::getBucket).distinct().count()))
                .showColumn("bucket")
                .showColumn("method")
                .buildTimeDataKey("rateLimitChecks");

        card.addPage(bucketsPage);

        card.addBuildTimeData("rateLimitChecks",
                rateLimitChecks.stream().map(RateLimitCheckBuildTimeData::new).collect(Collectors.toList()));

        card.setCustomCard("qwc-bucket4j-card.js");

        cardPageBuildItemBuildProducer.produce(card);
    }

    static class RateLimitCheckBuildTimeData {
        RateLimitCheckBuildTimeData(RateLimitCheckBuildItem item) {
            this.bucket = item.getBucket();
            this.method = item.getMethodDescription().toString();
        }

        private final String bucket;
        private final String method;

        public String getMethod() {
            return method;
        }

        public String getBucket() {
            return bucket;
        }
    }
}

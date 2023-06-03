package io.quarkiverse.bucket4j.deployment.devui;

import java.util.Objects;

import io.github.bucket4j.BucketConfiguration;
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
    void createVersion(BuildProducer<CardPageBuildItem> cardPageBuildItemBuildProducer) {
        final CardPageBuildItem card = new CardPageBuildItem();

        final PageBuilder versionPage = Page.externalPageBuilder("Version")
                .icon("font-awesome-solid:bucket")
                .url("https://bucket4j.com/")
                .doNotEmbed()
                .staticLabel(Objects.toString(BucketConfiguration.class.getPackage().getImplementationVersion(), "8.2.0"));
        card.addPage(versionPage);

        card.setCustomCard("qwc-bucket4j-card.js");

        cardPageBuildItemBuildProducer.produce(card);
    }
}

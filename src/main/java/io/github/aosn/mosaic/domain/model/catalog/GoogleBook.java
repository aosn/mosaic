/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.aosn.mosaic.MosaicApplication;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Book entity for Google Book API.
 *
 * @author mikan
 * @since 0.3
 */
@SuppressWarnings("unused")
public class GoogleBook implements ReleasedBook {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    @JsonProperty
    private VolumeInfo volumeInfo;

    @Override
    public @Nullable String getIsbn() {
        return volumeInfo.industryIdentifiers == null ? null : Stream.of(volumeInfo.industryIdentifiers)
                .filter(i -> i.type.equals("ISBN_13"))
                .map(i -> i.identifier)
                .findFirst().orElse(null);
    }

    @Override
    public String getTitle() {
        return volumeInfo == null ? null : volumeInfo.title;
    }

    @Override
    public @Nullable String getSubtitle() {
        return volumeInfo == null ? null : volumeInfo.subtitle;
    }

    @Override
    public int getPageCount() {
        return volumeInfo.pageCount == null ? -1 : volumeInfo.pageCount;
    }

    @Override
    public String getPublishedDate() {
        return volumeInfo.publishedDate == null ? "" : volumeInfo.publishedDate;
    }

    @Override
    public @Nullable String getPublisher() {
        return volumeInfo.publisher;
    }

    @Override
    public List<String> getAuthors() {
        return volumeInfo == null ? Collections.emptyList() : Arrays.asList(volumeInfo.authors);
    }

    @Override
    public @Nullable String getLanguage() {
        return null;
    }

    @Override
    public boolean isEBook() {
        return volumeInfo.saleInfo == null ? false : volumeInfo.saleInfo.eBook;
    }

    @Override
    public @Nullable String getThumbnailUrl() {
        return volumeInfo.imageLinks == null ? null : volumeInfo.imageLinks.thumbnail;
    }

    private static class VolumeInfo implements Serializable {

        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

        @JsonProperty
        private String title;

        @JsonProperty
        private String subtitle;

        @JsonProperty
        private String[] authors;

        @JsonProperty
        private String publisher;

        @JsonProperty
        private String publishedDate;

        @JsonProperty
        private String description;

        @JsonProperty
        private IndustryIdentifier[] industryIdentifiers;

        @JsonProperty
        private Integer pageCount;

        @JsonProperty
        private Integer printedPageCount;

        @JsonProperty
        private String printType;

        @JsonProperty
        private ImageLinks imageLinks;

        @JsonProperty
        @Getter
        private String language;

        @JsonProperty
        private SaleInfo saleInfo;
    }

    private static class IndustryIdentifier implements Serializable {

        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

        @JsonProperty
        private String type; // ISBN_10 or ISBN_13

        @JsonProperty
        private String identifier;
    }

    private static class ImageLinks implements Serializable {

        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

        @JsonProperty
        private String smallThumbnail;

        @JsonProperty
        private String thumbnail;
    }

    private static class SaleInfo implements Serializable {

        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

        @JsonProperty
        private String country; // JP

        @JsonProperty("isEbook")
        private Boolean eBook;
    }
}

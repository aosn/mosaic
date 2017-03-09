/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.stock;

import com.google.common.base.Strings;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.catalog.ReleasedBook;
import lombok.*;
import org.apache.commons.validator.routines.ISBNValidator;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Defines the book stock.
 * <p>This entity identifies by {@link User} and ISBN-13 but duplicate entries are allowed because .</p>
 *
 * @author mikan
 * @since 0.3
 */
@Entity
@Table(name = "stocks")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock implements ReleasedBook {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    @Id
    @Column
    @GeneratedValue
    @Getter
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne
    @Getter
    private User user;

    /**
     * ISBN13.
     * <p>ISBN10 codes are must to convert to ISBN13.</p>
     *
     * @see #normalizeIsbn(String)
     */
    @Column(nullable = false, length = 13)
    @Getter
    private String isbn;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @Getter
    @Setter
    private Visibility visibility;

    /**
     * Percentage of the reading progress.
     * <p>The value range is 0 to 100. Generally, it uses as {@link Progress} with following rule.</p>
     * <ul>
     * <li>{@link Progress#NOT_STARTED} - 0</li>
     * <li>{@link Progress#IN_PROGRESS} - 1~99</li>
     * <li>{@link Progress#COMPLETED} - 100</li>
     * </ul>
     *
     * @see Progress
     */
    @Column(nullable = false)
    private int progress;

    @Column
    @Enumerated(EnumType.ORDINAL)
    @Getter
    @Setter
    private ObtainType obtainType;

    @Column
    @Temporal(TemporalType.DATE)
    @Nullable
    @Getter
    @Setter
    private Date obtainDate;

    @Column
    @Temporal(TemporalType.DATE)
    @Nullable
    @Getter
    @Setter
    private Date completedDate;

    @Column
    @Enumerated(value = EnumType.ORDINAL)
    @Getter
    @Setter
    private MediaType mediaType;

    @Column(length = 128)
    @Getter
    @Setter
    private String boughtPlace;

    @Column
    @Convert(converter = BlogStringConverter.class)
    @Getter
    @Setter
    private String shortText;

    @Column
    @Convert(converter = BlogStringConverter.class)
    @Getter
    @Setter
    private String longText;

    @Column(nullable = false, length = 200)
    @Getter
    @Setter
    private String title;

    @Column(length = 200)
    @Nullable
    @Getter
    private String subtitle;

    /**
     * @see #isValidPublishedDate(String)
     */
    @Column(nullable = false, length = 10)
    @Getter
    private String publishedDate;

    @Column(nullable = false)
    @Getter
    private int pageCount;

    @Column(length = 200)
    @Nullable
    @Getter
    private String thumbnailUrl;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @Nullable
    @Getter
    @Setter
    private Date updatedTime;

    /**
     * Checks value for acceptable published date.
     *
     * @param value accepts yyyy-mm-dd, yyyy-mm, or yyyy
     * @return {@link true} for valid, {@link false} for invalid.
     */
    public static boolean isValidPublishedDate(String value) {
        return !Strings.isNullOrEmpty(value) && (value.matches("\\d{4}") || value.matches("\\d{4}-\\d{2}") ||
                value.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * Normalize any ISBN formats to number-only ISBN13 string.
     *
     * @param isbn any ISBN formats
     * @return normalized ISBN-13 string
     * @throws NullPointerException     if {@code isbn} is {@code null}
     * @throws IllegalArgumentException if {@code isbn} is invalid
     * @see ISBNValidator
     */
    public static String normalizeIsbn(String isbn) {
        if (isbn == null) {
            throw new NullPointerException("isbn is null.");
        }
        if (isbn.isEmpty()) {
            throw new IllegalArgumentException("isbn is empty.");
        }
        String trimmed = isbn.replaceAll(" ", "").replaceAll("-", "").replaceAll(":", "").replaceAll("ISBN", "");
        ISBNValidator validator = ISBNValidator.getInstance();
        if (trimmed.length() == 10) {
            trimmed = validator.convertToISBN13(trimmed);
        }
        if (!validator.isValidISBN13(trimmed)) {
            throw new IllegalArgumentException("ISBN invalid: " + isbn);
        }
        return trimmed;
    }

    public Progress getProgress() {
        switch (progress) {
            case 0:
                return Progress.NOT_STARTED;
            case 100:
                return Progress.COMPLETED;
            default:
                return Progress.IN_PROGRESS;
        }
    }

    public void setProgressPercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("percentage accepts 0~100");
        }
        progress = percentage;
    }

    public int getProgressPercentage() {
        return progress;
    }

    public void setPublishedDate(String date) {
        if (!isValidPublishedDate(date)) {
            throw new IllegalArgumentException("Invalid date: " + date);
        }
        publishedDate = date;
    }

    public void setPageCount(int pageCount) {
        if (pageCount < 0) {
            throw new IllegalArgumentException("pageCount accepts 0 or over");
        }
        this.pageCount = pageCount;
    }

    @Override
    public @Nullable String getPublisher() {
        throw new UnsupportedOperationException("Publisher not available");
    }

    @Override
    public List<String> getAuthors() {
        throw new UnsupportedOperationException("Authors not available");
    }

    @Override
    public @Nullable String getLanguage() {
        throw new UnsupportedOperationException("Language not available");
    }

    @Override
    public boolean isEBook() {
        return mediaType != MediaType.PAPER;
    }

    public boolean isAccessible(@Nullable User accessor) {
        switch (visibility) {
            case PUBLIC:
                return true;
            case ALL_USER:
                return accessor != null;
            case PRIVATE:
                return user.equals(accessor);
            default:
                throw new UnsupportedOperationException("Unsupported visibility: " + accessor);
        }
    }

    public boolean isMine(User myself) {
        return user.equals(myself);
    }

    public enum Visibility {
        PUBLIC,
        PRIVATE,
        ALL_USER
    }

    public enum ObtainType {
        OTHER,
        BUY,
        RENT,
        ASSUME
    }

    public enum Progress {
        NOT_STARTED(0),
        IN_PROGRESS(50),
        COMPLETED(100);

        private final int actualValue;

        Progress(int actualValue) {
            this.actualValue = actualValue;
        }

        public int actualValue() {
            return actualValue;
        }
    }

    public enum MediaType {
        OTHER,
        PAPER,
        PDF,
        E_PUB,
        KINDLE
    }

    @Converter
    public static class BlogStringConverter implements AttributeConverter<String, byte[]> {

        @Override
        public byte[] convertToDatabaseColumn(@Nullable String attribute) {
            if (attribute == null) {
                attribute = "";
            }
            return attribute.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String convertToEntityAttribute(byte[] dbData) {
            return new String(dbData, StandardCharsets.UTF_8);
        }
    }
}

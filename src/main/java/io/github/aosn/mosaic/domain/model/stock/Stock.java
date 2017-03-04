/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.stock;

import io.github.aosn.mosaic.domain.model.auth.User;
import lombok.*;
import org.apache.commons.validator.routines.ISBNValidator;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

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
public class Stock {

    @Id
    @Column
    @GeneratedValue
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne
    private User user;

    /**
     * ISBN13.
     * <p>ISBN10 codes are must to convert to ISBN13.</p>
     *
     * @see #setIsbn(String)
     */
    @Column(nullable = false, length = 13)
    @Getter
    private String isbn;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Visibility visibility;

    /**
     * Percentage of the reading progress.
     * <p>The value range is 0 to 100. Generally, it uses as {@link Progress} with following rule.</p>
     * <ul>
     * <li>{@link Progress#BOUGHT} - 0</li>
     * <li>{@link Progress#IN_PROGRESS} - 1~99</li>
     * <li>{@link Progress#COMPLETED} - 100</li>
     * </ul>
     *
     * @see Progress
     */
    @Column(nullable = false)
    @Getter
    private int progress;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private ObtainType obtainType;

    @Column
    @Temporal(TemporalType.DATE)
    @Nullable
    private Date obtainDate;

    @Column
    @Temporal(TemporalType.DATE)
    @Nullable
    private Date completedDate;

    @Column
    @Enumerated(value = EnumType.ORDINAL)
    private MediaType mediaType;

    @Column(length = 128)
    private String boughtPlace;

    @Column
    @Convert(converter = BlogStringConverter.class)
    private String shortText;

    @Column
    @Convert(converter = BlogStringConverter.class)
    private String longText;

    @Column(nullable = false, length = 200)
    private String bookName;

    @Column(length = 10)
    @Nullable
    private String publishDate; // yyyy-mm-dd or yyyy-mm

    @Column(nullable = false)
    private int pages;

    @Column(length = 200)
    @Nullable
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
     * Set ISBN.
     *
     * @param isbn ISBN13 or ISBN10
     * @throws NullPointerException     if {@code isbn} is {@code null}
     * @throws IllegalArgumentException if {@code isbn} is invalid
     * @see #normalizeIsbn(String)
     */
    public void setIsbn(String isbn) {
        this.isbn = normalizeIsbn(isbn);
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

    public enum Visibility {
        PUBLIC,
        PRIVATE,
        ALL_USER
    }

    public enum ObtainType {
        OTHER,
        BUY,
        RENT,
        ASSUME,
    }

    public enum Progress {
        BOUGHT,
        IN_PROGRESS,
        COMPLETED
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
        public byte[] convertToDatabaseColumn(String attribute) {
            return attribute.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String convertToEntityAttribute(byte[] dbData) {
            return new String(dbData, StandardCharsets.UTF_8);
        }
    }
}

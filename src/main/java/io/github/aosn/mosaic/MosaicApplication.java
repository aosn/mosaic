/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MosaicApplication {

    public static final long MOSAIC_SERIAL_VERSION_UID = 1L;

    public static void main(String[] args) {
        SpringApplication.run(MosaicApplication.class, args);
    }
}

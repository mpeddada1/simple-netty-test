package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySample {

  private final static Logger LOGGER = LoggerFactory.getLogger(MySample.class);

  public static void main(String... args) {
    System.out.println("HELLLOOO");
    LOGGER.info("HELLLOO!");
  }

}

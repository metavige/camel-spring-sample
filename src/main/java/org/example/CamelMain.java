package org.example;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import lombok.extern.slf4j.Slf4j;

/**
 * MySample
 */
@Slf4j
public class CamelMain {

  public static void main(String[] args) {

    try (CamelContext context = new DefaultCamelContext()) {

//      context.addRoutes(new RouteBuilder() {
//
//        @Override public void configure() throws Exception {
//          from("file:input_dir?noop=false")
//            .process(new HelloProcessor())
//            .to("file:output_dir");
//        }
//      });

      context.start();

      synchronized (CamelMain.class) {
        CamelMain.class.wait();
      }
    }
    catch (Exception ex) {
      log.error("error", ex);
    }
  }
}
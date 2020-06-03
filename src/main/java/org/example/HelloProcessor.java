package org.example;

import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import java.util.Map;
import java.util.function.Supplier;

public class HelloProcessor implements Processor
{

  @Override public void process(Exchange exchange) throws Exception {

    Message in = exchange.getIn();
    System.out.println(in.getBody());

    in.setBody("TEST");
    exchange.setMessage(in);
  }
  // region Fields

  // endregion

  // region Methods

  // endregion
}

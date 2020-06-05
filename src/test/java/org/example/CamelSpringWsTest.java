package org.example;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.SoapJaxbDataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringDelegatingTestContextLoader;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.DisableJmx;
import org.example.soap.WsClient;
import org.example.soap.WsClientConfig;
import org.example.wsdl.Add;
import org.example.wsdl.AddResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.ws.client.core.WebServiceTemplate;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration(classes = {
  WsClientConfig.class,
  CamelSpringWsTest.class
}, loader = CamelSpringDelegatingTestContextLoader.class)
public class CamelSpringWsTest extends CamelTestSupport {

  // region Fields

  String contextPath = "org.example.wsdl";
  //  String schemaPath = "classpath:schemas/calculator.wsdl";

  @EndpointInject("mock:out")
  protected MockEndpoint resultEndpoint;

  @Produce("direct:in")
  protected ProducerTemplate template;

  @Autowired
  private WebServiceTemplate webServiceTemplate;
  // endregion

  // region Methods

  @Test
  public void testCallSpringWs() throws Exception {

    // 1. Assign
    context.addRoutes(new RouteBuilder() {

      @Override public void configure() throws Exception {

        SoapJaxbDataFormat jaxb = new SoapJaxbDataFormat(contextPath);
        jaxb.setVersion("1.2");

        from("direct:in")
          //          .marshal(jaxb)
          .log("${body}")
          //          .to("spring-ws:http://www.dneonline.com/calculator.asmx")
          .process(exchange -> {

            Object response = webServiceTemplate.marshalSendAndReceive(
              "http://www.dneonline.com/calculator.asmx",
              exchange.getIn().getBody());
            exchange.getMessage().setBody(response);

          })
          .log("${body}")
          //          .unmarshal(jaxb)
          .to("mock:out");

        getContext().setTracing(true);
      }
    });

    int a = 2, b = 7;

    //    MockEndpoint resultEndpoint = getMockEndpoint("mock:out");
    resultEndpoint.expectedMessageCount(1);
    resultEndpoint.expectedMessagesMatches(m -> {
      AddResponse addResponse = m.getIn().getBody(AddResponse.class);
      Assert.assertNotNull(addResponse);
      Assert.assertEquals(a + b, addResponse.getAddResult());

      return true;
    });

    // 2. Act
    Add addReq = new Add();
    addReq.setIntA(a);
    addReq.setIntB(b);

    template.sendBody("direct:in", addReq);

    // 3. Assert
    assertMockEndpointsSatisfied();
  }

  // endregion
}

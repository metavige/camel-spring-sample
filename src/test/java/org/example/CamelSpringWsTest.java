package org.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.model.dataformat.SoapJaxbDataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.example.soap.WsClientConfig;
import org.example.wsdl.Add;
import org.example.wsdl.AddResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WsClientConfig.class, loader = AnnotationConfigContextLoader.class)
public class CamelSpringWsTest extends CamelTestSupport {

  // region Fields

  String contextPath = "org.example.wsdl";
  String schemaPath = "classpath:schemas/calculator.wsdl";

  // endregion

  // region Methods

  @Test
  public void testCallSpringWs() throws Exception {

    // 1. Assign
    int a = 2, b = 7;
    context.addRoutes(new RouteBuilder() {

      @Override public void configure() throws Exception {

        //        JaxbDataFormat jaxb = new JaxbDataFormat(true);
        //        jaxb.setContextPath(contextPath);
        ////        jaxb.setSchemaLocation(schemaPath);
        SoapJaxbDataFormat jaxb = new SoapJaxbDataFormat(contextPath);
        jaxb.setVersion("1.2");

        from("direct:in")
          .marshal(jaxb)
          .log("${body}")
          .to(
            "spring-ws:http://www.dneonline.com/calculator.asmx")
          .log("${body}")
          .unmarshal(jaxb)
          .to("mock:out");
      }
    });

    MockEndpoint mockEndpoint = getMockEndpoint("mock:out");
    mockEndpoint.expectedMessageCount(1);
    mockEndpoint.expectedMessagesMatches(m -> {
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

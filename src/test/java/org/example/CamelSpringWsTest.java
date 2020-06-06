package org.example;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.SoapJaxbDataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.example.soap.WsClientConfig;
import org.example.wsdl.Add;
import org.example.wsdl.AddResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.ws.client.core.WebServiceTemplate;

@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration(classes = {
  WsClientConfig.class
}, loader = AnnotationConfigContextLoader.class)
@EnableAutoConfiguration
public class CamelSpringWsTest extends CamelTestSupport {

  // region Fields

  private String contextPath = "org.example.wsdl";
  private String webServiceUri = "http://www.dneonline.com/calculator.asmx";
  //  String schemaPath = "classpath:schemas/calculator.wsdl";

  @EndpointInject("mock:out")
  protected MockEndpoint resultEndpoint;

  @Produce("direct:in")
  protected ProducerTemplate template;

  @Autowired
  private WebServiceTemplate webServiceTemplate;
  // endregion

  // region Methods

  /**
   * 測試使用 WebServiceClient 來直接呼叫 WebService，這邊使用的是 @see Processor
   * @throws Exception 測試發生錯誤
   */
  @Test
  public void testSpringWsFromProcess() throws Exception {

    // 1. Assign
    context.addRoutes(new RouteBuilder() {

      @Override public void configure() {

        from("direct:in")
          .log("${body}")
          .process(exchange -> {

            Object response = webServiceTemplate
              .marshalSendAndReceive(webServiceUri, exchange.getIn().getBody());
            exchange.getMessage().setBody(response);

          })
          .log("${body}")
          .to("mock:out");

//        getContext().setTracing(true);
      }
    });

    int a = 2, b = 7;

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

    template.sendBody(addReq);

    // 3. Assert
    MockEndpoint.assertIsSatisfied(context);
  }

  /**
   * 測試使用 Camel 提供的 Spring WS 支援來呼叫 WebService
   *
   * TODO: 這部分因為對 Inject 進去的 Bean 有問題，還沒測試出來
   *
   * @throws Exception 測試發生錯誤
   */
//  @Test
  public void testCamelSpringWebService() throws Exception {

    // 1. Assign
    context.addRoutes(new RouteBuilder() {

      @Override public void configure() {

        SoapJaxbDataFormat jaxb = new SoapJaxbDataFormat(contextPath);
        jaxb.setVersion("1.2");

        from("direct:in")
          .marshal(jaxb)
          .log("${body}")
          .to(String.format("spring-ws:%s?webServiceTemplate=#wst", webServiceUri))
          .log("${body}")
          .unmarshal(jaxb)
          .to("mock:out");

//        getContext().setTracing(true);
      }
    });

    int a = 2, b = 7;

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
    MockEndpoint.assertIsSatisfied(context);
  }
  // endregion
}

package org.example;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.SoapJaxbDataFormat;
import org.apache.camel.spi.Registry;
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
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.xml.transform.Source;

@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration(classes = {
  WsClientConfig.class
}, loader = AnnotationConfigContextLoader.class)
@EnableAutoConfiguration
public class CamelSpringWsTest extends CamelTestSupport {

  // region Fields

  private String webServiceUri = "http://www.dneonline.com/calculator.asmx";

  @EndpointInject("mock:out")
  protected MockEndpoint resultEndpoint;

  @Produce("direct:in")
  protected ProducerTemplate template;

  @Autowired
  private WebServiceTemplate webServiceTemplate;

  @Autowired
  private WebServiceMessageFactory messageFactory;

  @Autowired
  private Jaxb2Marshaller marshaller;

  // endregion

  // region Methods

  /**
   * 測試使用 WebServiceClient 來直接呼叫 WebService，這邊使用的是 @see Processor
   *
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

            // 直接用 Process, 呼叫 Spring-WS WebServiceTemplate 來處理 SOAP
            // 這樣的確比較容易懂，而且相對來講，不太依賴 Camel Spring-WS Endpoint
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
   * @throws Exception 測試發生錯誤
   */
  @Test
  public void testCamelSpringWebService() throws Exception {

    // 1. Assign
    context.addRoutes(new RouteBuilder() {

      @Override public void configure() {

        // 這邊只需要注入 messageFactory, 讓他支援 SOAP 1.2 就可以
        // 測試案例啟動的時候，會去呼叫 bindToRegistry 方法註冊要用的 Bean
        // 目前找到是要在這邊註冊一個要用的 Bean (之後再測試實際使用 Spring 正式執行是否不需要)
        from("direct:in")
          .to(String.format("spring-ws:%s?messageFactory=#messageFactory", webServiceUri))
          .transform(new Expression() {

            @Override public <T> T evaluate(Exchange exchange, Class<T> type) {
              // 用來轉換 SOAP XML -> Java Bean
              return (T) marshaller.unmarshal(exchange.getIn().getBody(Source.class));
            }
          })
          .log("output: ${body}")
          .to("mock:out");
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

  /**
   * 協助注入要提供給 Camel 內使用的 Bean
   *
   * @param registry
   * @throws Exception
   */
  @Override protected void bindToRegistry(Registry registry) throws Exception {

    registry.bind("messageFactory", messageFactory);
  }

  // endregion
}

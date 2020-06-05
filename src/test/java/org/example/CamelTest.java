package org.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.example.data.CompositeData;
import org.example.data.DetailData;
import org.example.data.MasterData;
import org.example.soap.WsClientConfig;
import org.example.wsdl.Add;
import org.example.wsdl.AddResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
  "classpath:META-INF/spring/camel-context-test.xml"
})
public class CamelTest extends CamelTestSupport {

  // region Fields

  @Value("classpath:data/sample-master.txt") Resource masterRes;
  @Value("classpath:data/sample.txt") Resource composeRes;

  // endregion

  // region Methods

  /**
   * 測試抓出 MockEndpoint 的資料，並判斷結果
   *
   * @throws Exception
   */
  @Test
  public void testMockOutput() throws Exception {
    // 1. Assign
    context.addRoutes(new RouteBuilder() {

      @Override public void configure() throws Exception {

        from("direct:in")
          .to("mock:out");
      }
    });

    String message = "hello";

    MockEndpoint mockEndpoint = getMockEndpoint("mock:out");
    mockEndpoint.expectedBodiesReceived(message);

    // 2. Act
    template.sendBody("direct:in", message);

    // 3. Assert
    assertMockEndpointsSatisfied();
  }

  /**
   * 測試 Bindy 切割字串變成物件
   *
   * @throws Exception
   */
  @Test
  public void testUnmarshal() throws Exception {
    // 1. Assign
    context.addRoutes(new RouteBuilder() {

      @Override public void configure() throws Exception {

        from("direct:in")
          .unmarshal(new BindyCsvDataFormat(MasterData.class))
          .to("mock:out");
      }
    });

    String masterData = getResBody(masterRes);
    MasterData aMaster = new MasterData(
      "A00001",
      "Ricky Chiang",
      Date.from(LocalDate
        .of(2020, 2, 10)
        .atStartOfDay(ZoneId.of("Asia/Taipei"))
        .toInstant())
    );

    // 2. Act
    MockEndpoint mock = getMockEndpoint("mock:out");
    mock.expectedBodiesReceived(aMaster);

    // 3. Assert
    template.sendBody("direct:in", masterData);

    assertMockEndpointsSatisfied();
  }

  /**
   * 測試註冊一個 Converter, 來轉換比較複雜的物件
   * <p>
   * TODO: 這個部分沒用到 Bindy 的轉換
   *
   * @throws Exception
   */
  @Test
  public void testConverter() throws Exception {

    // 1. Assign
    context.getTypeConverterRegistry()
      .addTypeConverter(CompositeData.class, String.class, new CompositeDataConverter());
    context.addRoutes(new RouteBuilder() {

      @Override public void configure() throws Exception {

        from("direct:in")
          .convertBodyTo(CompositeData.class)
          .to("mock:out");
      }
    });

    String compositeData = getResBody(composeRes);

    // 2. Act
    MockEndpoint mockEndpoint = getMockEndpoint("mock:out");
    mockEndpoint.expectedMessageCount(1);
    mockEndpoint.expectedMessagesMatches(m -> {
      CompositeData body = m.getMessage().getBody(CompositeData.class);

      Assert.assertNotNull(body);
      Assert.assertEquals("A00001", body.getMaster().getId());
      Assert.assertEquals(4, body.getDetails().size());

      return true;
    });

    template.sendBody("direct:in", compositeData);

    // 3. Assert
    assertMockEndpointsSatisfied();

  }

  /**
   * 測試 Split + Aggregator
   *
   * @throws Exception
   */
  @Test
  public void testSplitAggregate() throws Exception {

    // 1. Assign
    context.addRoutes(new RouteBuilder() {

      @Override public void configure() throws Exception {

        from("direct:in")
          .log("${header.myId}")
          .split()
          .tokenize(System.lineSeparator(), 1)
          .aggregationStrategy(new CompositeAggregateStrategy())
          //.log("${exchangeProperty.CamelSplitIndex}")
          .choice()
          .when(simple("${exchangeProperty.CamelSplitIndex} == 0"))
          .unmarshal(new BindyCsvDataFormat(MasterData.class))
          .when(simple("${exchangeProperty.CamelSplitIndex} > 0"))
          .unmarshal(new BindyCsvDataFormat(DetailData.class))
          .end()
          .end()
          .to("mock:out");
      }
    });

    MockEndpoint mockEndpoint = getMockEndpoint("mock:out");
    mockEndpoint.expectedMessageCount(1);
    mockEndpoint.expectedMessagesMatches(m -> {
      CompositeData data = m.getIn().getBody(CompositeData.class);
      Assert.assertNotNull(data);
      Assert.assertEquals(4, data.getDetails().size());

      return true;
    });

    // 2. Act
    template.sendBodyAndHeader("direct:in",
      getResBody(composeRes),
      "myId",
      "header-abc");

    // 3. Assert
    assertMockEndpointsSatisfied();
  }

  // region Private Methods

  private String getResBody(Resource res) throws IOException {

    return new String(Files.readAllBytes(res.getFile().toPath()));
  }

  // endregion

  // endregion
}

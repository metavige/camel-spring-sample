package org.example;

import org.apache.camel.test.junit4.TestSupport;
import org.example.soap.WsClient;
import org.example.soap.WsClientConfig;
import org.example.wsdl.AddResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WsClientConfig.class, loader = AnnotationConfigContextLoader.class)
public class WebServiceTest extends TestSupport {
  // region Fields

  @Autowired
  private WsClient wsClient;

  // endregion

//  @Autowired
//  public WebServiceTest(WsClient wsClient) {
//
//    this.wsClient = wsClient;
//  }

  // region Methods

  @Test
  public void testCallAddWebService() {

    // 1. Assign
    int a = 2, b = 3;

    // 2. Act
    AddResponse response = wsClient.add(a, b);

    // 3. Assert
    Assert.assertEquals(a + b, response.getAddResult());
  }

  // endregion
}

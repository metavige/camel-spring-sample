package org.example.soap;

import org.example.wsdl.Add;
import org.example.wsdl.AddResponse;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

@Component
public class WsClient extends WebServiceGatewaySupport {

  // region Fields

  // endregion

  public WsClient(WebServiceMessageFactory messageFactory) {

    super(messageFactory);
  }

  // region Methods

  public AddResponse add(int a, int b) {

    Add addRequest = new Add();
    addRequest.setIntA(a);
    addRequest.setIntB(b);

    return (AddResponse) getWebServiceTemplate().marshalSendAndReceive(addRequest);
  }

  // endregion
}

package org.example.soap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

@Configuration
@Slf4j
public class WsClientConfig {

  // region Fields
  public final static String WS_URL = "http://www.dneonline.com/calculator.asmx";
  // endregion

  // region Methods
  @Bean
  public WebServiceMessageFactory messageFactory() {

    SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
    messageFactory.setSoapVersion(SoapVersion.SOAP_12);
    return messageFactory;
  }

  @Bean
  public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller, WebServiceMessageFactory messageFactory) {

    log.info("create new webServiceTemplate");
    WebServiceTemplate webServiceTemplate = new WebServiceTemplate(messageFactory);
    webServiceTemplate.setMarshaller(marshaller);
    webServiceTemplate.setUnmarshaller(marshaller);
    return webServiceTemplate;
  }

  /**
   * 設定轉換 Java Bean <-> XML 的轉換器 (Marshaller)
   *
   * @return 回傳 @see Jaxb2Marshaller
   */
  @Bean
  public Jaxb2Marshaller marshaller() {

    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // 會掃瞄此類下面的對應的 jaxb2 實體類 因為是使用 marshaller 和 unmarshaller 來進行 xml 和 bean 直接轉換的
    // 具體是判斷此路徑下是否包含 ObjectFactory.class 文件

    // 設置 JAXBContext 對象
    marshaller.setContextPath("org.example.wsdl");
    return marshaller;
  }

  /**
   * 設定 WebService Client
   *
   * @param marshaller
   * @return
   */
  @Bean
  public WsClient wsClient(Jaxb2Marshaller marshaller, WebServiceMessageFactory messageFactory) {

    WsClient client = new WsClient(messageFactory);
    // 默認對應的ws服務地址 client 請求中還能動態修改的
    client.setDefaultUri(WS_URL);
    client.setMarshaller(marshaller);// 指定轉換類
    client.setUnmarshaller(marshaller);
    // 改變預設的 SOAP (1.1 -> 1.2)
    //    client.setMessageFactory(messageFactory);
    return client;
  }
  // endregion
}

package org.example.soap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class WsClientConfig {

  // region Fields
  private String WS_URL = "http://www.dneonline.com/calculator.asmx";
  // endregion

  // region Methods

  /**
   * 設定轉換 Java Bean <-> XML 的轉換器 (Marshaller)
   * @return
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
   * @param marshaller
   * @return
   */
  @Bean
  public WsClient wsClient(Jaxb2Marshaller marshaller) {

    WsClient client = new WsClient();
    // 默認對應的ws服務地址 client 請求中還能動態修改的
    client.setDefaultUri(WS_URL);
    client.setMarshaller(marshaller);// 指定轉換類
    client.setUnmarshaller(marshaller);
    return client;
  }
  // endregion
}

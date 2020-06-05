## Camel

Camel 的測試專案，用測試案例測試 Camel 的使用方式

- 確認怎麼使用 MockEndpoint
- 測試 Bindy 的使用
- 測試 Converter
- 測試 Split - Aggregator

## Web Service (Call asmx)

- 使用 jaxb2 產生 Java Code (with WSDL)
- 使用 spring-ws 來呼叫 Web Service
- 有發現 `Server did not recognize the value of HTTP Header SOAPAction` 的錯誤
    - 後來找到，需要改變預設的 SOAP Version，要使用 SOAP 1.2 才可以 (spring-ws 預設 1.1) 

### 參考

- https://howtodoinjava.com/spring-boot/spring-soap-client-webservicetemplate/
